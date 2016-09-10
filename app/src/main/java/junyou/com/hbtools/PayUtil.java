package junyou.com.hbtools;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayUtil {

    /**

     收到的钱，            money_count       浮点类型（float）  ，计算事件
     计费请求次数，        purchase_num    整形(int)    ， 计数事件
     抢红包的次数，        grasp_num           整形(int)     ，计数事件
     imei，               imei_num       （移动设备识别码，识别用户的手机）  *#06#
     imis，               imsi_num       （国际移动用户识别码，识别用户的手机卡）
     机型，                phone_type      计算事件

     分渠道的id，   （友盟自带）
     用的天数，     （友盟自带）

     */
    //抢红包次数
    public static void YMgrasp_num(Context context)
    {
        MobclickAgent.onEvent(context,"grasp_num");
    }
        //计费请求次数
    public static void YMpurchase_num(Context context)
    {
        MobclickAgent.onEvent(context,"purchase_num");
    }

    //收到的钱
    public static void YMmoney_count(Context context,int payid)
    {
        int payType  = 0; //付费类型类型
        Map<String, String> map_value = new HashMap<String, String>();

        switch (payid){
            case 0:
                //包月 6.66元
                map_value.put("oneMonth" , "6.66" );
                payType = 0;
                break;
            case 1:
                //一季度(三个月) 10.00元
                map_value.put("threeMonth" , "10.00" );
                payType = 1;
                break;
            case 2:
                //终身使用 18.00元
                map_value.put("allLife" , "18.00" );
                payType = 2;
                break;
        }
        MobclickAgent.onEventValue(context, "money_count" , map_value, payType);
    }

    //手机识别相关
    public static void YMPhoneInfo(Context context)
    {
        TelephonyManager mTm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String phoneType = android.os.Build.MODEL;  //获得手机机型
        String imei = mTm.getDeviceId();            //移动设备识别码,识别用户的手机
        String imsi = mTm.getSubscriberId();        //国际移动用户识别码,识别用户的手机卡

        Map<String, String> map_ekv = new HashMap<String, String>();
        if (phoneType != null){
            map_ekv.put("phoneType", phoneType);
        }
        if (imei != null){
            map_ekv.put("imei", imei);
        }
        if (imsi != null){
            map_ekv.put("imsi", imsi);
        }
        MobclickAgent.onEvent(context, "phone_type", map_ekv);
        Log.i("TAG", "手机型号:"+ phoneType+" imei:"+ imei + " imsi:"+ imsi);
    }
}
