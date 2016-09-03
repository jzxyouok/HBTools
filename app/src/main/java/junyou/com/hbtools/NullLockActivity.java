package junyou.com.hbtools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class NullLockActivity extends Activity {

    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, R.layout.activity_null_lock);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_null_lock);

        Window window = getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        View view = new View(this);
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    finish();
                    overridePendingTransition(0, 0);
                }
                return false;
            }
        });

        setContentView(view);
        mIntent = getIntent();
        init();
//        Log.i("TAG", "null activity --- onCreate");
        if (RobMoney.getInstance() != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    RobMoney.getInstance().openNotifyByNullActivity();
                }
            }, 0);
        }
    }

    private void init() {
        if (mIntent.getIntExtra("startType", 100) == 0)
        {
            finish();
            overridePendingTransition(0, 0);
        }
//        else if (mIntent.getIntExtra("startType",0) == 1)
//        {
//            finish();
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
        Log.i("TAG", "null activity --- onNewIntent");
        init();
    }
}
