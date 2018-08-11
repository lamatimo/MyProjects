const NOOP=()=>{};
const APP_ID="填自己的";
const APP_SECRET="填自己的";


const wxSDK={
    
}

//调出微信授权界面
//@onSuccess 成功的回调
//@onFailure 失败回调
wxSDK.toWXLoin=(onSuccess,onFailure)=>{
    onSuccess=onSuccess||NOOP;
    onFailure=onFailure||NOOP;
    let com=cc.find('Canvas')._components[0];

    jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "toWXLogin", "()V");

    let func=()=>{
        let errorCode=wxSDK.getLoginError();

        if(errorCode>0){
            //得到code
            let code=wxSDK.getLoginCode();
            wxSDK.getTokenByCode(code,onSuccess);
        }
        else if(errorCode<0){
            onFailure(errorCode);
        }

        if(errorCode!=0){
            com.unschedule(func);
        }
    }

    com.schedule(func,0.2);

}

//微信文字分享
wxSDK.shareText=(shareStr,target)=>{
    return jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "shareText", "(Ljava/lang/String;I)I",shareStr,parseInt(target));
}

/**
 * 截屏
 * @param  {[function]} func 回调函数
 * @return
 */
wxSDK.screenShoot = function(node,func) {
    
    if (!CC_JSB) return;
    let dirpath = jsb.fileUtils.getWritablePath() + 'ScreenShoot/';
    if (!jsb.fileUtils.isDirectoryExist(dirpath)) {
        jsb.fileUtils.createDirectory(dirpath);
    }
    let name = 'ScreenShoot-' + (new Date()).valueOf() + '.png';
    
    let filepath = dirpath + name;
    let size = cc.winSize;
    let rt = cc.RenderTexture.create(size.width, size.height);
    
    node._sgNode.addChild(rt);
    
    rt.setVisible(false);
    rt.begin();
    
    cc.director.getScene()._sgNode.visit();
  
    rt.end();
   
    
    rt.saveToFile('ScreenShoot/' + name, cc.ImageFormat.PNG, true, function() {
        rt.removeFromParent();
        if (func) {
            func(filepath);
        }
    });
}

/**
 * 截屏分享
 * @param  {[cc.Node]} node 要截的节点
 * @param  {[int]} targetScene 要分享到的场景(0,1,2)
 * @return
 */
wxSDK.shareImage=(node,targetScene)=>{
    let callFunc=(filepath)=>{
        let retCode=jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "shareImage", "(Ljava/lang/String;I)I",filepath,parseInt(targetScene));
        wxSDK.node.changeLabel("错误代码:"+retCode);
    }

    wxSDK.screenShoot(node,callFunc);
}

/**
 * 调用浏览器打开链接
 * @param  {[String]} url 要打开的链接
 * @return
 */
wxSDK.openBrowser=(url)=>{
    //url必须是http或者https开头
    if(-1==url.indexOf("http://")&&-1==url.indexOf("https://")){
        url="http://"+url;
    }

    wxSDK.node.changeLabel(url);
    jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "openBrowser", "(Ljava/lang/String;)V",url);
}

/**
 * 获取位置
 * @param  
 * @return
 */
wxSDK.getLocation=()=>{
    let retStr=jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getLocation", "()Ljava/lang/String;");

    if("-1"==retStr){
        wxSDK.node.changeLabel("没有权限！");
    }
    else{
        wxSDK.node.changeLabel("经纬度:"+retStr);
    }
}

/**
 * 复制文本到剪切板
 * @param  {[String]} str 要复制的文本
 * @return
 */
wxSDK.setTextToClipboard=(str)=>{
    jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "setTextToClipboard", "(Ljava/lang/String;)V",str);
}

/**
 * 从剪切板获取文本
 * @param   {[function]} onSuccess 成功以后将调用
 * @return  {[String]} 获取的文本（异步的）
 */
wxSDK.getTextFromClipboard=(onSuccess)=>{
    let retStr=jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getTextFromClipboard", "()Ljava/lang/String;");
    if(retStr!="__ERROR"){
        onSuccess(retStr);
    }
    else{
        let com = cc.find('Canvas')._components[0];


        let func = () => {
            com.unschedule(func);
            retStr=jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getCopyStr", "()Ljava/lang/String;");
            onSuccess(retStr);
        }

        com.scheduleOnce(func, 0.2);
    }

}

/**
 * 获取电量
 * @param   {[function]} changeCall 如果传则监听电量变化
 * @return  {[String]} 电量信息
 */
wxSDK.getPower=(changeCall)=>{
    if("function"==typeof changeCall){
        wxSDK.powerChangeCall=changeCall;
    }
    else{
        wxSDK.powerChangeCall=NOOP;
    }
   
    let objName="wxSDK";
    let funcName="powerChange";

    return jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getPower", "(Ljava/lang/String;Ljava/lang/String;)I",objName,funcName);
}

/**
 * 电量变化调用
 * @param   {[int]} percent 电量的百分比
 * @return  
 */
wxSDK.powerChange=(percent)=>{
    if(!wxSDK.cnt){
        wxSDK.cnt=0;
    }

    wxSDK.powerChangeCall(percent);
    wxSDK.node.changeLabel(wxSDK.cnt+++"电量变化:"+percent);
}

/**
 * 取消电量监听
 * @param
 * @return
 */
wxSDK.cancelPowerMonitor=()=>{
    wxSDK.powerChangeCall=NOOP;
}


//通过code获取access_token
wxSDK.getTokenByCode=(code,onSuccess)=>{
    let url="https://api.weixin.qq.com/sns/oauth2/access_token?appid="
            +APP_ID+"&secret="
            +APP_SECRET+"&code="
            +code+"&grant_type=authorization_code";
    
    
    let xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4 && (xhr.status >= 200 && xhr.status < 400)) {
            let access_token = xhr.responseText;
            wxSDK.getUserInfo(JSON.parse(access_token),onSuccess);
        }
    };
    xhr.open("GET", url, true);
    xhr.send();
}

//通过access_token获取用户信息
wxSDK.getUserInfo = (info,onSuccess) => {
    let url = "https://api.weixin.qq.com/sns/userinfo?access_token="
        + info.access_token + "&openid="
        + info.openid;

    
    let xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4 && (xhr.status >= 200 && xhr.status < 400)) {
            let userInfo = xhr.responseText;
            onSuccess(JSON.parse(userInfo));
        }
    };
    xhr.open("GET", url, true);
    xhr.send();
}

//获取登录状态
wxSDK.getLoginError=()=>{
    return jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getLoginError", "()I");
}

//获取微信code
wxSDK.getLoginCode=()=>{
    return jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getLoginCode", "(I)Ljava/lang/String;");
}

/************************************************android层调用*******************************************************/

wxSDK.ceshi=(onSuccess,onFailure)=>{
    
    let numbel=jsb.reflection.callStaticMethod("com/Majiang/tcmj/lm/wxSDK", "getTestNum", "()I");
    
    if(numbel!=wxSDK.rr){
        wxSDK.node.changeLabel(numbel);
        wxSDK.rr=numbel;
    }
}

wxSDK.ceshi2=(hehe)=>{
    wxSDK.node.changepow(hehe);
}

module.exports=wxSDK;