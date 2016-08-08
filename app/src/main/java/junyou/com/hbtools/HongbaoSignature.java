package junyou.com.hbtools;


import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

public class HongbaoSignature
{
    public String sender, content, time, contentDescription = "", commentString;
    public boolean others;

    public boolean generateSignature(AccessibilityNodeInfo node, String excludeWords)
    {
        try {
            AccessibilityNodeInfo hongbaoNode = node.getParent();
            if (!"android.widget.LinearLayout".equals(hongbaoNode.getClassName())) return false;

            String hongbaoContent = hongbaoNode.getChild(0).getText().toString();
            if (hongbaoContent == null || "查看红包".equals(hongbaoContent)) return false;

            String[] excludeWordsArray = excludeWords.split(" +");
            for (String word : excludeWordsArray)
            {
                if (word.length() > 0 && hongbaoContent.contains(word)) return false;
            }

            AccessibilityNodeInfo messageNode = hongbaoNode.getParent();

            Rect bounds = new Rect();
            messageNode.getBoundsInScreen(bounds);
            if (bounds.top < 0) return false;

            String[] hongbaoInfo = getSenderContentDescriptionFromNode(messageNode);
            if (this.getSignature(hongbaoInfo[0], hongbaoContent, hongbaoInfo[1]).equals(this.toString())) return false;

            this.sender = hongbaoInfo[0];
            this.time = hongbaoInfo[1];
            this.content = hongbaoContent;
            return true;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() 
    {
        return this.getSignature(this.sender, this.content, this.time);
    }

    private String getSignature(String... strings)
    {
        String signature = "";
        for (String str : strings)
        {
            if (str == null) return null;
            signature += str + "|";
        }

        return signature.substring(0, signature.length() - 1);
    }

    public String getContentDescription()
    {
        return this.contentDescription;
    }

    public void setContentDescription(String description)
    {
        this.contentDescription = description;
    }

    public void cleanSignature()
    {
        this.content = "";
        this.time = "";
        this.sender = "";
    }

    private String[] getSenderContentDescriptionFromNode(AccessibilityNodeInfo node)
    {
        int count = node.getChildCount();
        String[] result = {"unknownSender", "unknownTime"};
        for (int i = 0; i < count; i++)
        {
            AccessibilityNodeInfo thisNode = node.getChild(i);
            if ("android.widget.ImageView".equals(thisNode.getClassName()) && "unknownSender".equals(result[0]))
            {
                CharSequence contentDescription = thisNode.getContentDescription();
                if (contentDescription != null) result[0] = contentDescription.toString().replaceAll("头像$", "");
            } else if ("android.widget.TextView".equals(thisNode.getClassName()) && "unknownTime".equals(result[1]))
            {
                CharSequence thisNodeText = thisNode.getText();
                if (thisNodeText != null) result[1] = thisNodeText.toString();
            }
        }
        return result;
    }
}
