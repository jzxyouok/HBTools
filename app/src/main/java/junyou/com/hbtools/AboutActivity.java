package junyou.com.hbtools;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import junyou.com.hbtools.fragments.AboutFragment;

public class AboutActivity extends FragmentActivity {

    static private AboutActivity instance;
    private Dialog dialog_openShare;
    private TextView about_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        prepareSettings();
        instance = this;
        about_text = (TextView) findViewById(R.id.about_days_text);
        //弹窗
        View view_2 = LayoutInflater.from(this).inflate(R.layout.dialog_share,null);
        dialog_openShare = new Dialog(this,R.style.common_dialog);
        dialog_openShare.setContentView(view_2);
    }

    private void prepareSettings()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.preferences_fragment, new AboutFragment());
        fragmentTransaction.commit();
    }

    public void about_performBack(View view)
    {
        super.onBackPressed();
    }

    public static AboutActivity getInstance()
    {
        return instance;
    }

    public void about_getMoreTime(View view)
    {
        Log.i("TAG", "获得更多时间");
        if (null != about_text)
        {
//            about_text.setText("111天");
        }
        if (null != dialog_openShare)
        {
            dialog_openShare.show();
        }
    }

    public void closeOpenShare(View view)
    {
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }

    public void sharePengYouQuanClick(View view)
    {
        Log.i("TAG", "点击分享到朋友圈");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }
    public void shareWeiXinClick(View view)
    {
        Log.i("TAG", "点击分享到微信");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }
    public void shareQQClick(View view)
    {
        Log.i("TAG", "点击分享到QQ");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }
    public void shareWeiboClick(View view)
    {
        Log.i("TAG", "点击分享到微博");
        if (null != dialog_openShare)
        {
            dialog_openShare.dismiss();
        }
    }
}
