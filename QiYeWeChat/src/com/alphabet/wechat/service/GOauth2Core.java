package com.alphabet.wechat.service;

import java.net.URLEncoder;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;

import net.sf.json.JSONObject;

/** 
 * @Title: GOauth2Core
 * @Description: 网页授权登录
 * @author yang.lvsen
 * @date 2018年5月16日 下午5:24:59
 */
public class GOauth2Core {
	
    /** 
     * 企业获取code地址处理 
     * @param appid 企业的CorpID 
     * @param redirect_uri 授权后重定向的回调链接地址，请使用urlencode对链接进行处理 
     * @param response_type 返回类型，此时固定为：code 
     * @param scope 应用授权作用域，此时固定为：snsapi_base 
     * @param state 重定向后会带上state参数，企业可以填写a-zA-Z0-9的参数值 
     * @param #wechat_redirect 微信终端使用此参数判断是否需要带上身份信息 
     * 员工点击后，页面将跳转至 redirect_uri/?code=CODE&state=STATE，企业可根据code参数获得员工的userid 
     * 
     */  
    public static String GetCode(){
    	//Oauth授权登陆重定向路径，配置为：http://您自己填写的企业号应用可信域/项目名字/定向的Servlet，
    	String REDIRECT_URI = "xxxx";
    	String code = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=CORPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&agentid=AGENTID&state=STATE#wechat_redirect";  
        String code_url = code.replaceAll("CORPID", ConstantCommon.CorpID).replaceAll("REDIRECT_URI", URLEncoder.encode(REDIRECT_URI));  
        return code_url;  
    }  
    

    /**
     * 根据code获取成员信息
     * 权限说明：
     * 	跳转的域名须完全匹配access_token对应应用的可信域名，否则会返回50001错误。
     * 	不能使用“通讯录同步助手”调用该接口。
     * 	管理员须拥有agent的使用权限；agentid必须和跳转链接时所在的企业应用ID相同 
     * @author yang.lvsen
     * @date 2018年5月16日下午5:42:56
     * @param access_token 调用接口凭证 
     * @param code 通过员工授权获取到的code，每次员工授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期 
     * @param agentid 跳转链接时所在的企业应用ID 
     * @return String
     */
    public static String GetUserID (String access_token,String code ,String agentid){
        String UserId = "";
        String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=ACCESS_TOKEN&code=CODE&agentid=AGENTID";
        postUrl = postUrl.replaceAll("ACCESS_TOKEN", access_token).replaceAll("CODE", code).replaceAll("AGENTID", agentid);
        String returnMsg = HttpClientUtil.post(postUrl, null);
        JSONObject jsonObj = JSONObject.fromObject(returnMsg);
        String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
        if("0".equals(errcode)){
            UserId = jsonObj.getString("UserId");
            if(ErpCommon.isNotNull(UserId)){
                System.out.println("获取信息成功，o(∩_∩)o ————UserID:"+UserId);
            }else{
                int errorrcode = jsonObj.getInt("errcode");
                String errmsg = jsonObj.getString("errmsg");
                System.out.println("错误码："+errorrcode+"————"+"错误信息："+errmsg);
            }
        }else{
            System.out.println("获取授权失败了，●﹏●，自己找原因。。。");
        }
        return UserId;
    }
    

}
