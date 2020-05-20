package tw.org.iii.yichun.foodsharing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.org.iii.yichun.foodsharing.Global.MainUtils;
import tw.org.iii.yichun.foodsharing.Global.MyCamera.MyCamara2;
import tw.org.iii.yichun.foodsharing.Item.User;
import tw.org.iii.yichun.foodsharing.Loading.LoginActivity;
import tw.org.iii.yichun.foodsharing.profile.ProfileFragment;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.allview)
    RelativeLayout allview;

    private BottomNavigationView bmView;
    private ViewPager viewPager;
    private Intent intent;
    private myReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //ALERT_WINDOW();//檢查用戶是否開啟懸浮視窗


            init();

            // 將首頁設為 default fragment
            if (savedInstanceState == null && MainUtils.isGotoProfile()==false) {
                bmView.setSelectedItemId(R.id.tabHome);
            }else if (MainUtils.isGotoProfile()==true){
                bmView.setSelectedItemId(R.id.tabProfile);
                MainUtils.setGotoProfile(false);
            }



        receiver = new myReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("lipin");
        filter.addAction("EditFoodCard");
        registerReceiver(receiver,filter);

    }
    /**
     * 實作廣播器
     */
    private class myReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("lipin","我是廣播器2");
            if (intent.getAction().equals("lipin")){
                String Notice = intent.getStringExtra("Notice");
                Log.v("lipin","我是廣播器");
               Snackbar snackbar =  Snackbar.make(allview,Notice,Snackbar.LENGTH_INDEFINITE);
               snackbar.setAction("ok", new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                   }
               });
               snackbar.show();
            }
            if (intent.getAction().equals("EditFoodCard")){
                MainUtils.setGotoProfile(true);
            }
        }
    }


    /**
     * 檢查用戶是否有開啟懸浮視窗
     */
//    private void ALERT_WINDOW() {
//        if (!Settings.canDrawOverlays(MainActivity.this)) {
//
//            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
//                    .setTitle("您需要開啟懸浮視窗")
//                    .setMessage("如果需要在程式使用中時顯示對方通知,您需要開啟懸浮視窗")
//                    .setNegativeButton("前往開啟", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                                    Uri.parse("package:" + getPackageName()));
//                            startActivityForResult(intent, 10);
//                        }
//                    })
//                    .setPositiveButton("不開啟", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            Snackbar.make(allview,"程式在使用中時,系統將不會提示您有誰想拿取食物",Snackbar.LENGTH_LONG).show();
//                        }
//                    });
//            dialog.show();
//
//        }
//    }

    private void init() {
        bmView = findViewById(R.id.bottom_nav_view);
        viewPager = findViewById(R.id.view_pager);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new NotificationFragment());
        fragments.add(new MessageFragment());
        fragments.add(new HomeFragment());
        fragments.add(new ProfileFragment());
        fragments.add(new ShopFragment());

        FragmentAdapter adapter = new FragmentAdapter(fragments, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        changeBottomNav();
        setToolBar();
    }


    // 自訂 ToolBar
    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.nav_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.nav_add) {
                    toaddfood();

                } else if (i == R.id.nav_about) {

                } else if (i == R.id.nav_feedback) {

                } else if (i == R.id.nav_logout) {
                    Sign_out();
                }

                return false;
            }
        });

//      setSupportActionBar(toolbar);
//
//      getSupportActionBar().setDisplayShowTitleEnabled(true); //隱藏 toolbar default title
//      getSupportActionBar().setDisplayHomeAsUpEnabled(false); //是否顯示返回鍵
//      getSupportActionBar().setHomeButtonEnabled(true); // 左上圖示是否可以點擊
    }
    /**
     * 點擊toolbar上的登出按鈕
     */
    private void Sign_out(){

        UIhandler uIhandler = new UIhandler();//刪除token需在後端執行,因此實作出handler,讓他回到主線程

        uIhandler.sendEmptyMessage(50);//刪除現在用戶的token

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    /**
     * 刪除token需在後端執行,因此實作出handler,讓他回到主線程
     */
    private class UIhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==50){
                mythread();
            }
        }
    }

    /**
     * 刪除現在用戶使用的token
     */
    private void mythread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                   Log.v("lipin","刪除token:"+e.toString());
                }
            }
        }).start();
    }

    /**
     * 點擊toolbar上的＋,會進入新增卡片的頁面
     */
    private void toaddfood() {
        Intent intent = new Intent(this, AddFoodActivity.class);
        startActivity(intent);
        finish();
    }

    // 點擊 Bottom Nav 事件
    private void changeBottomNav() {
        bmView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int currentIndex = viewPager.getCurrentItem();
                int menuId = menuItem.getItemId();

                switch (menuId) {
                    case R.id.tabNotification:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.tabMessage:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.tabHome:
                        viewPager.setCurrentItem(2);
                        break;
                    case R.id.tabProfile:
                        viewPager.setCurrentItem(3);
                        break;
                    case R.id.tabShop:
                        viewPager.setCurrentItem(4);
                        break;
                }

                switch (menuId) {
                    case 0:
                }
                return false;
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i < 5) {
                    bmView.getMenu().getItem(i).setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        // 禁止 viewPager 左右滑動
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    /**
     * 改寫返回鍵,如果在首頁點擊返回鍵,此程式並不會死掉
     */
    @Override
    public void onBackPressed() {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * 暫停要做的事
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);

        super.onDestroy();
    }
}



