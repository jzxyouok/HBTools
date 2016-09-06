package junyou.com.hbtools;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import junyou.com.hbtools.fragments.SettingFragment;

public class SettingActivity extends FragmentActivity
{
    private Dialog dialog_open_vip;
    private static SettingActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        ActionBar actionbar =getSupportActionBar();
//        if (null !=actionbar)
//        {
//            actionbar.setDisplayHomeAsUpEnabled(true);
//        }
        loadUI();
        prepareSettings();
        instance = this;
    }

    public static SettingActivity getInstance()
    {
        return instance;
    }

    private void prepareSettings()
    {

        String title, fragId;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString("title");
            fragId = bundle.getString("frag_id");
        } else {
            title = "设置";
            fragId = "GeneralSettingsFragment";
        }

        TextView textView = (TextView) findViewById(R.id.settings_bar);
        textView.setText(title);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if ("GeneralSettingsFragment".equals(fragId))
        {
            fragmentTransaction.replace(R.id.preferences_fragment, new SettingFragment());
        }
        fragmentTransaction.commit();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        switch (item.getItemId())
//        {
//            case android.R.id.home:
//                finish();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void performBack(View view)
    {
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadUI()
    {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(0xffE46C62);
    }

    public void superVipClick(View view)
    {
        Log.i("TAG", "点击弹出超级VIP弹窗");
        View view_1 = LayoutInflater.from(SettingActivity.this).inflate(R.layout.dialog_supervip, null);
        dialog_open_vip = new Dialog(this,R.style.common_dialog);
        dialog_open_vip.setContentView(view_1);
        dialog_open_vip.show();
    }

    public void super_vip_click(View view)
    {
        dialog_open_vip.dismiss();
        Log.i("TAG", "点击获取超级VIP");
    }

    public void closeOpenSuperVip(View view)
    {
        dialog_open_vip.dismiss();
    }

    //SettingFragment中的弹窗方法
    public void closeDownloadClick(View view)
    {
        Log.i("TAG", "关闭弹窗");
        SettingFragment.getInstance().dialog_setting_share.dismiss();
    }
    public void opendownloadClick(View view)
    {
        Log.i("TAG", "打开下载骏游连连看");
        /*
        //打开网页才能下载
        Intent webViewIntent = new Intent(this, WebViewActivity.class);
        webViewIntent.putExtra("title", "骏游科技");
//        webViewIntent.putExtra("url", "http://www.zjhzjykj.com");
        webViewIntent.putExtra("url", "http://www.zjhzjykj.com/game/ShowClass.asp?ClassID=2");
        startActivity(webViewIntent);
        SettingFragment.getInstance().dialog_setting_share.dismiss();
        */
        //点击按钮直接下载
        (new DownloadUtil()).enqueue("http://www.zjhzjykj.com/images/tgllx-daiji_3009-2.3.0-201605191729.apk", getApplicationContext());
        SettingFragment.getInstance().dialog_setting_share.dismiss();

        //点击直接增加天数
        int days = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0);
        SharedPreferences sharedP = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedP.edit();
        //设置天数
        /*
        editor.putInt(Constants.LEFT_DAYS_COUNT,days + 1);
        editor.commit();
        Toast.makeText(getApplicationContext(), "开始下载，又可以再使用一天了哦~", Toast.LENGTH_SHORT).show();
        try{
            int days_1 = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0);
            MainActivity.getInstance().left_days_text.setText(String.valueOf(days_1) + " 天");
            //增加天数的时候一定要这句话，不然没用
            editor.putBoolean(Constants.IS_SERVICE_ON,true);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
        */
    }

    //打开APK
    /*
    private void openFile(File file) {
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }
    */
    public class DownloadUtil {
        public void enqueue(String url, Context context) {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "tgllx-daiji_3009-2.3.0-201605191729.apk");
            r.allowScanningByMediaScanner();
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(r);
        }
    }
}
