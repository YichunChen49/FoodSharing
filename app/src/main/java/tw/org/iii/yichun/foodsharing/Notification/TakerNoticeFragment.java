package tw.org.iii.yichun.foodsharing.Notification;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.org.iii.yichun.foodsharing.AddFoodActivity;
import tw.org.iii.yichun.foodsharing.FoodinfoGiver;
import tw.org.iii.yichun.foodsharing.Global.MainUtils;
import tw.org.iii.yichun.foodsharing.Global.Utils;
import tw.org.iii.yichun.foodsharing.Item.User;
import tw.org.iii.yichun.foodsharing.MainActivity;
import tw.org.iii.yichun.foodsharing.R;
import tw.org.iii.yichun.foodsharing.profile.TakeHistoryFragment;


public class TakerNoticeFragment extends Fragment {
    private ListView listView;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

            view = inflater.inflate(R.layout.fragment_taker_notice, container, false);

            listView = view.findViewById(R.id.notification_comment);

            MainUtils.showloading(getActivity());
            getTakerqueue();


        return view;
    }

    private class ListViewAdapter extends BaseAdapter {
        private List<HashMap<String, Object>> data;
        private LayoutInflater layoutInflater;
        private Context context;

        public ListViewAdapter(Context context, List<HashMap<String, Object>> data){
            this.context = context;
            this.data = data;
            this.layoutInflater = LayoutInflater.from(context);
        }
        public void removeItem(int index){
            data.remove(index);
        }

        /**
         * List Item 集合，對應 listView_profile_history.xml
         */
        public final class ListItem{
            public TextView notification_msg; // 食物照片
            public TextView notification_time; // 食物名稱
            public LinearLayout allitem;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 取得 listView_profile_history.xml
            ListItem ListItem = new ListItem();
            convertView = layoutInflater.inflate(R.layout.listview_notification, null);
            ListItem.notification_msg = (TextView) convertView.findViewById(R.id.notification_msg);
            ListItem.notification_time = (TextView) convertView.findViewById(R.id.notification_time);
            ListItem.allitem = convertView.findViewById(R.id.allitem);
            convertView.setTag(ListItem);

            // 綁定資料
            ListItem.notification_msg.setText("關於您的"+data.get(position).get("title")+",用戶"+
                    (String) data.get(position).get("account")+"想向您索取食物");
            ListItem.notification_time.setText((String)data.get(position).get("modified"));

            return convertView;
        }
    }
    //監聽使用者點擊第幾個item
    private void ListviewListener(){

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("lipin","監聽"+position);

                Log.v("lipin",(String)
                        list.get(position).get("foodid")+"::"+(String) list.get(position).get("userid"));


                //拿取自訂的dialog
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View newview = inflater.inflate(R.layout.mydialog3, null);
                //拿取自訂dialog的ID
                ImageView imag = newview.findViewById(R.id.foodList_img);
                TextView foodname = newview.findViewById(R.id.foodList_title);
                TextView dueDate = newview.findViewById(R.id.foodList_deadline);
                TextView split = newview.findViewById(R.id.foodList_quantity);
                TextView qty = newview.findViewById(R.id.foodList_leftQuantity);

                imag.setImageBitmap((Bitmap) list.get(position).get("image"));
                foodname.setText((String) list.get(position).get("title"));
                dueDate.setText("索取時間:" + (String)list.get(position).get("createtime"));
                split.setText("索取數量:" + (String) list.get(position).get("qty"));
                qty.setText("預估剩餘份數:" + (String) list.get(position).get("leftQuantity") + "份");

                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getActivity())
                        .setTitle((String) list.get(position).get("account")+"想索取食物")
                        .setView(newview)
                        .setNegativeButton("讓他進入排隊", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateQueue(position);
                                /**
                                 * 通知對方,傳送對方token
                                 */
                                String url = "http://" + Utils.ip + "/FoodSharing_war/GiverMessgingService";
                                StringRequest request = new StringRequest(
                                        Request.Method.POST,
                                        url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.v("lipin", error.toString());
                                            }
                                        }
                                ) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        HashMap<String, String> param = new HashMap<>();

