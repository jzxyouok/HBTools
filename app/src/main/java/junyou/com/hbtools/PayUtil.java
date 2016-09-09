package junyou.com.hbtools;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

public class PayUtil {

    //抢红包次数
    public static void YMgrasp_num(Context context)
    {
        //记录到友盟
        MobclickAgent.onEvent(context,"grasp_num");
    }
    //计费请求次数
    public static void YMpurchase_num(Context context)
    {
        //记录到友盟
        MobclickAgent.onEvent(context,"purchase_num");
    }

    //收到的钱
    public static void YMmoney_count(Context context,int payid)
    {
        String payString = "money_count";
        int payType  = 0; //付费类型类型
        Map<String, String> map_value = new HashMap<String, String>();

        //记录到友盟
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
                map_value.put("all" , "18.00" );
                payType = 2;
                break;
        }

        MobclickAgent.onEventValue(context, payString , map_value, payType);
    }
}
