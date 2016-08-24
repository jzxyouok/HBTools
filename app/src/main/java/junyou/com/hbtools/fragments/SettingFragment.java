package junyou.com.hbtools.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import junyou.com.hbtools.R;

public class SettingFragment extends PreferenceFragment
{
    public SettingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_preferences);
        setPrefListeners();
    }

    private void setPrefListeners() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    }
