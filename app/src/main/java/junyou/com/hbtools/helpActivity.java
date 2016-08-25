package junyou.com.hbtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class helpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    public void performBack(View view)
    {
        super.onBackPressed();
    }
}
