package com.Majiang.tcmj.lm;	//包名要与申请的一致


import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap.CompressFormat;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import android.content.Intent;
import android.net.Uri;
import com.Majiang.tcmj.lm.LocationUtils;
import android.text.ClipboardManager;//导入需要的库
import android.content.IntentFilter;
import android.content.Context;
import com.Majiang.tcmj.lm.PowerManagerUtils;



public class wxSDK {

    //	APP_ID
    public static final String APP_ID = "这个写自己的";
    //api句柄
    public static IWXAPI apiHandle=null;
    //微信登录返回的编号
    public static int loginError=0;
    //微信登录code
    public static String loginCode="";
    //AppActivity实例
    public static Cocos2dxActivity AppActivityIns;
    //copyStr
    private static String copyStr="";



    //发送一条msg到js
    public static void showInfo(String msg) {
        Cocos2dxJavascriptJavaBridge.evalString("const JsAndJava=require(\"JsAndJava\");JsAndJava.showInfo(\""+msg+"\");");
    }


    /**************内部调用***********************/
    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /********************************JS层调用**************************************/

    //微信登录授权
    public static void toWXLogin() {
        if (!wxSDK.apiHandle.isWXAppInstalled()) {
            //未安装微信客户端处理
            return;
        }
        wxSDK.loginError=0;

        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "diandi_wx_login";
        wxSDK.apiHandle.sendReq(req);
    }

    //获取登录返回的编号
    public static int getLoginError() {
        //0等待返回数据，1可以获取用户信息
        //-1用户取消,-2用户拒绝，-3不支持,-4未知原因
        return wxSDK.loginError;
    }

    //获取登录的code
    public static String getLoginCode(int x) {
        return wxSDK.loginCode;
    }

    //文字分享  0,1,2   聊天，朋友圈，微信收藏
    public static int shareText(String shareStr,int targetScene){

        if (shareStr == null || shareStr.length() == 0) {
            return -2;
        }

        // 初始化WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = shareStr;

        // 格式化
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = shareStr;

        // req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = wxSDK.buildTransaction("text");
        req.message = msg;
        req.scene = targetScene;

        wxSDK.apiHandle.sendReq(req);
        return targetScene;
    }

    //图片分享  0,1,2   聊天，朋友圈，微信收藏
    public static int shareImage(String filePath,int targetScene){

        if (new File(filePath).exists()) {
            Object localObject1 = BitmapFactory.decodeFile(filePath);
            Object localObject2 = new WXImageObject((Bitmap) localObject1);

            WXMediaMessage localWXMediaMessage = new WXMediaMessage();
            localWXMediaMessage.mediaObject = ((WXMediaMessage.IMediaObject) localObject2);
            localObject2 = Bitmap.createScaledBitmap((Bitmap) localObject1, 128, 72, true);
            ((Bitmap) localObject1).recycle();
            localWXMediaMessage.thumbData = wxSDK.bmpToByteArray((Bitmap) localObject2, true);
            localObject1 = new SendMessageToWX.Req();
            ((SendMessageToWX.Req) localObject1).transaction = String.valueOf(System.currentTimeMillis());
            ((SendMessageToWX.Req) localObject1).message = localWXMediaMessage;


            ((SendMessageToWX.Req) localObject1).scene = targetScene;

            wxSDK.apiHandle.sendReq((BaseReq) localObject1);
            return 1;
        }
        else{
            return -1;
        }
    }

    //调用浏览器打开链接
    public static void openBrowser(String url){
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            wxSDK.AppActivityIns.startActivity(it);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
    }

    //获取位置
    public static String getLocation(){
        return LocationUtils.getInstance().getLocations(wxSDK.AppActivityIns);
    }

    //复制内容到剪切板
    public static void setTextToClipboard(final String str){
        try
        {
            //Log.d("cocos2dx","copyToClipboard " + text);
            Runnable runnable = new Runnable() {
                public void run() {
                    ClipboardManager cm = (ClipboardManager) wxSDK.AppActivityIns.getSystemService(wxSDK.AppActivityIns.CLIPBOARD_SERVICE);
                    //将文本内容放到系统剪贴板里。
                    cm.setText(str);
                }
            };
            //getSystemService运行所在线程必须执行过Looper.prepare()
            //否则会出现Can't create handler inside thread that has not called Looper.prepare()
            wxSDK.AppActivityIns.runOnUiThread(runnable);

        }catch(Exception e){
            // Log.d("cocos2dx","copyToClipboard error");
            e.printStackTrace();
        }
    }

    //从剪切板获取内容
    public static String getTextFromClipboard(){
        try
        {
            return wxSDK._getTextFromClipboard();
        }catch(Exception e){
            e.printStackTrace();
            //尝试用UI线程获取
            Runnable runnable = new Runnable() {
                public void run() {
                    wxSDK.copyStr=wxSDK._getTextFromClipboard();
                }
            };
            wxSDK.AppActivityIns.runOnUiThread(runnable);

            return "__ERROR";
        }
    }

    //从剪切板获取内容
    public static String _getTextFromClipboard() {
            ClipboardManager clipboardManager = (ClipboardManager) wxSDK.AppActivityIns.getSystemService(wxSDK.AppActivityIns.CLIPBOARD_SERVICE);

            if (!clipboardManager.hasText()) {
                return "";
            } else {
                return clipboardManager.getText().toString();
            }

    }

    //从剪切板获取
    public static String getCopyStr() {
        return wxSDK.copyStr;
    }

    //获取手机电量
    public static int getPower(String objectName,String funcName) {
        String battertInfo="";
        PowerManagerUtils.getInstance().registerPowerListener(wxSDK.AppActivityIns,objectName,funcName);

        return PowerManagerUtils.getInstance().getPowerPercent(wxSDK.AppActivityIns);
    }

    //测试下数据
    public static int getTestNum(){
        return PowerManagerUtils.getInstance().testNum;
    }

    

}
