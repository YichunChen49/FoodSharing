package tw.org.iii.yichun.foodsharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import tw.org.iii.yichun.foodsharing.Global.MainUtils;
import tw.org.iii.yichun.foodsharing.Global.Utils;
import tw.org.iii.yichun.foodsharing.Item.User;

public class HomeFragment extends Fragment {

    private Intent intent;
    private ImageView selectmap,filter;
    private SearchView search;
    private ListView listView;
    private String searchQuery;//搜尋文字
    private boolean mHasLoadedOnce;// 页面已经加载过

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);



        selectmap = view.findViewById(R.id.selectmap);
        filter = view.findViewById(R.id.filter);
        search = view.findViewById(R.id.search);
        listView = view.findViewById(R.id.home_lv);




        Log.v("lipin","2");

            if (mHasLoadedOnce){
                MainUtils.showloading(getContext());
                Log.v("lipin","3");
            }

           gotomap(); //去地圖頁面

           gotoFilter();//去進階搜尋頁面

           search();//狀態欄監聽客戶搜尋功能

           Verifyposition();//判斷是否有取得經緯度,顯示出不同的foodcard資訊

           listViewClickListener();//監聽客戶點擊哪個食物卡片


//        Log.v("lipin", User.getToken());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("lipin","321");
        this.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        mHasLoadedOnce = isVisibleToUser;
        if (!isVisibleToUser){
            this.onDestroyView();
            Log.v("lipon","123");
        }
    }

    /**
     * 狀態欄搜尋功能
     */
    private void search() {
        search.setIconifiedByDefault(false);//使整個search都可被點擊,不再只有圖示可點擊

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;

                //虛擬鍵盤點擊搜尋才會觸發
                if (query.length()>=1) {
                    MainUtils.showloading(getContext());
                    searching(query);
                }

                Log.v("lipin", "搜尋1");

                search.clearFocus();//清除鍵盤

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //虛擬鍵盤有打出字就會持續觸發
                Log.v("lipin", "搜尋2");
                return false;
            }
        });
    }
    /**
     * Volley搜尋功能
     */
    private void searching(String query){
        String url = "http://"+Utils.ip+"/FoodSharing_war/Search";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JsonFoodcard(response);

                        listView.setAdapter(new ListViewAdapter(getActivity(), MainUtils.getList()));

                        MainUtils.dimissloading();

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
                HashMap<String,String> params = new HashMap();

                params.put("query",query);
                params.put("userid",User.getId());

                return params;
            }
        };
        MainUtils.queue.add(request);
    }

    /**
     * 判斷是否有取得經緯度,做不同的事
     */
    private void Verifyposition(){
        if (User.getLongitude() != null) {
            getposition();//取得現在所在位置轉成文字地址
        } else {
            getfoodcard();//資料庫獲得食物資訊,並將食物資訊放在list裡面,把參數給予適配器adpader
        }
    }

    /**
     * 監聽客戶點擊第幾個食物卡片
     */
    private void listViewClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //做出畫面-點擊第0個，跳到該卡片詳細資料頁面 (Giver視角)

                Log.v("lipin", position + "查看");

                GotoFoodinfoTaker(position, view);
                //fragment的跳轉頁面動畫寫法,須先getActivity
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * 點擊前往跳食物詳細頁面
     */
    private void GotoFoodinfoTaker(int position, View view) {
        intent = new Intent(view.getContext(), FoodinfoTaker.class);

        intent.putExtra("position",position);//將序列化的hashmap丟給下個頁面處理

        startActivity(intent);
    }

    /**
     * 去地圖頁面
     */
    private void gotomap() {
        selectmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), tw.org.iii.yichun.foodsharing.GoogleMap.Map.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    //去進階搜尋
    private void gotoFilter() {
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchFilterActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    /**
     * 食物 ListView Adapter
     */
    public class ListViewAdapter extends BaseAdapter {

        private List<HashMap<String, Object>> data;
        private LayoutInflater layoutInflater;
        private Context context;

        public ListViewAdapter(Context context, List<HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
            this.layoutInflater = LayoutInflater.from(context);
        }

        /**
         * HomeList Item 集合，對應 listView_home.xml
         */
        public final class ListItem {
            public ImageView image; // 食物照片
            public TextView title, location, deadline, quantity, leftQuantity;
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

            // 取得 listView_home.xml
            ListItem ListItem = new ListItem();
            convertView = layoutInflater.inflate(R.layout.listview_home, null);
            ListItem.image = (ImageView) convertView.findViewById(R.id.foodList_img);
            ListItem.title = (TextView) convertView.findViewById(R.id.foodList_title);
            ListItem.location = (TextView) convertView.findViewById(R.id.foodList_location);
            ListItem.deadline = (TextView) convertView.findViewById(R.id.foodList_deadline);
            ListItem.quantity = (TextView) convertView.findViewById(R.id.foodList_quantity);
            ListItem.leftQuantity = (TextView) convertView.findViewById(R.id.foodList_leftQuantity);
            convertView.setTag(ListItem);

            // 綁定資料

            ListItem.image.setImageBitmap((Bitmap) data.get(position).get("image"));
            ListItem.title.setText((String) data.get(position).get("title"));
            ListItem.location.setText("地區:" + (String) data.get(position).get("city") +
                    (String) data.get(position).get("dist"));
            ListItem.deadline.setText("期限:" + (String) data.get(position).get("deadline"));
            ListItem.quantity.setText("可否拆領:" + (String) data.get(position).get("quantity"));
            ListItem.leftQuantity.setText("預估剩餘份數:" + (String) data.get(position).get("leftQuantity") + "份");

            return convertView;
        }
    }

    /**
     * 抓取現在客戶經緯度轉換為,拿取區域,與縣市
     */
    private void getposition() {

        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&language=zh-TW&key=AIzaSyDn1AwRWz2oQX_oOr5CobxB1PRe-Vg2eyA",
                User.getLatitude(), User.getLongitude());

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        jsonposition(response);

                        getfoodcard();//資料庫獲得食物資訊,並將食物資訊放在list裡面,把參數給予適配器adpader


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("lipin", error.toString());
                    }
                }
        );
        MainUtils.queue.add(request);
    }

    /**
     * 將傳回來了json地址拉出來存取地區,縣市
     */
    private void jsonposition(String response) {
        try {
            JSONObject root = new JSONObject(response);
            String statis = root.getString("status");
            //檢查是否為ok,google如果有拿取到正確地址會在status欄位顯示ok

            Log.v("lipin", "status" + statis);

            if (statis.equals("OK")) {
                JSONArray results = root.getJSONArray("results");
                JSONObject result = results.getJSONObject(0);
                JSONArray address_components = result.getJSONArray("address_components");
                JSONObject city = address_components.getJSONObject(3);
                JSONObject dist = address_components.getJSONObject(2);

                //將轉出來的縣市地址傳入權域靜態類中
                User.setCity(city.getString("short_name"));
                User.setDist(dist.getString("short_name"));

            }

        } catch (Exception e) {
            Log.v("lipin", e.toString());
        }


    }

    /**
     * 抓取sql內的食物資訊
     */
    private void getfoodcard() {
        String url = "http://" + Utils.ip + "/FoodSharing_war/Sql_getAddFoodCard";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //讀取sql中的foodcard符合該user的全部資料
                        JsonFoodcard(response);

                        //將抓下來的參數給予建構式中的ListViewAdapter class


                            listView.setAdapter(new ListViewAdapter(getActivity(), MainUtils.getList()));

                            MainUtils.dimissloading();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("lipin", error.toString());
                    }
                }
        ) {
            //拿取user當前地址,如果抓不到user當前位置就抓取foodcard最新20個表單
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("userid", User.getId());
                //檢查有無取得地址,如果有的話,將地址傳給後端,讓顯示在首頁的不再是最新的20個資訊,而是你附近地址的資訊
                if (User.getDist() != null && User.getCity() != null) {
                    params.put("city", User.getCity());
                    params.put("dist", User.getDist());
                    Log.v("lipin", User.getCity() + "22222");
                    return params;
                } else {
                    return params;
                }
            }
        };
        MainUtils.queue.add(request);
    }

