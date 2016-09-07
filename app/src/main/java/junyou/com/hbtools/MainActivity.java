package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.BoolRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.LoginFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AccessibilityManager.AccessibilityStateChangeListener
{
    private AccessibilityManager accessibilityManager;
    SharedPreferences sharedPreferences;
    RelativeLayout mainLayoutHeader;
    private static MainActivity instance;

    private static final String LEFT_DAYS_COUNT = "left_days_count";  //剩余的天数
    private static final String DATE_MARK = "date_mark";            //日期记录
    private static final int BORN_DAYS = 3;                         //初始天数

    private Switch openWechat_switch;
    private Switch openQQ_switch;

    //左上角两个个按钮
    private ImageButton setting_imagebtn;
    private ImageButton help_imagebtn;

    private RelativeLayout shouldOpenServer_layout;

    private ImageView top_image;

    private TextView wechat_auto_text;
    private TextView qq_auto_text;

    //红包个数 金额标签
    public TextView num_redpkt;
    public TextView num_money;

    //跑马灯文本
    private TextView marquee_text;

    //剩余天数
    public TextView left_days_text;

    //弹窗
    private Dialog dialog_openSvs;
    private Dialog dialog_openShare;
    private Dialog dialog_receiveTime;
    private Dialog dialog_tryDays;

    //广播消息
    private Intent bor_intent;

    //sdk 相关
    private IWXAPI wxAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        regToWx();      //注册微信id

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);

        mainLayoutHeader = (RelativeLayout) findViewById(R.id.layout_header);

        //-----------------------new items--------------------------//
        //开关
        openWechat_switch = (Switch) findViewById(R.id.open_wechat_switch);
        if (openWechat_switch != null){
            openWechat_switch.setOnCheckedChangeListener(wechat_swtich_listener);
        }
        openQQ_switch = (Switch) findViewById(R.id.open_qq_switch);
        if (openQQ_switch != null){
            openQQ_switch.setOnCheckedChangeListener(qq_switch_listener);
        }

        wechat_auto_text = (TextView)findViewById(R.id.wechat_auto);
        qq_auto_text = (TextView) findViewById(R.id.qq_auto);

        //设置和帮助按钮
        setting_imagebtn = (ImageButton) findViewById(R.id.imageButton_setting);
        if (setting_imagebtn != null){
            setting_imagebtn.setOnClickListener(onClickSetting);
        }
        help_imagebtn = (ImageButton) findViewById(R.id.imageButton_help);
        if (help_imagebtn != null){
            help_imagebtn.setOnClickListener(onClickHelp);
        }
        //顶部图片
        top_image = (ImageView) findViewById(R.id.top_img_show);

        //红包个数标签 金额标签
        num_redpkt = (TextView) findViewById(R.id.packt_num_text);
        num_money = (TextView) findViewById(R.id.money_num_text);

        //剩余天数标签
        left_days_text = (TextView) findViewById(R.id.left_days_text);

        //布局获取
        shouldOpenServer_layout = (RelativeLayout) findViewById(R.id.should_openServer);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //跑马灯文本
        marquee_text = (TextView) findViewById(R.id.marquee_text);

        //广播
       bor_intent = new Intent("junyou.com.hbtools.RECEIVER");

        updateServiceStatus();
        showDatas();
        refrishMarqueeText();
        showLeftDays();
        showDialog();
        showSwitchStatus();

    }
    //注册微信id
    private void regToWx()
    {
        wxAPI = WXAPIFactory.createWXAPI(this,Constants.APP_ID,true);
        wxAPI.registerApp(Constants.APP_ID);

    }

    private void showSwitchStatus()
    {
        SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
        boolean wechat_data = sharedP.getBoolean("wechat_switch",true);
        boolean qq_data = sharedP.getBoolean("qq_switch",true);

        if (wechat_data) {
            Log.i("TAG", "微信开");
            openWechat_switch.setChecked(true);
            wechat_auto_text.setText("自动抢   开启");
            wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));
        }else {
            Log.i("TAG", "微信关");
            openWechat_switch.setChecked(false);
            wechat_auto_text.setText("自动抢   关闭");
            wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
        }

        if (qq_data){
            Log.i("TAG", "QQ开");
            openQQ_switch.setChecked(true);
            qq_auto_text.setText("自动抢   开启");
            qq_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));
        }else{
            Log.i("TAG", "QQ关");
            openQQ_switch.setChecked(false);
            qq_auto_text.setText("自动抢   关闭");
            qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
        }
    }

    private void refrishMarqueeText()
    {
        final String []marquee_lists = {
                getResources().getString(R.string.marquee_word_1),
                getResources().getString(R.string.marquee_word_2),
                getResources().getString(R.string.marquee_word_3),
                getResources().getString(R.string.marquee_word_4),
                getResources().getString(R.string.marquee_word_5),
                getResources().getString(R.string.marquee_word_6),
                getResources().getString(R.string.marquee_word_7),
                getResources().getString(R.string.marquee_word_8),
                getResources().getString(R.string.marquee_word_9),
                getResources().getString(R.string.marquee_word_10),
                getResources().getString(R.string.marquee_word_11),
                getResources().getString(R.string.marquee_word_12)
        };
        //调度器
        Timer timer = new Timer();
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                    {
                        int num = (int)(Math.random()*12);  //0-11
                        if (null != marquee_text)
                        {
                            marquee_text.setText(marquee_lists[num]);
                        }
//                        Log.i("TAG",num + marquee_lists[num]);
                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };

        TimerTask task = new TimerTask(){
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 20000,20000);    //20秒之后执行，每20秒执行一次
    }

    private void showDialog()
    {
        //打开设置弹窗
        View view_1 = LayoutInflater.from(instance).inflate(R.layout.dialog_openservice, null);
        dialog_openSvs = new Dialog(this,R.style.common_dialog);
        dialog_openSvs.setContentView(view_1);
        //打开分享弹窗
        View view_2 = LayoutInflater.from(instance).inflate(R.layout.dialog_share,null);
        dialog_openShare = new Dialog(this,R.style.common_dialog);
        dialog_openShare.setContentView(view_2);

        //主页的获取更多天数弹窗
//        View view_3 = LayoutInflater.from(instance).inflate(R.layout.dialog_receivetime,null);
//        dialog_receiveTime = new Dialog(this,R.style.common_dialog);
//        dialog_receiveTime.setContentView(view_3);
//        dialog_receiveTime.show();

        //刚启动 赠送天数弹窗
        SharedPreferences sharedP = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedP.edit();
        int days = getSharedPreferences("config",MODE_PRIVATE).getInt("showTryDaysDialog",-1);
        if (days <0){
            View view_4 = LayoutInflater.from(instance).inflate(R.layout.dialog_trydays,null);
            dialog_tryDays = new Dialog(this,R.style.common_dialog);
            dialog_tryDays.setContentView(view_4);
            dialog_tryDays.show();
            editor.putInt("showTryDaysDialog",1);
            editor.commit();
        }
    }

    //右下角显示剩余的天数
    private void showLeftDays()
    {
        SharedPreferences sharedP = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedP.edit();

        //设置天数
        /**
         * 若为-99  设置为3天
         * 若不为-99  获得之前设置的天数
         */
        int days = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,-99);
        if (days == -99 )
        {
//            Log.i("TAG", "天数小于0。。。");
            editor.putInt(Constants.LEFT_DAYS_COUNT,BORN_DAYS);
            editor.commit();
            if (left_days_text != null)
            {
                int days_1 = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0);
                left_days_text.setText(String.valueOf(days_1) + " 天");
            }
        }else
        {
//            Log.i("TAG", ",天数有值。。。");
            if (left_days_text != null)
            {
                int days_2 = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,BORN_DAYS);
                left_days_text.setText(String.valueOf(days_2) + " 天");
            }
        }

        /**
         * 1. 第一次进入app,获取保存的时间，若为空，则保存现在的时间
         *     若不为空，拿当前的系统时间和保存的时间比较
         *     若相等，则是同一天，不相等，则是新的一天
         *  2.  若是新的一天  将剩余天数减 1  改掉UI显示
         *      若不是新的一天  不操作
         */

        //判断是否新的一天
        Calendar calendar = Calendar.getInstance();
        /*
        //for test
        String nowDate = calendar.get(Calendar.YEAR) + "年"
                + (calendar.get(Calendar.MONTH)+1) + "月"//从0计算
                + calendar.get(Calendar.DAY_OF_MONTH) + "日"
                + calendar.get(Calendar.HOUR_OF_DAY) + "时"
                + calendar.get(Calendar.MINUTE)+ "分"
                + calendar.get(Calendar.SECOND)+ "秒";
        */
        String nowDate = calendar.get(Calendar.YEAR) + "年"
                + (calendar.get(Calendar.MONTH)+1) + "月"//从0计算
                + calendar.get(Calendar.DAY_OF_MONTH) + "日";

        String defaultTime = getSharedPreferences("config",MODE_PRIVATE).getString(DATE_MARK,"empty");
        if ("empty".equals(defaultTime)){
            editor.putString(DATE_MARK,nowDate);
            editor.commit();
//            Log.i("TAG", "<<<第一次进来,日期为empty,我保存到了本地");
            editor.putBoolean(Constants.IS_SERVICE_ON,true);
            editor.apply();
            editor.putBoolean(Constants.IS_NEW_DAY,true);
            editor.apply();
        }else{
           String saveTime =  getSharedPreferences("config",MODE_PRIVATE).getString(DATE_MARK,"empty");
            if (nowDate.equals(saveTime)) {
                Log.i("TAG","<<<不是新的一天");
                editor.putBoolean(Constants.IS_SERVICE_ON,true);
                editor.apply();
//                editor.putBoolean(Constants.IS_NEW_DAY,false);
//                editor.apply();
            }else{
                Log.i("TAG","<<<是新的一天");
                editor.putBoolean(Constants.IS_NEW_DAY,true);
                editor.apply();

                int days_5 = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0) - 1;
                if (days_5 >=0){
                    editor.putInt(Constants.LEFT_DAYS_COUNT,days_5);
                    editor.apply();
//                    Log.i("TAG", "设置的天数" + getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0));
                    if (left_days_text != null)
                    {
                        left_days_text.setText(getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0) + " 天");
                    }
                    if (days_5 == 0){
                        editor.putBoolean(Constants.IS_SERVICE_ON,false);
                        editor.apply();
                    }else{
                        editor.putBoolean(Constants.IS_SERVICE_ON,true);
                        editor.apply();
                    }
                }else{
                    //没有天数了，需要一个弹窗提醒
                    Toast.makeText(getApplicationContext(), "亲，没有天数了，赶快去分享获得天数吧！", Toast.LENGTH_SHORT).show();
                    Log.i("TAG", "没有天数了");
                    editor.putBoolean(Constants.IS_SERVICE_ON,false);
                    editor.apply();
                }
            }
