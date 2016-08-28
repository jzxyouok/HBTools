package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RobMoney extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static RobMoney instance;

    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_DETAILS_CH = "红包详情";
    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "已超过24小时";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";
    private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";
    private static final String WECHAT_LUCKMONEY_RECEIVE_ACTIVITY = "LuckyMoneyReceiveUI";
    private static final String WECHAT_LUCKMONEY_DETAIL_ACTIVITY = "LuckyMoneyDetailUI";
    private static final String WECHAT_LUCKMONEY_GENERAL_ACTIVITY = "LauncherUI";
    private String currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY;

    private boolean mMutex = false, mListMutex = false, mChatMutex = false;
        private SharedPreferences sharedPreferences;
    private HongbaoSignature signature = new HongbaoSignature();
    private PowerUtil powerUtil;
    private AccessibilityNodeInfo rootNodeInfo, mReceiveNode, mUnpackNode;
    private boolean mLuckyMoneyPicked, mLuckyMoneyReceived;
    private int mUnpackCount = 0;
    private boolean mIsEnterWeChatList=false;

    //四个标签的存储字符
    private String mTotalNum = "totalnum";
    private String mTotalNumToday = "totalnumtoday";
    private String mTotalMoney = "totalmoney";
    private String mTotalMoneyToday = "totalmoneytoday";

    //-----------[QQ红包]---------------//
    static final String QQ_HONGBAO_TEXT_KEY = "[QQ红包]";
    private boolean caihongbao = false;
    private AccessibilityNodeInfo rootNodeInfo_1;
    private List<AccessibilityNodeInfo> mReceiveNode_1;
    private boolean mLuckyMoneyReceived_1;
    private String lastFetchedHongbaoId = null;
    private long lastFetchedTime = 0;
    private static final int MAX_CACHE_TOLERANCE = 5000;
    private static final String WECHAT_OPEN_EN = "Open";
    private static final String WECHAT_OPENED_EN = "You've opened";
    private final static String QQ_DEFAULT_CLICK_OPEN = "点击拆开";
    //    private final static String QQ_DEFAULT_HAVE_OPENED = "已拆开";
    private final static String QQ_HONG_BAO_PASSWORD = "口令红包";
    private final static String QQ_CLICK_TO_PASTE_PASSWORD = "点击输入口令";

    //广播
    private MsgReceiver msgReceiver;
    boolean mIsWeChatOn = true;
    boolean mIsQQOn = true;
    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
            mIsWeChatOn = intent.getBooleanExtra("wechat_broadcast", true);
            int v_1 = mIsWeChatOn==true ? 1:0;
            Log.i("TAG", "微信消息:" + v_1);

            mIsQQOn = intent.getBooleanExtra("qq_broadcast",true);
            int v_2 = mIsQQOn == true ? 1:0;
            Log.i("TAG", "qq消息" + v_2);
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        Log.i("TAG","service onCreate");
        //动态注册广播接收器
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("junyou.com.hbtools.RECEIVER");
        registerReceiver(msgReceiver, intentFilter);
    }

    public RobMoney()
    {

    }

    public static RobMoney getInstance()
    {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        if (sharedPreferences == null)
        {
            return;
        }
        //关闭了红包快手
        if (!sharedPreferences.getBoolean("pref_watch_notification",true))
        {
            Log.i("TAG", "不能抢红包了，关闭了红包助手");
            return;
        }
        setCurrentActivityName(event);
	        /* 检测通知消息 */
        if (!mMutex)
        {
            //是否是红包的判断，若是红包就打开消息栏进入该软件，若不是红包直接返回
            if (watchNotifications(event)) return;
            //若是红包，执行点击红包的操作
            if(openQQHongbao(event)) return;
            if (openWeChatHongbao(event)) return;	//监视微信（貌似这个方法没作用）
            mListMutex = false;
        }
        if (!mChatMutex)
        {
            mChatMutex = true;
            watchChat(event);           //  监视微信
            mChatMutex = false;
            mIsEnterWeChatList = false;
        }
    }

    //查找红包列表，执行点击红包事件
    private boolean openWeChatHongbao(AccessibilityEvent event)
    {
        if (mListMutex) return false;
        mListMutex = true;
        AccessibilityNodeInfo eventSource = event.getSource();
        // Not a message
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || eventSource == null)
        {
            return false;
        }
        List<AccessibilityNodeInfo> nodes = eventSource.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);

        if (!nodes.isEmpty() && currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY))
        {
            AccessibilityNodeInfo nodeToClick = nodes.get(0);

            if (nodeToClick == null) return false;

            CharSequence contentDescription = nodeToClick.getContentDescription(); //从红包节点上获取的值

            if (contentDescription != null && !signature.getContentDescription().equals(contentDescription))
            {
                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);		//自动打开红包
                signature.setContentDescription(contentDescription.toString());
                return true;
            }
        }
        return false;
    }

    //观察QQ通知栏
    private boolean qqNotification(AccessibilityEvent event)
    {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        {
            return false;
        }
        Log.i("TAG","通知栏有QQ消息!!!");
        // Not a hongbao
        String tip = event.getText().toString();
        if (tip.contains(QQ_HONGBAO_TEXT_KEY))
        {
            Log.i("TAG","是QQ红包~~~");
            if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification))
            {
                return false;
            }

            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification)
            {
                Notification notification = (Notification) parcelable;
                try {
                    notification.contentIntent.send();
                } catch (PendingIntent.CanceledException e)
                {
                    e.printStackTrace();
                }
            }
            return true;
        }
        else
        {
            Log.i("TAG","不是QQ红包!");
        }

        return true;
    }

    //观察通知栏的消息，查找[微信红包]或者[QQ红包关]关键字，打开通知栏消息
    private boolean watchNotifications(AccessibilityEvent event)
    {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        {
            return false;
        }
        Log.i("TAG","通知栏有消息!!!");
        // Not a hongbao
        String tip = event.getText().toString();
        if (tip.contains(WECHAT_NOTIFICATION_TIP) || tip.contains(QQ_HONGBAO_TEXT_KEY))
        {
            Log.i("TAG","是红包~~~");
            mIsEnterWeChatList = true;
            if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification))
            {
                return false;
            }

            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification)
            {
                Notification notification = (Notification) parcelable;
                try {
                    /* 清除signature,避免进入会话后误判 */
                    signature.cleanSignature();
                    notification.contentIntent.send();
                } catch (PendingIntent.CanceledException e)
                {
                    e.printStackTrace();
                }
            }
            return true;
        }
        else
        {
            Log.i("TAG","不是红包");
        }

        return true;
    }

    private void setCurrentActivityName(AccessibilityEvent event)
    {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        {
            return;
        }

        Log.i("TAG","window state changed");

        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            getPackageManager().getActivityInfo(componentName, 0);
            currentActivityName = componentName.flattenToShortString();
        } catch (PackageManager.NameNotFoundException e)
        {
            currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY;
        }
    }

    private void watchFlagsFromPreference()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        this.powerUtil = new PowerUtil(this);
        Boolean watchOnLockFlag = sharedPreferences.getBoolean("pref_watch_on_lock", false);//是否息屏抢红包
        this.powerUtil.handleWakeLock(watchOnLockFlag);
    }

    @Override
    public void onInterrupt()
    {

    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        Log.i("TAG","service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("pref_watch_on_lock"))   //是否息屏抢红包
        {
            Boolean changedValue = sharedPreferences.getBoolean(key, false);
           this.powerUtil.handleWakeLock(changedValue);
        }
    }

     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     @Override
    public void onServiceConnected()
    {
        Log.i("TAG","onServiceConnected");
        super.onServiceConnected();
        this.watchFlagsFromPreference();
        AccessibilityServiceInfo info = getServiceInfo();
        //这里可以设置多个包名，监听多个应用
        info.packageNames = new String[]{"com.tencent.mobileqq","com.tencent.mm"};
        setServiceInfo(info);
    }

    @Override
    public void onDestroy()
    {
        this.powerUtil.handleWakeLock(false);
        //注销广播
        unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void watchChat(AccessibilityEvent event)
    {
        this.rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) return;
        mReceiveNode = null;
        mUnpackNode = null;
        checkNodeInfo(event.getEventType());

        /* 如果已经接收到红包并且还没有戳开 */
        if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNode != null) && mIsEnterWeChatList)
        {
            Log.i("TAG","收到红包并且还没有戳开");
            mMutex = true;
            mReceiveNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mLuckyMoneyReceived = false;
            mLuckyMoneyPicked = true;
        }
        /* 如果戳开但还未领取 */
        if (mUnpackCount == 1 && (mUnpackNode != null))
        {
            Log.i("TAG","打开了一个红包");     //监听抢到的红包个数
            int delayFlag = sharedPreferences.getInt("pref_open_delay", 0) * 1000;  //延时拆开红包
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            try {
                                Log.i("TAG","打开了红包。。。。");
                                mUnpackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                addTotalNum();        //记录红包个数
                            } catch (Exception e) {
                                mMutex = false;
                                mLuckyMoneyPicked = false;
                                mUnpackCount = 0;
                            }
                        }
                    },
                    delayFlag);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkNodeInfo(int eventType)
    {
        if (this.rootNodeInfo == null) return;

        if (signature.commentString != null)
        {
            sendComment();
            signature.commentString = null;
        }

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */

        AccessibilityNodeInfo node1 = (sharedPreferences.getBoolean("pref_watch_self", false)) ?//拆开自己发的红包
                this.getTheLastNode(WECHAT_VIEW_OTHERS_CH, WECHAT_VIEW_SELF_CH) : this.getTheLastNode(WECHAT_VIEW_OTHERS_CH);
        if (node1 != null && currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY))
        {
//            Log.i("TAG","聊天会话窗口，遍历节点匹配“领取红包”和查看红包");
            String excludeWords = sharedPreferences.getString("pref_watch_exclude_words", "");  //屏蔽红包文字内容
           // Log.i("TAG","excludeWords=="+ excludeWords);
            if (this.signature.generateSignature(node1, excludeWords))
            {
                Log.i("TAG","进来了、、、、、、");
                mLuckyMoneyReceived = true;
                mReceiveNode = node1;
            }
            return;
        }

        /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
        AccessibilityNodeInfo node2 = findOpenButton(this.rootNodeInfo);
        if (node2 != null &&
                "android.widget.Button".equals(node2.getClassName()) &&
                currentActivityName.contains(WECHAT_LUCKMONEY_RECEIVE_ACTIVITY))
        {
            mUnpackNode = node2;
            mUnpackCount += 1;
            Log.i("TAG", "有拆红包关键字");
            return;
        }

        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了” */
        boolean hasNodes = this.hasOneOfThoseNodes(
                WECHAT_BETTER_LUCK_CH, WECHAT_DETAILS_CH,
                WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH);

        if (mMutex && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && hasNodes
                && (currentActivityName.contains(WECHAT_LUCKMONEY_DETAIL_ACTIVITY)
                || currentActivityName.contains(WECHAT_LUCKMONEY_RECEIVE_ACTIVITY))) {
            mMutex = false;
            mLuckyMoneyPicked = false;
            mUnpackCount = 0;
            performGlobalAction(GLOBAL_ACTION_BACK);            //点击返回键
            signature.commentString = generateCommentString();
            //在这里累加钱
            List<AccessibilityNodeInfo> list = getRootInActiveWindow().findAccessibilityNodeInfosByText("元");
            if (!list.isEmpty())
            {
                //下标是2的节点是红包的金额
                for (AccessibilityNodeInfo info:list)
                {
                    Log.i("TAG", "红包的钱:"+info.getParent().getChild(2).getText().toString());

//                    String mm = sharedPreferences.getString(mTotalMoney,"");
                    SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
                    String mm =  sharedP.getString(mTotalMoney,"");
                    DecimalFormat decimalFormat=new DecimalFormat("#0.00");
                    if (!"".equals(mm))
                    {
                        //原来是非空的
                        float oldd = Float.valueOf(mm);
                        float neww = Float.valueOf(info.getParent().getChild(2).getText().toString());
                        float result = oldd + neww;
                        //保留两位小数
                        String result_1=decimalFormat.format(result);

                        Log.i("TAG", "oldd："+ mm);
                        Log.i("TAG", "result："+ result_1);
                        SharedPreferences.Editor editor = sharedP.edit();
                        editor.putString(mTotalMoney,result_1);
                        editor.commit();
                        //写到主页上的标签上
                        MainActivity.getInstance().money_total.setText(result_1);
                        MainActivity.getInstance().money_today.setText(result_1);
                        MainActivity.getInstance().num_money.setText(result_1);
                    }else
                    {
                        //原来是空的
                        float neww = Float.valueOf(info.getParent().getChild(2).getText().toString());
                        String neww_1=decimalFormat.format(neww);
                        SharedPreferences.Editor editor = sharedP.edit();
                        editor.putString(mTotalMoney,neww_1);
                        editor.commit();
                        //写到主页上的标签上
                        MainActivity.getInstance().money_total.setText(neww_1);
                        MainActivity.getInstance().money_today.setText(neww_1);
                        MainActivity.getInstance().num_money.setText(neww_1);
                    }

                }
            }
            Log.i("TAG", "手慢了");
        }
    }

    //发送回复
    private void sendComment()
    {
	 /*
     try {
         AccessibilityNodeInfo outNode =
                 getRootInActiveWindow().getChild(0).getChild(0);
         AccessibilityNodeInfo nodeToInput = outNode.getChild(outNode.getChildCount() - 1).getChild(0).getChild(1);

         if ("android.widget.EditText".equals(nodeToInput.getClassName()))
         {
             Bundle arguments = new Bundle();
             arguments.putCharSequence(AccessibilityNodeInfo
                     .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, signature.commentString);
             nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
         }
     } catch (Exception e) {
         // Not supported
     }
     */
    }

    private AccessibilityNodeInfo getTheLastNode(String... texts)
    {
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null, tempNode;
        List<AccessibilityNodeInfo> nodes;

        for (String text : texts)
        {
            if (text == null) continue;
            nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (nodes != null && !nodes.isEmpty())
            {
                tempNode = nodes.get(nodes.size() - 1);
                if (tempNode == null) return null;
                Rect bounds = new Rect();
                tempNode.getBoundsInScreen(bounds);
                if (bounds.bottom > bottom)
                {
                    bottom = bounds.bottom;
                    lastNode = tempNode;
                    signature.others = text.equals(WECHAT_VIEW_OTHERS_CH);
                }
            }
        }
        return lastNode;
    }

    private AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo node)
    {
        if (node == null)
            return null;

        //非layout元素
        if (node.getChildCount() == 0)
        {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }

        //layout元素，遍历找button
        AccessibilityNodeInfo button;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            button = findOpenButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }

    private boolean hasOneOfThoseNodes(String... texts)
    {
        List<AccessibilityNodeInfo> nodes;
        for (String text : texts)
        {
            if (text == null) continue;

            nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);

            if (nodes != null && !nodes.isEmpty()) return true;
        }
        return false;
    }

    private String generateCommentString()
    {
        if (!signature.others) return null;

        Boolean needComment = sharedPreferences.getBoolean("pref_comment_switch", false);//开启自动回复
        if (!needComment) return null;

        String[] wordsArray = sharedPreferences.getString("pref_comment_words", "").split(" +");    //自定义回复内容
        if (wordsArray.length == 0) return null;

        Boolean atSender = sharedPreferences.getBoolean("pref_comment_at", false);            //@发红包的人
        if (atSender)
        {
            return "@" + signature.sender + " " + wordsArray[(int) (Math.random() * wordsArray.length)];
        } else
        {
            return wordsArray[(int) (Math.random() * wordsArray.length)];
        }
    }

    /**
     * QQ红包
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean openQQHongbao(AccessibilityEvent event)
    {
//        Log.i("TAG","监听QQ");
        if (mListMutex) return false;
        mListMutex = true;

        this.rootNodeInfo_1 = event.getSource();
        if (rootNodeInfo_1 == null)
        {
            return false;
        }
        mReceiveNode_1 = null;
        checkNodeInfo();
        // checkKey();
        /* 如果已经接收到红包并且还没有戳开 */
        if (mLuckyMoneyReceived_1 && (mReceiveNode_1 != null))
        {
            int size = mReceiveNode_1.size();
            if (size > 0)
            {
                String id = getHongbaoText(mReceiveNode_1.get(size - 1));
                long now = System.currentTimeMillis();
                if (this.shouldReturn(id, now - lastFetchedTime))
                {
                    return false;
                }

                lastFetchedHongbaoId = id;
                lastFetchedTime = now;

                AccessibilityNodeInfo cellNode = mReceiveNode_1.get(size - 1);
                if (cellNode.getText().toString().equals("口令红包已拆开"))
                {
                    return false;
                }
                //处理普通红包
                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("TAG","拆开普通红包");
                //处理口令红包
                if (cellNode.getText().toString().equals(QQ_HONG_BAO_PASSWORD))
                {
                    AccessibilityNodeInfo rowNode = getRootInActiveWindow();
                    if (rowNode == null)
                    {
                        return false;
                    } else
                    {
                        recycle(rowNode);
                    }
                }
                Log.i("TAG", "-----------结束------------");
                mLuckyMoneyReceived_1 = false;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkNodeInfo()
    {
        //Log.i("TAG", "监听聊天窗口");
        if (rootNodeInfo_1 == null)
        {
            Log.i("TAG", "333");
            return;
        }
         /* 聊天会话窗口，遍历节点匹配“点击拆开”，“口令红包”，“点击输入口令” */
        List<AccessibilityNodeInfo> nodes1 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo_1, new String[]{
                QQ_DEFAULT_CLICK_OPEN, QQ_HONG_BAO_PASSWORD, QQ_CLICK_TO_PASTE_PASSWORD, "发送"});

        if (!nodes1.isEmpty())
        {
            Log.i("TAG", "有QQ红包字眼出现");
            String nodeId = Integer.toHexString(System.identityHashCode(this.rootNodeInfo_1));
            if (!nodeId.equals(lastFetchedHongbaoId))
            {
                mLuckyMoneyReceived_1 = true;
                mReceiveNode_1 = nodes1;
            }
            return;
        }
    }

    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts)
    {
        for (String text : texts)
        {
            if (text == null) continue;
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty())
            {
                if (text.equals(WECHAT_OPEN_EN) && !nodeInfo.findAccessibilityNodeInfosByText(WECHAT_OPENED_EN).isEmpty())
                {
                    continue;
                }
                return nodes;
            }
        }
        return new ArrayList<>();
    }

    private String getHongbaoText(AccessibilityNodeInfo node)
    {
        /* 获取红包上的文本 */
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException npe) {
            return null;
        }
        return content;
    }

    private boolean shouldReturn(String id, long duration)
    {
        // ID为空
        if (id == null) return true;
        // 名称和缓存不一致
        if (duration < MAX_CACHE_TOLERANCE && id.equals(lastFetchedHongbaoId))
        {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void recycle(AccessibilityNodeInfo info)
    {
        if (info.getChildCount() == 0)
        {
//            Log.e(TAG, "child widget----------------------------" + info.getClassName());
//            Log.e(TAG, "showDialog:" + info.canOpenPopup());
//            Log.e(TAG, "Text：" + info.getText());
//            Log.e(TAG, "windowId:" + info.getWindowId());
            if (info.getText() != null && info.getText().toString().equals(QQ_CLICK_TO_PASTE_PASSWORD))
            {
                info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("TAG","点击输入口令");
                //performGlobalAction(GLOBAL_ACTION_BACK);

            }

            if (info.getClassName().toString().equals("android.widget.Button") && info.getText().toString().equals("发送"))
            {
                //点击发送消息
                Log.i("TAG","点击发送消息");
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
               // performGlobalAction(GLOBAL_ACTION_BACK);
            }

        } else
        {
            for (int i = 0; i < info.getChildCount(); i++)
            {
                if (info.getChild(i) != null)
                {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    private void addTotalNum()
    {
        SharedPreferences sharedP=  getSharedPreferences("config",MODE_PRIVATE);
        int nowNum =sharedP.getInt(mTotalNum,0) + 1;
        SharedPreferences.Editor editor = sharedP.edit();
        editor.putInt(mTotalNum,nowNum);
        editor.commit();

        Log.i("TAG", "抢到总共:"+ nowNum + "个红包");
       MainActivity.getInstance().num_total.setText(nowNum + "");
        MainActivity.getInstance().num_today.setText(nowNum + "");
        MainActivity.getInstance().num_redpkt.setText(nowNum + "");
    }
}