//                                            Log.v("lipin", "123124" + data.get(position).get("token").toString().trim());

                                        param.put("TakerToken", list.get(position).get("token").toString().trim());//拿取對方token
                                        param.put("UserToken", User.getToken());//拿取我方token
                                        param.put("username", User.getAccount());
                                        param.put("foodname", list.get(position).get("title").toString());

                                        return param;
                                    }
                                };
                                MainUtils.queue.add(request);
                            }

                        })
                        .setPositiveButton("不讓他排隊", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listViewAdapter.removeItem(position);
                                listViewAdapter.notifyDataSetChanged();
                            }
                        });
                dialog.show();


            }
        });
    }
    /**
     * 更新排隊狀態
     */
    private void updateQueue(int position){
        String url = "http://"+ Utils.ip+"/FoodSharing_war/updatequequ";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                            MainUtils.dimissloading();
                            listViewAdapter.removeItem(position);
                            listViewAdapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("lipin",error.toString());
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();
                params.put("foodid", (String) list.get(position).get("foodid"));
                params.put("userid", (String) list.get(position).get("userid"));
                params.put("qty",(String) list.get(position).get("qty"));

                return params;
            }
        };
        MainUtils.queue.add(request);
    }
    /**
     * 拿取想排隊的人的通知
     */
    ListViewAdapter listViewAdapter;
    private void getTakerqueue(){
        String url = "http://"+ Utils.ip+"/FoodSharing_war/SharerNotice";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonFoodcard(response);
                       listViewAdapter = new ListViewAdapter(getActivity(),list);

                        listView.setAdapter(listViewAdapter);
                        MainUtils.dimissloading();
                        Log.v("lipin",list.toString());
                        ListviewListener();//監聽使用者點擊第幾個item

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();
                params.put("userid", User.getId());



                return params;
            }
        };
        MainUtils.queue.add(request);
    }
    /**
     * 抓取json的食物資訊
     *
     * @param response
     */
    JSONObject row;
    HashMap<String, Object> hashMap;
    List<HashMap<String, Object>>list;
    private void JsonFoodcard(String response) {

        list = new ArrayList<HashMap<String, Object>>();

        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                row = array.getJSONObject(i);

                getData(list);//將抓取的值一個一個放在listview裡面

            }

        } catch (Exception e) {
            Log.v("lipin", "JsonFoodcard:" + e.toString());
        }

    }

    /**
     * 食物 ListView getData
     */
    // TODO: 2020/4/27 撈資料庫資料
    Bitmap bitmap;
    public void getData( List<HashMap<String, Object>>list) throws ParseException {
        //因為list加入的方式的比對方式是地址,重複的地址物件會被蓋過,所以需要每次尋訪時是產生新的hashMap,故在此new出來
        hashMap = new HashMap<String, Object>();
        //拿取拆領的數字,0 or 1
        int split = Integer.valueOf(row.optString("split"));

        //將拆領的值更改為字串
        String stringSplit = null;
        if (split == 1) {
            stringSplit = "可拆領";
        } else if (split == 0) {
            stringSplit = "不可拆領";
        }
        //將sql回來的截止時間,優化顯示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = format.parse(row.optString("due_date"));
        String dueDate = format.format(date);

        //取出base64的圖片
        String base64Img = row.optString("foodimg");
        //轉成bitmap
        bitmap = MainUtils.base64Tobitmap(base64Img);

        //直接將資料顯示在首頁資訊

        hashMap.put("image", bitmap);
        hashMap.put("title", row.optString("name"));

        //下面東東目的,當客戶端點擊後進入詳細資料時可查看
        hashMap.put("city", row.optString("city"));
        hashMap.put("dist", row.optString("dist"));
        hashMap.put("deadline", dueDate);
        hashMap.put("quantity", stringSplit);
        hashMap.put("leftQuantity", row.optString("qty"));
        hashMap.put("account", row.optString("account"));
        hashMap.put("username", row.optString("username"));
        hashMap.put("address", row.optString("address"));
        hashMap.put("detail", row.optString("detail"));
        hashMap.put("category", row.optString("category"));
        hashMap.put("tag", row.optString("tag"));
        hashMap.put("token", row.optString("token"));
        hashMap.put("foodid",row.optString("id"));
        hashMap.put("status",row.optString("status"));
        hashMap.put("createtime",row.optString("createtime"));
        hashMap.put("userid",row.optString("user_id"));
        hashMap.put("qty",row.optString("takerqty"));
        hashMap.put("modified",row.optString("modified"));
        list.add(hashMap);
    }

    @Override
    public void onStop() {
        if (bitmap!=null) {
            bitmap.recycle();
            System.gc();
        }
        super.onStop();
    }
}