//            Log.i("TAG","保存的时间："+saveTime+"， 现在："+nowDate);
        }
    }

    private void showDatas()
    {
        SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
        Log.i("TAG", "初始总红包数量:"+ String.valueOf(sharedP.getInt("totalnum",0)));
        Log.i("TAG", "初始总资产:"+ sharedP.getString("totalmoney",""));
        //显示数据
        num_redpkt.setText(String.valueOf(sharedP.getInt("totalnum",0)));
        if ("".endsWith(sharedP.getString("totalmoney","")))
        {
            num_money.setText("0.00");
        }else
        {
            num_money.setText(sharedP.getString("totalmoney",""));
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
            Intent helpAvt = new Intent(MainActivity.this,helpActivity.class);
            startActivity(helpAvt);
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
            Toast.makeText(getApplicationContext(), "红包快手已经开启", Toast.LENGTH_SHORT).show();
            shouldOpenServer_layout.setVisibility(View.INVISIBLE);
            top_image.setImageResource(R.mipmap.top_img_radpacket_yes);

            openWechat_switch.setChecked(true);
            openQQ_switch.setChecked(true);
            if (mainLayoutHeader != null){
                mainLayoutHeader.setBackgroundColor(getResources().getColor(R.color.mainbgOn));
            }
        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "红包快手已经关闭", Toast.LENGTH_SHORT).show();
            shouldOpenServer_layout.setVisibility(View.VISIBLE);
            top_image.setImageResource(R.mipmap.top_img_radpacket_on);

            openWechat_switch.setChecked(false);
            openQQ_switch.setChecked(false);
            if (mainLayoutHeader != null){
                mainLayoutHeader.setBackgroundColor(getResources().getColor(R.color.mainbgOff));
            }
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
                Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_SHORT).show();
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
            if (isServiceEnabled())
            {
                //服务已经开启
                if (isChecked)
                {
                    //打开开关
                    //发送广播
                    bor_intent.putExtra("wechat_broadcast", true);
                    sendBroadcast(bor_intent);

                    wechat_auto_text.setText("自动抢   开启");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));
                    //  存数据
                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("wechat_switch",true);
                    editor.commit();

                    if (sharedP.getBoolean("wechat_switch",true))
                    {
                        Log.i("TAG", "手动设置了微信开");
                    }else
                    {
                        Log.i("TAG", "不能手动设置微信开");
                    }

                }else
                {
                    //关闭开关
                    bor_intent.putExtra("wechat_broadcast", false);
                    sendBroadcast(bor_intent);

                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("wechat_switch",false);
                    editor.commit();

                    if (!sharedP.getBoolean("wechat_switch",true))
                    {
                        Log.i("TAG", "手动设置了微信关");
                    }else
                    {
                        Log.i("TAG", "不能手动设置微信关");
                    }
                }
            }else
            {
                //服务已经关闭
                if (isChecked)
                {
                    //未开启服务 弹出提示，再进入设置
                    if (null != dialog_openSvs)
                    {
                        dialog_openSvs.show();
                    }
                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("wechat_switch",true);
                    editor.commit();

                }else
                {
                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("wechat_switch",false);
                    editor.commit();
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener qq_switch_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isServiceEnabled())
            {
                if (isChecked)
                {
                    bor_intent.putExtra("qq_broadcast", true);
                    sendBroadcast(bor_intent);

                    qq_auto_text.setText("自动抢   开启");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("qq_switch",true);
                    editor.commit();

                    if (sharedP.getBoolean("qq_switch",true))
                    {
                        Log.i("TAG", "手动设置了qq开");
                    }else
                    {
                        Log.i("TAG", "不能手动设置qq开");
                    }

                }else
                {
                    bor_intent.putExtra("qq_broadcast", false);
                    sendBroadcast(bor_intent);

                    qq_auto_text.setText("自动抢   关闭");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("qq_switch",false);
                    editor.commit();

                    if (!sharedP.getBoolean("qq_switch",true))
                    {
                        Log.i("TAG", "手动设置了qq关");
                    }else
                    {
                        Log.i("TAG", "不能手动设置qq关");
                    }
                }
            }else
            {
                if (isChecked)
                {
                    //未开启服务 弹出提示，再进入设置
                    if (null != dialog_openSvs)
                    {
                        dialog_openSvs.show();
                    }
                    qq_auto_text.setText("自动抢   关闭");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("qq_switch",true);
                    editor.commit();

                }else
                {
                    qq_auto_text.setText("自动抢   关闭");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));

                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedP.edit();
                    editor.putBoolean("qq_switch",false);
                    editor.commit();
                }
            }
        }
    };

    public void openSettings(View view)
    {
        if (!isServiceEnabled())
        {
            if (null != dialog_openSvs)
            {
                dialog_openSvs.show();
            }
            //未开启服务 弹出提示，再进入设置
            /*
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("必须打开辅助功能->红包快手->开启服务，才能抢红包哦.")
                    .setPositiveButton("去打开辅助功能", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                    })
                    .setNegativeButton("取消",null)
                    .show();
*/
        }
    }

    //右下角获取更多天数按钮
    public void getMoreTime(View view)
    {
        Log.i("TAG", "点我获取天数哦");
        if (null != dialog_openShare)
            dialog_openShare.show();
    }

    public void openServiceClick(View view)
    {
        Log.i("TAG", "点击打开系统设置");
        try
        {
            Log.i("TAG", "打开了设置");
            Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(accessibleIntent);
        } catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (null != dialog_openSvs)
        {
            dialog_openSvs.dismiss();
        }
    }

    public void closeOpenServiceClick(View view)
    {
        Log.i("TAG", "点击关闭系统设置提示");
        if (null != dialog_openSvs)
        {
            dialog_openSvs.dismiss();
        }
    }

    public void closeOpenShare(View view)
    {
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }

    public void sharePengYouQuanClick(View view)
    {
        Log.i("TAG", "点击分享到朋友圈");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }

        //不使用sdk分享

        final String PackageName = "com.tencent.mm";
        final String ActivityName = "com.tencent.mm.ui.tools.ShareToTimeLineUI"; //微信朋友圈
        if (ShareHelper.isInstalled(this,PackageName,ActivityName)){
            //图片加文字
            /*
            Bitmap bt= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
            final Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bt, null,null));
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(PackageName, ActivityName);//带图片分享
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra("Kdescription", "红包快手，让红包来的容易点~~");
            startActivity(intent);
            */
            //使用sdk分享
            WXWebpageObject webpage = new WXWebpageObject();
//            webpage.webpageUrl = "http://www.zjhzjykj.com/images/hbks.apk";     //网址替换掉就可以了
            webpage.webpageUrl = "http://www.zjhzjykj.com";     //网址替换掉就可以了

            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = "红包快手";
            msg.description = "红包快手，让红包来得容易点~";
            Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            msg.thumbData = Util.bmpToByteArray(thumb, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = ShareHelper.buildTransaction("webpage");
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneTimeline;    //朋友圈
            wxAPI.sendReq(req);
        }else {
            Toast.makeText(getApplicationContext(), "您没有安装微信", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareWeiXinClick(View view)
    {
        Log.i("TAG", "点击分享给微信朋友");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }

        final String PackageName = "com.tencent.mm";
        final String ActivityName = "com.tencent.mm.ui.tools.ShareImgUI";
        if (ShareHelper.isInstalled(this,PackageName,ActivityName)){

            //文字或链接
            /*
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(PackageName, ActivityName);
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~~");
            startActivity(intent);
               */
            //图片
            /*
            Bitmap bt= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
            final Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bt, null,null));
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(PackageName, ActivityName);
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");        //分享图片，没有图片用转回到分享文字
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            startActivity(intent);
            */
            //使用sdk分享
            WXWebpageObject webpage = new WXWebpageObject();
//            webpage.webpageUrl = "http://www.zjhzjykj.com/images/hbks.apk";
            webpage.webpageUrl = "http://www.zjhzjykj.com";
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = "红包快手";
            msg.description = "红包快手，让红包来得容易点~";
            Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            msg.thumbData = Util.bmpToByteArray(thumb, true);

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = ShareHelper.buildTransaction("webpage");
            req.message = msg;
            req.scene = SendMessageToWX.Req.WXSceneSession;     //好友
            wxAPI.sendReq(req);
        }else {
            Toast.makeText(getApplicationContext(), "您没有安装微信", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareQQClick(View view)
    {
        Log.i("TAG", "点击分享给QQ好友");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
          final String PackageName = "com.tencent.mobileqq";
          final String ActivityName = "com.tencent.mobileqq.activity.JumpActivity"; //qq好友
         if (ShareHelper.isInstalled(this,PackageName,ActivityName)){
             //分享文字给好友
             Intent intent = new Intent(Intent.ACTION_SEND);
             ComponentName component = new ComponentName(PackageName,ActivityName);
             intent.setComponent(component);
             intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~");
             intent.setType("text/plain");
             startActivity(intent);
         }else {
             Toast.makeText(getApplicationContext(), "您没有安装手机QQ", Toast.LENGTH_SHORT).show();
         }
        //todo  分享到qq空间
    }

    public void shareWeiboClick(View view)
    {
        Log.i("TAG", "点击分享到微博");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }

        if (isSinaWiBoAvilible(this))
        {
            //分享文字
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String pakName = "com.sina.weibo";
            intent.setPackage(pakName);
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~");
            this.startActivity(Intent.createChooser(intent, ""));

            /*
            //图片加文字
            Bitmap bt= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
            final Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bt, null,null));
            Intent intent = new Intent();
            intent.setPackage("com.sina.weibo");
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~");
            startActivity(intent);
            */
        }else
        {
            Toast.makeText(getApplicationContext(), "您没有安装新浪微博", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSinaWiBoAvilible(Context context)
    {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++)
            {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.sina.weibo"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void closeReceiveTime(View view)
    {
        Log.i("TAG", "关闭收到天数啦");
        if (dialog_receiveTime != null){
            dialog_receiveTime.dismiss();
        }
    }

    public void receive_confirm_click(View view)
    {
        Log.i("TAG", "确定收到天数");
        if (dialog_receiveTime != null){
            dialog_receiveTime.dismiss();
        }
    }

    public void receive_getmore_click(View view)
    {
        Log.i("TAG", "成为超级VIP");
        if (dialog_receiveTime != null){
            dialog_receiveTime.dismiss();
        }
    }

    public void try_days_click(View view){
        if (dialog_tryDays != null){
            dialog_tryDays.dismiss();
        }
    }

    public void closeTryDays(View view){
        if (dialog_tryDays != null){
            dialog_tryDays.dismiss();
        }
    }
}
