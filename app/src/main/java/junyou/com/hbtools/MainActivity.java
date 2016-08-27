package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

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
    private RelativeLayout shouldOpenServer_layout;

    private TextView wechat_auto_text;
    private TextView qq_auto_text;

    private ImageView top_image;

    //红包个数 金额标签
    public TextView num_redpkt;
    public TextView num_money;

    //跑马灯文本
    private TextView marquee_text;

    //剩余天数
    private TextView left_days_text;

    //弹窗
    Dialog dialog_openSvs;
    Dialog dialog_openShare;
    Dialog dialog_receiveTime;

    private static MainActivity instance;
    SharedPreferences sharedPreferences;

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

        imgbtn_setting = (ImageButton) findViewById(R.id.imgbtn_settings);
        imgbtn_setting.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_setting.setImageResource(R.mipmap.icon_circularset);
//        imgbtn_setting.setOnClickListener(onClickSetting);

        imgbtn_share = (ImageButton) findViewById(R.id.imgbtn_share);
        imgbtn_share.setBackgroundColor(Color.TRANSPARENT);
        imgbtn_share.setImageResource(R.mipmap.icon_share);
//        imgbtn_share.setOnClickListener(onClickShare);

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

        wechat_auto_text = (TextView)findViewById(R.id.wechat_auto);
        qq_auto_text = (TextView) findViewById(R.id.qq_auto);

        //设置和帮助按钮
        setting_imagebtn = (ImageButton) findViewById(R.id.imageButton_setting);
        setting_imagebtn.setOnClickListener(onClickSetting);
        help_imagebtn = (ImageButton) findViewById(R.id.imageButton_help);
        help_imagebtn.setOnClickListener(onClickHelp);
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
        updateServiceStatus();
        showDatas();
        refrishMarqueeText();
//        showLeftDays();
        //获取设置中开关的状态
//        Boolean watchOnLockFlag = sharedPreferences.getBoolean("pref_watch_notification", false);
//        if (watchOnLockFlag)
//        {
//            Log.i("TAG", "trueeeeee");
//        } else
//        {
//            Log.i("TAG", "falseeeeeee");
//        }
        showDialog();

    }

    private void refrishMarqueeText()
    {
        final String []marquee_lists = {
                getResources().getString(R.string.marquee_word_1),
                getResources().getString(R.string.marquee_word_2),
                getResources().getString(R.string.marquee_word_3),
                getResources().getString(R.string.marquee_word_4),
                getResources().getString(R.string.marquee_word_5)
        };
        //调度器
        Timer timer = new Timer();
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                    {
                        int num = (int)(Math.random()*5);  //0-4
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
        timer.schedule(task, 1000,8000);    //1秒之后执行，每5秒执行一次
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
        //设置里的下载弹窗
//        View view_3 = LayoutInflater.from(instance).inflate(R.layout.dialog_settingshare,null);
//        Dialog dialog_settingShare = new Dialog(this,R.style.common_dialog);
//        dialog_settingShare.setContentView(view_3);
//        dialog_settingShare.show();
    }
    //时间显示有问题，TODO
    private void showLeftDays()
    {
        SharedPreferences sharedP = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedP.edit();
        //设置天数
        /*
        editor.putInt("left_days_count",15);
        editor.commit();

        if (left_days_text != null)
        {
            int days = getSharedPreferences("config",MODE_PRIVATE).getInt("left_days_count",0);
            left_days_text.setText(String.valueOf(days) + " 天");
        }
*/

        Calendar calendar = Calendar.getInstance();
        String nowDate = calendar.get(Calendar.YEAR) + "年"
                + (calendar.get(Calendar.MONTH)+1) + "月"//从0计算
                + calendar.get(Calendar.DAY_OF_MONTH) + "日";

        if (!nowDate.equals(getSharedPreferences("config",MODE_PRIVATE).getString("date_mark","")))
        {
            //日期不相等
            editor.putInt("left_days_count",getSharedPreferences("config",MODE_PRIVATE).getInt("left_days_count",0) - 1);
            editor.commit();

            if (left_days_text != null)
            {
                int days = getSharedPreferences("config",MODE_PRIVATE).getInt("left_days_count",0);
                left_days_text.setText(String.valueOf(days) + " 天");
            }
        }else
        {
            //日期相等  保存今天的日期信息
            editor.putString("date_mark",nowDate);
            editor.commit();
        }
        Log.e("TAG", nowDate);
        Log.i("TAG", "存储日期:" + getSharedPreferences("config",MODE_PRIVATE).getString("date_mark",""));
    }
    private void showDatas()
    {
        SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
        Log.i("TAG", "初始总红包数量:"+ String.valueOf(sharedP.getInt("totalnum",0)));
        Log.i("TAG", "初始总资产:"+ sharedP.getString("totalmoney",""));
        //显示数据
        /*
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
*/
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
            Toast.makeText(getApplicationContext(), "抢红包神器已经开启", Toast.LENGTH_LONG).show();
            imagebtn.setImageResource(R.mipmap.bat_sel_money);
            isGrasping.setText(R.string.action_isGrasping);
            imageview_2.setVisibility(View.VISIBLE);
            shouldOpenServer_layout.setVisibility(View.INVISIBLE);
            top_image.setImageResource(R.mipmap.top_img_radpacket_yes);

            openWechat_switch.setChecked(true);
            openQQ_switch.setChecked(true);

        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "抢红包神器已经关闭", Toast.LENGTH_LONG).show();
            imagebtn.setImageResource(R.mipmap.bat_nor_money);
            isGrasping.setText(R.string.action_clickToGradp);
            imageview_2.setVisibility(View.INVISIBLE);
            shouldOpenServer_layout.setVisibility(View.VISIBLE);
            top_image.setImageResource(R.mipmap.top_img_radpacket_on);

            openWechat_switch.setChecked(false);
            openQQ_switch.setChecked(false);
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
            if (isServiceEnabled())
            {
                if (isChecked)
                {
                    wechat_auto_text.setText("自动抢");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));
                    sharedPreferences.edit().putBoolean("wechat_switch",true);
                }else
                {
                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
                    sharedPreferences.edit().putBoolean("wechat_switch",false);
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
                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
                    sharedPreferences.edit().putBoolean("wechat_switch",false);
                }else
                {
                    wechat_auto_text.setText("自动抢   关闭");
                    wechat_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
                    sharedPreferences.edit().putBoolean("wechat_switch",false);
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
                    qq_auto_text.setText("自动抢");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextyellow));
                    sharedPreferences.edit().putBoolean("qq_switch",true);
                }else
                {
                    qq_auto_text.setText("自动抢   关闭");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
                    sharedPreferences.edit().putBoolean("qq_switch",false);
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
                    sharedPreferences.edit().putBoolean("qq_switch",false);
                }else
                {
                    qq_auto_text.setText("自动抢   关闭");
                    qq_auto_text.setTextColor(getResources().getColor(R.color.colortextblue));
                    sharedPreferences.edit().putBoolean("qq_switch",false);
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
    //左下角获取更多天数按钮
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
            Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_LONG).show();
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
    }
    public void shareWeiXinClick(View view)
    {
        Log.i("TAG", "点击分享到微信");
    }
    public void shareQQClick(View view)
    {
        Log.i("TAG", "点击分享到QQ");
    }
    public void shareWeiboClick(View view)
    {
        Log.i("TAG", "点击分享到微博");
    }
}
