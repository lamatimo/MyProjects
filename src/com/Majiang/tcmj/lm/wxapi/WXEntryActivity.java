package com.Majiang.tcmj.lm.wxapi;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;



import android.app.Activity;
import org.cocos2dx.lib.Cocos2dxActivity;
import android.os.Bundle;
//import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import com.Majiang.tcmj.lm.Constants;
import com.Majiang.tcmj.lm.wxSDK;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import android.content.Intent;


public class WXEntryActivity extends Cocos2dxActivity implements IWXAPIEventHandler{

	//private IWXAPI apiHandle;
	//private Cocos2dxActivity app=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		wxSDK.apiHandle= WXAPIFactory.createWXAPI(this,wxSDK.APP_ID,true);

		wxSDK.apiHandle.registerApp(wxSDK.APP_ID);

		wxSDK.apiHandle.handleIntent(getIntent(),this);

		//app=this;
    }

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {
		finish();
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	//app发送消息给微信，处理返回消息的回调
	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
			//用户同意
			case BaseResp.ErrCode.ERR_OK:
				String code = ((SendAuth.Resp) resp).code;
				wxSDK.loginError=1;
				wxSDK.loginCode=code;
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				wxSDK.loginError=-1;
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				wxSDK.loginError=-2;
				break;
			case BaseResp.ErrCode.ERR_UNSUPPORT:
				wxSDK.loginError=-3;
				break;
			default:
				wxSDK.loginError=-4;
				break;
			}

		finish();
	}


	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		wxSDK.apiHandle.handleIntent(intent, this);
		finish();
	}

	//分享
}