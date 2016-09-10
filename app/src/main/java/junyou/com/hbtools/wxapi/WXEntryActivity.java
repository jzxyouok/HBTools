package junyou.com.hbtools.wxapi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import junyou.com.hbtools.Constants;
import junyou.com.hbtools.MainActivity;
import junyou.com.hbtools.R;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

//
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        // 将该app注册到微信
        api.registerApp(Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) baseReq);
                break;
            default:
                break;
        }
    }
    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp baseResp) {
        String result = null;
        SharedPreferences sharedP = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedP.edit();

        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:		//发送成功
                result = "分享成功";
                Log.i("TAG", "分享成功");
                    //增加天数
                if (getSharedPreferences("config",MODE_PRIVATE).getBoolean(Constants.IS_NEW_DAY,true))
                {
                    Log.i("TAG","新的一天");
                    int use_day = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.USE_DAY,1);
                    if (use_day <= 10){
                        int days = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0);
                        editor.putInt(Constants.LEFT_DAYS_COUNT,days + 1);
                        editor.apply();
                        editor.putBoolean(Constants.IS_NEW_DAY,false);
                        editor.apply();

                        try{
                            int days_1 = getSharedPreferences("config",MODE_PRIVATE).getInt(Constants.LEFT_DAYS_COUNT,0);
                            MainActivity.getInstance().left_days_text.setText(String.valueOf(days_1) + " 天");

                            editor.putBoolean(Constants.IS_SERVICE_ON,true);
                            editor.apply();

                            MainActivity.getInstance().dialog_receiveTime.show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        //超过十天，不能通过分享获得天数
                        Toast.makeText(getApplicationContext(), "您免费使用超过了十天,不能获得获得分享天数了！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL: //发送取消
                result = "分享取消";
                Log.i("TAG", "分享取消");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED: //发送延迟
                result = "分享被拒绝";
                Log.i("TAG", "分享被拒绝");
                break;
            default:
                result = "分享失败";	           //未知
                Log.i("TAG", "分享失败");
                break;
        }
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        finish();
    }
}
