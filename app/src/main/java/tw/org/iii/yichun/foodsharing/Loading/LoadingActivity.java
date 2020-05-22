package tw.org.iii.yichun.foodsharing.Loading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import tw.org.iii.yichun.foodsharing.BuildConfig;
import tw.org.iii.yichun.foodsharing.Global.Utils;
import tw.org.iii.yichun.foodsharing.Global.MainUtils;
import tw.org.iii.yichun.foodsharing.Item.User;
import tw.org.iii.yichun.foodsharing.MainActivity;
import tw.org.iii.yichun.foodsharing.R;


/**
 * 讀取頁面
 */
public class LoadingActivity extends AppCompatActivity {

    private String account;
    private String password;
    private int intcount;
    private LocationManager mLastLocation;//取得現在位置(經緯度)
    Intent intent;
    boolean ispermission;
    boolean Location;//檢查是否取得到地址了
    private  String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        /**
         * 拿取權限
         * gps權限
         */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SYSTEM_ALERT_WINDOW)
                        != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO},
                    123);
        } else {
            ispermission = true;
            //取得位置管理員
            mLastLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

    }

    /**
     * 每500毫秒監聽是否有取得經度
     */
    private class timertack extends TimerTask{
        int count = 0;
        @Override
        public void run() {
            count++;
            Log.v("lipin",count+"次數");
             if (User.getLatitude()!= null){
                 Location = true;
                 cancel();
                 Log.v("lipin",User.getLatitude().toString()+"::::::::"+User.getLongitude().toString());
             }else if (count>=5){   //如果超過五次就不再執行
                    Log.v("lipin",count+"次數");
                    cancel();
             }else {
                 Location = false;
            }
        }
    }

    /**
     * 延遲3分鐘,查看是否有拿到經度,有拿到與沒拿到做的事不一樣
     */
    private class timertack2 extends TimerTask{

        @Override
        public void run() {
            important();
            cancel();
        }
    }

    /**
     * 有無權限都要做的事
     */
    private void important(){
        getaccount();//拿取存在手機內的帳號密碼

        Log.v("lipin", account + password);

        if (account != null) {
            Log.v("lipin", "有執行媽01");
            //判斷token 是否一樣
            VerifyToken();

            //如果是二次登入者,讓他存取該user的資訊,然後Auto_login()方法比對帳密是否正確要跳轉哪個頁面
            User();//將user資訓存到靜態類
        } else {
            Log.v("lipin", "有執行媽02");
            //如果是第一次登入者,無帳號,或有帳號密碼錯誤者
            Auto_login();//讓他判斷要跳轉哪個頁面
        }
    }


    //執行完權限確認才做的事
    private void init() {
        Timer timer = new Timer();

        MainUtils.showloading(this);
        Log.v("lipin","2");
        timer.schedule(new timertack(),0, 500);

        timer.schedule(new timertack2(),1000*3);

    }


    /**
     *要求客戶需要有權限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ispermission = true;
            mLastLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Gps();
            init();
            Log.v("lipin",6+"");

        } else {
            ispermission = false;

            //如果不打開,gps權限,做的事
            Timer timer = new Timer();
            MainUtils.showloading(this);
            timer.schedule(new timertack2(),1000*3);//一樣讓他跑三秒,不過就不會再獲得gps地址

            Toast toast = Toast.makeText(this,
                    "若不開啟GPS我們將無法幫您判斷您附近有多少剩食提供者", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

        }
        mLastLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.v("lipin","1");

    }

    //    退後台home鍵效果
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }


    private void getaccount() {
        account = getSharedPreferences("FoodSharing_User", MODE_PRIVATE)
                .getString("account", null);
        password = getSharedPreferences("FoodSharing_User", MODE_PRIVATE)
                .getString("password", null);
    }

    /**
     * 判斷自動哪取出來的帳號密碼
     * 表單key:FoodSharing_User
     */
    private void Auto_login() {
        if (account != null && password != null) {
            VerifyID();
        } else {
            intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
            MainUtils.dimissloading();
            Log.v("lipin","5");
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


        }
    }

    /**
     * 拿取以存取帳密到web端判斷,最後判斷token是否有被更改,如果有更改,將會update新的token
     */
    private void VerifyID() {
        String url = "http://" + Utils.ip + "/FoodSharing_war/Sql_Login_Verify";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getVerify(response);//拿取驗證是否成功的頁面
                        Verify();//驗證帳號是否正確
                        Log.v("lipin", account + "::" + password + "::" + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("account", account);
                params.put("passwd", password);
                params.put("token",token);
                return params;
            }
        };
        MainUtils.queue.add(request);
    }

    /**
     * 拿取驗證結果 正確為1 不正確為0
     * @param respons
     */
    private void getVerify(String respons) {
        String count = respons.substring(0, 1);
        intcount = Integer.valueOf(count);
    }

    /**
     * 確認是否為正確的帳密
     */
    private void Verify() {
        if (intcount == 1) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            MainUtils.dimissloading();

        } else {
            intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            MainUtils.dimissloading();
        }
    }

    /**
     * 拿取user全部資料
     */
    private void User() {
        String url = "http://" + Utils.ip + "/FoodSharing_war/Sql_getUser";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        MainUtils.setUser(response);
                        Log.v("lipin", "執行完了");
                        Auto_login();
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
                HashMap<String, String> params = new HashMap<>();
                params.put("account", account);
                params.put("passwd", password);

                Log.v("lipin", account + "::::::::" + password);

                return params;
            }
        };
        MainUtils.queue.add(request);
    }

    private MyListener myListener;
    @Override
    protected void onStart() {
        super.onStart();
        if (mLastLocation!=null) {
            Gps();
            init();
            Log.v("lipin", "onStart");
        }
    }
    /**
     * 實作gps監聽
     */
    private void Gps(){
        myListener = new MyListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = mLastLocation.getLastKnownLocation(LocationManager.GPS_PROVIDER);//取得最後已知的座標
        if (location != null) {
            User.setLatitude(location.getLatitude());//拿取經度,傳給user靜態類保存
            User.setLongitude(location.getLongitude());//拿取偉度,傳給user靜態類保存
        }
        mLastLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, myListener);
    }

    /**
     * gps監聽
     */
    private class MyListener implements LocationListener {
        /**
         * 拿取現在位置
         */
        @Override
        public void onLocationChanged(Location location) {
            if(location != null){
                User.setLatitude(location.getLatitude());
                User.setLongitude(location.getLongitude());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    /**
     * 拿取當下tonken比對是否一樣
     */
    private void VerifyToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
            }
        });
    }




}
