package junyou.com.hbtools;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

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
        Intent webViewIntent = new Intent(this, WebViewActivity.class);
        webViewIntent.putExtra("title", "骏游科技");
//        webViewIntent.putExtra("url", "http://www.zjhzjykj.com");
        webViewIntent.putExtra("url", "http://www.zjhzjykj.com/game/ShowClass.asp?ClassID=2");
        startActivity(webViewIntent);
        SettingFragment.getInstance().dialog_setting_share.dismiss();
    }
}
