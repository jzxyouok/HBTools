package junyou.com.hbtools;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import junyou.com.hbtools.fragments.SettingFragment;

public class SettingActivity extends FragmentActivity
{

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
        } else if ("CommentSettingsFragment".equals(fragId)) {
//            fragmentTransaction.replace(R.id.preferences_fragment, new CommentSettingsFragment());
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

}
