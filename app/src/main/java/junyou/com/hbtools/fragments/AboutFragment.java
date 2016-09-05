package junyou.com.hbtools.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import junyou.com.hbtools.AboutActivity;
import junyou.com.hbtools.R;
import junyou.com.hbtools.SettingActivity;

public class AboutFragment extends PreferenceFragment
{
    public AboutFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preference);

        //点赞功能
        Preference click_dianzan = findPreference("pref_dianzan");
        click_dianzan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Log.i("TAG", "点赞~");
                return false;
            }
        });

        //跟新功能
        Preference click_update = findPreference("pref_update");
        click_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(AboutActivity.getInstance(), "已经是最新版本~", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
