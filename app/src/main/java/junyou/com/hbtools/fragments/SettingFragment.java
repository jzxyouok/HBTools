package junyou.com.hbtools.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import junyou.com.hbtools.AboutActivity;
import junyou.com.hbtools.MainActivity;
import junyou.com.hbtools.R;
import junyou.com.hbtools.SettingActivity;

//PreferenceFragment
public class SettingFragment extends PreferenceFragment
{
    private CheckBoxPreference settingShare_Preference;

    public Dialog dialog_setting_share;
    private static SettingFragment instance;

    public SettingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_preferences);
        setPrefListeners();
        instance = this;
        settingShare_Preference = (CheckBoxPreference) findPreference("pref_no_ad");
        //单击事件
        settingShare_Preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                Log.i("TAG", "点击立即去广告");
                View view_1 = LayoutInflater.from(SettingActivity.getInstance()).inflate(R.layout.dialog_settingshare, null);
                dialog_setting_share = new Dialog(SettingActivity.getInstance(),R.style.common_dialog);
                dialog_setting_share.setContentView(view_1);
                dialog_setting_share.show();
                //下载骏游连连看后设置为true
                settingShare_Preference.setChecked(false);
                return false;
            }
        });

        //开关按钮事件
        /*
        settingShare_Preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                return false;
            }
        });
        */

        //点击打开关于页面
        Preference prefAbout = findPreference("pref_etc_about");
        prefAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent aboutAvt = new Intent(getActivity(),AboutActivity.class);
                startActivity(aboutAvt);
                return false;
            }
        });

        //点击红包权限设置 打开系统设置
        Preference prefSetting = findPreference("pref_etc_limit");
        prefSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibleIntent);
                return false;
            }
        });
    }

    public static SettingFragment getInstance()
    {
        return instance;
    }

    private void setPrefListeners() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }
    //放在SettingActivity中去实现
    /*
    public void closeDownloadClick(View view)
    {
        Log.i("TAG", "关闭弹窗");
        dialog_setting_share.dismiss();
    }
    public void opendownloadClick(View view)
    {
        Log.i("TAG", "打开下载链接");
    }
    */
}
