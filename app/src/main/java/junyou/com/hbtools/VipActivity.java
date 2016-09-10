package junyou.com.hbtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class VipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
    }

    public void performBack(View view)
    {
        super.onBackPressed();
    }

    public void vip_one_month(View view)
    {
        Log.i("TAG", "购买一个月");
    }

    public void vip_three_month(View view)
    {
        Log.i("TAG", "购买三个月");
    }

    public void vip_all_life(View view)
    {
        Log.i("TAG", "购买终身使用");
    }
}
