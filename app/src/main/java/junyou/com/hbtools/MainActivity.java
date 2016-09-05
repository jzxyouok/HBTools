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

    private static MainActivity instance;

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
    private TextView left_days_text;

    //弹窗
    private Dialog dialog_openSvs;
    private Dialog dialog_openShare;
    private Dialog dialog_receiveTime;

    //广播消息
    private Intent bor_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
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
//        showLeftDays();
        showDialog();
        showSwitchStatus();
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
        timer.schedule(task, 20000,20000);    //10秒之后执行，每10秒执行一次
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

        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "红包快手已经关闭", Toast.LENGTH_SHORT).show();
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
        if (isWeixinAvilible(this))
        {
            Bitmap bt= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
            final Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bt, null,null));
            Intent intent = new Intent();
            ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");

            intent.setComponent(comp);
            intent.setAction("android.intent.action.SEND");
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra("Kdescription", "红包快手，让红包来的容易点~");
            startActivity(intent);
        }else
        {
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
        if (isWeixinAvilible(this))
        {
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            intent.setType("text/plain");                   // 分享发送的数据类型
            String pakName = "com.tencent.mm";              //微信
            intent.setPackage(pakName);
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~"); // 分享的内容
            this.startActivity(Intent.createChooser(intent, ""));// 目标应用选择对话框的标题;
        }else
        {
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
        if (isQQAvilible(this))
        {
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            intent.setType("text/plain");                   // 分享发送的数据类型
//        String pakName = "com.qzone";                   //qqzone
            String pakName = "com.tencent.mobileqq";      //qq
            intent.setPackage(pakName);
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~"); // 分享的内容
            this.startActivity(Intent.createChooser(intent, ""));// 目标应用选择对话框的标题;
        }else
        {
            Toast.makeText(getApplicationContext(), "您没有安装手机QQ", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareWeiboClick(View view)
    {
        Log.i("TAG", "点击分享到微博");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
        if (isSinaWeBoAvilible(this))
        {
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            intent.setType("text/plain");                   // 分享发送的数据类型
            String pakName = "com.sina.weibo";              //微博
            intent.setPackage(pakName);
            intent.putExtra(Intent.EXTRA_TEXT, "红包快手，让红包来的容易点~"); // 分享的内容
            this.startActivity(Intent.createChooser(intent, ""));// 目标应用选择对话框的标题;
        }else
        {
            Toast.makeText(getApplicationContext(), "您没有安装新浪微博", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isWeixinAvilible(Context context)
    {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++)
            {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isQQAvilible(Context context)
    {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++)
            {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSinaWeBoAvilible(Context context)
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
}
