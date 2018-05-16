package com.alphabet.wechat.service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;

import net.sf.json.JSONObject;

/** 
 * @Title: OAuth2Service
 * @Description: 网页授权登录
 * @author yang.lvsen
 * @date 2018年5月16日 下午5:24:59
 */
public class OAuth2Service {
	
    /** 
     * 企业获取code地址处理 
     * @param appid 企业的CorpID 
     * @param redirect_uri 授权后重定向的回调链接地址，请使用urlencode对链接进行处理 
     * @param response_type 返回类型，此时固定为：code 
     * @param scope 应用授权作用域，此时固定为：snsapi_base，
     * 		snsapi_base：静默授权，可获取成员的的基础信息（UserId与DeviceId）；
     * 		snsapi_userinfo：静默授权，可获取成员的详细信息，但不包含手机、邮箱；
     * 		snsapi_privateinfo：手动授权，可获取成员的详细信息，包含手机、邮箱
     * @param agentid 企业应用的id。当scope是snsapi_userinfo或snsapi_privateinfo时，该参数必填。注意redirect_uri的域名必须与该应用的可信域名一致。
     * @param state 重定向后会带上state参数，企业可以填写a-zA-Z0-9的参数值 ，长度不可超过128个字节
     * @param #wechat_redirect 微信终端使用此参数判断是否需要带上身份信息 
     * 员工点击后，页面将跳转至 redirect_uri/?code=CODE&state=STATE，企业可根据code参数获得员工的userid 
     */  
    public static String getCode(){
    	//Oauth授权登陆重定向路径，配置为：http://您自己填写的企业号应用可信域/项目名字/定向的Servlet，
    	String REDIRECT_URI = "xxxx";
    	String code = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=CORPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&agentid=AGENTID&state=STATE#wechat_redirect";  
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
     * @return String
     */
    public static Map<String,String> getUserInfoByCode(String access_token, String code){
        Map<String,String> resultMap = new HashMap<String,String>();
        String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token=ACCESS_TOKEN&code=CODE";
        postUrl = postUrl.replaceAll("ACCESS_TOKEN", access_token).replaceAll("CODE", code);
        String returnMsg = HttpClientUtil.post(postUrl, null);
        JSONObject jsonObj = JSONObject.fromObject(returnMsg);
        String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
        if("0".equals(errcode)){
            String userId = jsonObj.getString("UserId");	//成员UserID
            if(ErpCommon.isNotNull(userId)){	//企业员工
            	System.out.println("获取信息成功，o(∩_∩)o ————UserID:"+userId);
            	resultMap.put("UserId", userId);
            	resultMap.put("DeviceId", jsonObj.getString("UserId")==null?"":jsonObj.getString("UserId"));	//手机设备号(由企业微信在安装时随机生成，删除重装会改变，升级不受影响)
            	//成员票据，最大为512字节。scope为snsapi_userinfo或snsapi_privateinfo，且用户在应用可见范围之内时返回此参数。后续利用该参数可以获取用户信息或敏感信息。
            	resultMap.put("user_ticket", jsonObj.getString("user_ticket")==null?"":jsonObj.getString("user_ticket"));
            	resultMap.put("expires_in", jsonObj.getString("expires_in")==null?"":jsonObj.getString("expires_in"));//user_ticket的有效时间（秒），随user_ticket一起返回
            }else{	//非企业员工
            	resultMap.put("OpenId", jsonObj.getString("OpenId")==null?"":jsonObj.getString("OpenId"));	//非企业成员的标识，对当前企业唯一
            	resultMap.put("DeviceId", jsonObj.getString("DeviceId")==null?"":jsonObj.getString("DeviceId"));	//手机设备号(由企业微信在安装时随机生成，删除重装会改变，升级不受影响)
            }
        }else{
            System.out.println("获取授权失败了，●﹏●，错误码："+errcode+"，错误信息："+jsonObj.getString("errmsg"));
        }
        return resultMap;
    }
    
    
    /** 
     * 使用user_ticket获取成员详情
     * 权限说明：需要有对应应用的使用权限，且成员必须在授权应用的可见范围内。
     * @author yang.lvsen
     * @date 2018年5月16日 下午8:36:49
     * @param @param userTicket
     * @return Map<String,String>
     */
    public static Map<String,String> userTicket(String userTicket){
    	Map<String,String> map = new HashMap<String,String>();
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserdetail?access_token=ACCESS_TOKEN";
			postUrl = postUrl.replaceAll("ACCESS_TOKEN", accessToken);
			JSONObject updJsonObj = new JSONObject();
			updJsonObj.put("user_ticket", userTicket);	//成员票据
			String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if("0".equals(errcode)){
				map.put("userid", jsonObj.getString("userid")==null?"":jsonObj.getString("userid").toString());
				map.put("name", jsonObj.getString("name")==null?"":jsonObj.getString("name").toString());
				map.put("mobile", jsonObj.getString("mobile")==null?"":jsonObj.getString("mobile").toString());//成员手机号，仅在用户同意snsapi_privateinfo授权时返回
				map.put("gender", jsonObj.getString("gender")==null?"":jsonObj.getString("gender").toString());//性别。0表示未定义，1表示男性，2表示女性
				map.put("email", jsonObj.getString("email")==null?"":jsonObj.getString("email").toString());//成员邮箱，仅在用户同意snsapi_privateinfo授权时返回
				map.put("avatar", jsonObj.getString("avatar")==null?"":jsonObj.getString("avatar").toString());//头像url。注：如果要获取小图将url最后的”/0”改成”/100”即可
				map.put("qr_code", jsonObj.getString("qr_code")==null?"":jsonObj.getString("qr_code").toString());//员工个人二维码（扫描可添加为外部联系人），仅在用户同意snsapi_privateinfo授权时返回
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return map;
    }
    

}
