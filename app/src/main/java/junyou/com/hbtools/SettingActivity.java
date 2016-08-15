package junyou.com.hbtools;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import static junyou.com.hbtools.R.color.colorBG;

public class SettingActivity extends AppCompatActivity
{
    private ListView listView;
    private final String[] strs = new String[]{
            "微信红包自动抢", "QQ红包自动抢", "通用设置", "防踢设置", "优化设置","关于"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar =getSupportActionBar();
        if (null !=actionbar)
        {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.listview);
        listView.setFastScrollEnabled(true);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strs));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        int group_1 = 1;
//        int group_2 = 2;
//        int group_3 = 3;
//
//        menu.add(group_1,1,1,"微信红包自动抢");
//        menu.add(group_1,2,2,"QQ红包自动抢");
//
//        menu.add(group_2,3,3,"通用设置");
//        menu.add(group_2,4,4,"防踢设置");
//        menu.add(group_2,5,5,"优化设置");
//
//        menu.add(group_3,6,6,"关于");
//
//        return super.onCreateOptionsMenu(menu);
//    }
}
