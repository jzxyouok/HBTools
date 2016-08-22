package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import static junyou.com.hbtools.R.mipmap.bat_nor_money;

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
    TextView num_total ;
    TextView num_today ;
    TextView money_total;
    TextView money_today ;

    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
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
        updateServiceStatus();

        imgbtn_setting = (ImageButton) findViewById(R.id.imgbtn_settings);
        imgbtn_setting.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_setting.setImageResource(R.mipmap.icon_circularset);
        imgbtn_setting.setOnClickListener(onClickSetting);

        imgbtn_share = (ImageButton) findViewById(R.id.imgbtn_share);
        imgbtn_share.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_share.setImageResource(R.mipmap.icon_share);
        imgbtn_share.setOnClickListener(onClickShare);

        imgbtn_help = (ImageButton) findViewById(R.id.imgbtn_help);
        imgbtn_help.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_help.setImageResource(R.mipmap.icon_help);
        imgbtn_help.setOnClickListener(onClickHelp);

        //四个文本控件
         num_total = (TextView)findViewById(R.id.num_total);
//        num_total.setText("500");
         num_today = (TextView)findViewById(R.id.num_today);
         money_total = (TextView)findViewById(R.id.money_total);
         money_today = (TextView)findViewById(R.id.money_today);
        //RobMoney.getInstance().showData();
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
        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "抢红包神器已经关闭", Toast.LENGTH_LONG).show();
            imagebtn.setImageResource(R.mipmap.bat_nor_money);
            isGrasping.setText(R.string.action_clickToGradp);
            imageview_2.setVisibility(View.INVISIBLE);
        }
    }

    private View.OnClickListener myClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            try
            {
                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibleIntent);
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
}
