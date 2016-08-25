package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AccessibilityManager.AccessibilityStateChangeListener
{
    //开关切换按钮
    private ImageButton imagebtn ;
    private TextView isGrasping;
    private ImageView imageview_2;
    private AccessibilityManager accessibilityManager;

    //底部三个按钮
    private ImageButton imgbtn_setting;
    private ImageButton imgbtn_share;
    private ImageButton imgbtn_help;

    //四个文本控件
    public  TextView num_total ;
    public  TextView num_today ;
    public  TextView money_total;
    public  TextView money_today ;
//-----------------------new items---------------------------------//
    private Switch openWechat_switch;
    private Switch openQQ_switch;

    //左上角两个个按钮
    private ImageButton setting_imagebtn;
    private ImageButton help_imagebtn;
    private LinearLayout shouldOpenServer_layout;

    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

//        sharedPreferences.registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);

        ImageView iv = (ImageView) findViewById(R.id.imageView_1);
//        iv.setImageResource(R.mipmap.bg_top);
        ImageView iv_downimg = (ImageView) findViewById(R.id.btn_bgimg);
//        iv_downimg.setImageResource(R.mipmap.bat_down_money);

        imagebtn = (ImageButton)findViewById(R.id.imageButton_1);
        imagebtn.setBackgroundColor(Color.TRANSPARENT);
        imagebtn.setOnClickListener(myClickListener);

        isGrasping = (TextView)findViewById(R.id.textView_1);

        imageview_2 = (ImageView) findViewById(R.id.imageview_2);
//      imageview_2.setImageResource(R.mipmap.icon_money);


        imgbtn_setting = (ImageButton) findViewById(R.id.imgbtn_settings);
        imgbtn_setting.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_setting.setImageResource(R.mipmap.icon_circularset);
//        imgbtn_setting.setOnClickListener(onClickSetting);

        imgbtn_share = (ImageButton) findViewById(R.id.imgbtn_share);
        imgbtn_share.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_share.setImageResource(R.mipmap.icon_share);
        imgbtn_share.setOnClickListener(onClickShare);

        imgbtn_help = (ImageButton) findViewById(R.id.imgbtn_help);
        imgbtn_help.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_help.setImageResource(R.mipmap.icon_help);
//        imgbtn_help.setOnClickListener(onClickHelp);

        //四个文本控件
         num_total = (TextView)findViewById(R.id.num_total);
         num_today = (TextView)findViewById(R.id.num_today);
         money_total = (TextView)findViewById(R.id.money_total);
         money_today = (TextView)findViewById(R.id.money_today);

        //-----------------------new items--------------------------//
        //开关
        openWechat_switch = (Switch) findViewById(R.id.open_wechat_switch);
        openWechat_switch.setOnCheckedChangeListener(wechat_swtich_listener);
        openQQ_switch = (Switch) findViewById(R.id.open_qq_switch);
        openQQ_switch.setOnCheckedChangeListener(qq_switch_listener);

        //写到这  TODO
        setting_imagebtn = (ImageButton) findViewById(R.id.imageButton_setting);
        help_imagebtn = (ImageButton) findViewById(R.id.imageButton_help);

        shouldOpenServer_layout = (LinearLayout) findViewById(R.id.should_openServer);
//        shouldOpenServer_layout.setVisibility(View.INVISIBLE);
        updateServiceStatus();
        showDatas();
        //获取设置中开关的状态
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean watchOnLockFlag = sharedPreferences.getBoolean("pref_watch_notification", false);
        if (watchOnLockFlag)
        {
            Log.i("TAG", "trueeeeee");
        } else
        {
            Log.i("TAG", "falseeeeeee");
        }
    }

    private void showDatas()
    {
        SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);

        Log.i("TAG", "初始总红包数量:"+ String.valueOf(sharedP.getInt("totalnum",0)));
        Log.i("TAG", "初始总资产:"+ sharedP.getString("totalmoney",""));
        //显示数据
        num_total.setText(String.valueOf(sharedP.getInt("totalnum",0)));

        if ("".endsWith(sharedP.getString("totalmoney","")))
        {
            money_total.setText("0.00");
        }else
        {
            money_total.setText(sharedP.getString("totalmoney",""));
        }
        //今天的数据
        num_today.setText(String.valueOf(sharedP.getInt("totalnum",0)));
        if ("".endsWith(sharedP.getString("totalmoney","")))
        {
            money_today.setText("0.00");
        }else
        {
            money_today.setText(sharedP.getString("totalmoney",""));
        }

    }

    public static MainActivity getInstance()
    {
        return instance;
    }

    private View.OnClickListener onClickSetting = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.i("TAG","setting");
            Intent settingAvt = new Intent(MainActivity.this,SettingActivity.class);
            settingAvt.putExtra("title", "设置");
            settingAvt.putExtra("frag_id", "GeneralSettingsFragment");
            startActivity(settingAvt);
        }
    };

    private  View.OnClickListener onClickShare = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.i("TAG","share");
        }
    };

    private  View.OnClickListener onClickHelp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.i("TAG","help");
        }
    };

    @Override
    protected void onDestroy()
    {
        //移除监听服务
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled)
    {
        updateServiceStatus();
    }

    private void updateServiceStatus()
    {
        if (isServiceEnabled())
        {
            Log.i("TAG","service is on");
            Toast.makeText(getApplicationContext(), "抢红包神器已经开启", Toast.LENGTH_LONG).show();
            imagebtn.setImageResource(R.mipmap.bat_sel_money);
            isGrasping.setText(R.string.action_isGrasping);
            imageview_2.setVisibility(View.VISIBLE);
            shouldOpenServer_layout.setVisibility(View.INVISIBLE);
        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "抢红包神器已经关闭", Toast.LENGTH_LONG).show();
            imagebtn.setImageResource(R.mipmap.bat_nor_money);
            isGrasping.setText(R.string.action_clickToGradp);
            imageview_2.setVisibility(View.INVISIBLE);
            shouldOpenServer_layout.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener myClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            try
            {
//                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//                startActivity(accessibleIntent);
            } catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    };

    private boolean isServiceEnabled()
    {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices)
        {
            if (info.getId().equals(getPackageName() + "/.RobMoney"))
            {
                return true;
            }
        }
        return false;
    }

    private CompoundButton.OnCheckedChangeListener wechat_swtich_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
            {
                Log.i("TAG", "打开微信抢红包");
            }else
            {
                Log.i("TAG", "关闭微信抢红包");
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener qq_switch_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
            {
                Log.i("TAG", "打开QQ抢红包");
            }else
            {
                Log.i("TAG", "关闭QQ抢红包");
            }
        }
    };

    public void openSettings(View view)
    {
        try
        {
            Log.i("TAG", "打开了设置");
            Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(accessibleIntent);
        } catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
