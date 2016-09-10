package junyou.com.hbtools;

/**
 * Created by Administrator on 2016/9/6.
 */
public class Constants {
    // APP_ID 为apk应用从官方网站申请到的合法appId
    // 签名：bd2d158d72211ed13c1e08694fbec1b2
    // 包名：junyou.com.hbtools
    public static final String APP_ID = "wx2eb61bf0cd4d04a9";

    public static class ShowMsgActivity {
        public static final String STitle = "showmsg_title";
        public static final String SMessage = "showmsg_message";
        public static final String BAThumbData = "showmsg_thumb_data";
    }

    //服务是否能够开启 若有天数，能抢红包，若没有天数，不能抢红包
    public static final String IS_SERVICE_ON = "isserviceon";         //服务是否开启
    public static final String IS_ALLLIFEUSE = "isalllifeuse";        //是否终身使用
    public static final String LEFT_DAYS_COUNT = "left_days_count";  //剩余的天数
    public static final String IS_NEW_DAY = "is_new_day";           //是否新的一天

    public static final String USE_DAY = "use_day";                 //用户使用的天数
}
