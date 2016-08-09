package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AccessibilityManager.AccessibilityStateChangeListener
{

    //开关切换按钮
    private Button switchPlugin;

    private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //去掉标题
        setContentView(R.layout.activity_main);

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
        switchPlugin = (Button) findViewById(R.id.button_accessible);
        switchPlugin.setOnClickListener(myClickListener);
        updateServiceStatus();


    }

    @Override
    protected void onDestroy()
    {
        //移除监听服务
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled)
    {
        updateServiceStatus();
    }

    private void updateServiceStatus()
    {
        if (isServiceEnabled())
        {
            Log.i("TAG","service is on");
            Toast.makeText(getApplicationContext(), "抢红包神器已经开启", Toast.LENGTH_LONG).show();
            switchPlugin.setText(R.string.service_off);
        } else
        {
            Log.i("TAG","service is off");
            Toast.makeText(getApplicationContext(), "抢红包神器已经关闭", Toast.LENGTH_LONG).show();
            switchPlugin.setText(R.string.service_on);
        }
    }

    private View.OnClickListener myClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            try
            {
                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibleIntent);
            } catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "遇到一些问题,请手动打开系统设置>辅助服务>微信红包助手", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    };

    private boolean isServiceEnabled()
    {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices)
        {
            if (info.getId().equals(getPackageName() + "/.RobMoney"))
            {
                return true;
            }
        }
        return false;
    }
}
