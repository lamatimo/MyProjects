package com.Majiang.tcmj.lm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.cocos2dx.lib.Cocos2dxActivity;

import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

public class PowerManagerUtils {
    static PowerManagerUtils powerManagerUtils;
    int batteryLevel=0;
    BatteryBroadcastReceiver receiver;

    //是否注册过
    boolean isRegister=false;
    //注册者
    Context register;
    String jsObjectName=null;
    String jsFuncName=null;

    //测试数据
    public int testNum=0;


    public static PowerManagerUtils getInstance(){
        if (powerManagerUtils == null){
            powerManagerUtils = new PowerManagerUtils();
        }
        return powerManagerUtils;
    }

    //注册电量监听
    public void registerPowerListener(Context context,String objectName,String funcName){
        if(!isRegister) {
            register = context;
            receiver = new BatteryBroadcastReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent=register.registerReceiver(receiver, filter);
            //获取当前电量
            int level = intent.getIntExtra("level", 0);
            //电量的总刻度
            int scale = intent.getIntExtra("scale", 100);
            //把它转成百分比
            batteryLevel =  level * 100 / scale;
            isRegister = true;
        }
        jsFuncName = funcName;
        jsObjectName = objectName;

    }

    //获取电量
    public int getPowerPercent(Context context){
        if(!isRegister){
            registerPowerListener(context,"","");
        }
        return batteryLevel;
    }

    class BatteryBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断它是否是为电量变化的Broadcast Action
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                //把它转成百分比
                batteryLevel =  level * 100 / scale;


                if (jsObjectName != null && jsObjectName.length() != 0) {

                    Runnable runnable = new Runnable() {
                        public void run() {
                            Cocos2dxJavascriptJavaBridge.evalString("const wxSDK=require(\"wxSDK\");"
                                    + "wxSDK.ceshi2("
                                    +testNum+");"
                            );
                            testNum++;
//                            Cocos2dxJavascriptJavaBridge.evalString(
//                                    "const "+jsObjectName+"=require('"+jsObjectName+"');"
//                                            + jsObjectName+"."+jsFuncName+"("+batteryLevel+");"
//                            );
                        }
                    };
                    ((Cocos2dxActivity)register).runOnGLThread(runnable);
                }
            }
        }
    }

    public void closeBatteryListener(){
        register.unregisterReceiver(receiver);
        isRegister=false;
    }
}
