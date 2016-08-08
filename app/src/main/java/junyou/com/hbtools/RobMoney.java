package junyou.com.hbtools;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RobMoney extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener
{

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

    //-----------[QQ红包]---------------//
    static final String QQ_HONGBAO_TEXT_KEY = "[QQ红包]";

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i("TAG","service onCreate");
    }

    public RobMoney()
    {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        if (sharedPreferences == null)
        {
            Log.i("TAG","sharedPreferences is null");
            return;
        }

        setCurrentActivityName(event);
	        /* 检测通知消息 */
        if (!mMutex)
        {
            //是否是红包的判断，若不是红包直接返回
            if (watchNotifications(event)) return;
            //若是红包，执行点击红包的操作
            //if(openQQHongbao(event)) return;			//抢QQ红包
            if (openWeChatHongbao(event)) return;		//抢微信
            mListMutex = false;
        }

        if (!mChatMutex)
        {
            mChatMutex = true;
            watchChat(event);
            mChatMutex = false;
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
            Log.i("TAG","NULLLLL");
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

                Log.i("TAG","contdescri=="+contentDescription);
                Log.i("TAG","descri=="+signature.getContentDescription());
                Log.i("TAG","contentDescription exist");
                return true;
            }
            else
            {
                Log.i("TAG","contdescri=="+contentDescription);
                Log.i("TAG","descri=="+signature.getContentDescription());
                Log.i("TAG","contentDescription is null");
            }
        }
        return false;
    }

    //观察通知栏的消息，查找[微信红包]或者[QQ红包关]关键字，打开通知栏消息
    private boolean watchNotifications(AccessibilityEvent event)
    {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        {
            return false;
        }
        Log.i("TAG","notification state changed!!!");
        // Not a hongbao
        String tip = event.getText().toString();
        if (tip.contains(WECHAT_NOTIFICATION_TIP) || tip.contains(QQ_HONGBAO_TEXT_KEY))
        {
            Log.i("TAG","is hongbao~~~~~~");
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
            Log.i("TAG","not hongbao!");
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
        Boolean watchOnLockFlag = sharedPreferences.getBoolean("pref_watch_on_lock", false);
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
        if (key.equals("pref_watch_on_lock"))
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
        info.packageNames = new String[]{"com.tencent.mobileqq"};
        setServiceInfo(info);
    }

    @Override
    public void onDestroy()
    {
        this.powerUtil.handleWakeLock(false);
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
        if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNode != null))
        {
            mMutex = true;

            mReceiveNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mLuckyMoneyReceived = false;
            mLuckyMoneyPicked = true;
        }
        /* 如果戳开但还未领取 */
        if (mUnpackCount == 1 && (mUnpackNode != null))
        {
            int delayFlag = sharedPreferences.getInt("pref_open_delay", 0) * 1000;
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            try {
                                mUnpackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
        AccessibilityNodeInfo node1 = (sharedPreferences.getBoolean("pref_watch_self", false)) ?
                this.getTheLastNode(WECHAT_VIEW_OTHERS_CH, WECHAT_VIEW_SELF_CH) : this.getTheLastNode(WECHAT_VIEW_OTHERS_CH);
        if (node1 != null && currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY)) {
            String excludeWords = sharedPreferences.getString("pref_watch_exclude_words", "");
            if (this.signature.generateSignature(node1, excludeWords)) {
                mLuckyMoneyReceived = true;
                mReceiveNode = node1;
                Log.d("sig", this.signature.toString());
            }
            return;
        }

        /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
        AccessibilityNodeInfo node2 = findOpenButton(this.rootNodeInfo);
        if (node2 != null && "android.widget.Button".equals(node2.getClassName()) && currentActivityName.contains(WECHAT_LUCKMONEY_RECEIVE_ACTIVITY)) {
            mUnpackNode = node2;
            mUnpackCount += 1;
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
            performGlobalAction(GLOBAL_ACTION_BACK);
            signature.commentString = generateCommentString();
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

        Boolean needComment = sharedPreferences.getBoolean("pref_comment_switch", false);
        if (!needComment) return null;

        String[] wordsArray = sharedPreferences.getString("pref_comment_words", "").split(" +");
        if (wordsArray.length == 0) return null;

        Boolean atSender = sharedPreferences.getBoolean("pref_comment_at", false);
        if (atSender)
        {
            return "@" + signature.sender + " " + wordsArray[(int) (Math.random() * wordsArray.length)];
        } else
        {
            return wordsArray[(int) (Math.random() * wordsArray.length)];
        }
    }
}