//    /**
//     * 將sql讀出的資料丟進執行緒中
//     */
//    private void fetchfoodcard(){
//        new Thread(){
//            @Override
//            public void run() {
//                Log.v("lipin","有來媽");
//                UIhandler uIhandler = new UIhandler();
//                MainUtils.showloading(getContext());
//
//
//
//                //顯示在首頁的資訊讓他在執行緒內跑
//                uIhandler.sendEmptyMessage(10);
//            }
//        };
//    }
//
//    /**
//     * 把轉為資訊的foodcard加入到主線成
//     */
//    private class UIhandler extends Handler{
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            if (msg.what == 10){
//
//
//                MainUtils.dimissloading();
//            }
//        }
//    }


    /**
     * 抓取json的食物資訊
     *
     * @param response
     */
    JSONObject row;
    HashMap<String, Object> hashMap;
    private void JsonFoodcard(String response) {

        List<HashMap<String, Object>>list = new ArrayList<HashMap<String, Object>>();

        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                row = array.getJSONObject(i);

                getData(list);//將抓取的值一個一個放在listview裡面

            }
            MainUtils.setList(list);

        } catch (Exception e) {
            Log.v("lipin", "JsonFoodcard:" + e.toString());
        }

    }

    /**
     * 食物 ListView getData
     */
    Bitmap bitmap;
    // TODO: 2020/4/27 撈資料庫資料
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

        Log.v("lipin", dueDate);

        //取出base64的圖片
        String base64Img = row.optString("foodimg");
        //轉成bitmap
        bitmap = MainUtils.base64Tobitmap(base64Img);

        //直接將資料顯示在首頁資訊

        hashMap.put("image", bitmap);
        hashMap.put("title", row.optString("name"));
        hashMap.put("city", row.optString("city"));
        hashMap.put("dist", row.optString("dist"));
        hashMap.put("deadline", dueDate);
        hashMap.put("quantity", stringSplit);
        hashMap.put("leftQuantity", row.optString("qty"));

        //下面東東目的,當客戶端點擊後進入詳細資料時可查看
        hashMap.put("account", row.optString("account"));
        hashMap.put("username", row.optString("username"));
        hashMap.put("address", row.optString("address"));
        hashMap.put("detail", row.optString("detail"));
        hashMap.put("category", row.optString("category"));
        hashMap.put("tag", row.optString("tag"));
        hashMap.put("foodid",row.optString("id"));
        hashMap.put("token", row.optString("token"));

        list.add(hashMap);
    }

    /**
     * 1.在這個xml加入 android:focusableInTouchMode="true" 開啟焦點功能
     * 2.search.clearFocus(); //關閉焦點
     * 3.隱藏鍵盤
     * 使搜尋按鈕不會自動彈跳出鍵盤,此段為隱藏鍵盤
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(search.getWindowToken(),0);

        search.clearFocus();

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(v, InputMethodManager.SHOW_FORCED); //show software keyboard
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //hide
                }
            }
        });
    }

    @Override
    public void onStop() {
        search.setQuery("",false);//清空搜尋欄
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v("lipin","onDestroy");
        if (bitmap!=null) {
            bitmap.recycle();
            System.gc();
        }
        super.onDestroy();
    }

}
