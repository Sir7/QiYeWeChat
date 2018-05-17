package com.alphabet.wechat.common;

import java.util.ArrayList;
import java.util.List;

import com.alphabet.common.ErpCommon;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author yang.lvsen
 * @date 2018年1月16日 下午12:49:42
 */
public class WeChatServer {
	
	/** 
	 * 获取access_token
	 * @author yang.lvsen
	 * @date 2018年1月4日 下午4:29:29
	 * @param corpId
	 * @param corpSecret
	 * @return String
	 * @throws
	 */
	public static String getToken(String corpId,String corpSecret) throws Exception {
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpId+"&corpsecret="+corpSecret;
		//调用企业微信API接口
		String returnMsg = HttpClientUtil.post(postUrl, null); 
		System.out.println(returnMsg);
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		String accessToken = jsonObj.getString("access_token") == null ? "" : jsonObj.getString("access_token").toString();
		return accessToken;
	}
	
	/** 
	 * 获取企业微信服务器的ip段
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午8:17:14
	 * @return List<String>
	 * @throws
	 */
	public static List<String> WeChatIP(String corpId,String corpSecret) throws Exception{
		List<String> ipList = new ArrayList<String>();
		String accessToken = getToken(corpId, corpSecret);
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/getcallbackip?access_token="+accessToken;
		//调用企业微信API接口
		String returnMsg = HttpClientUtil.post(postUrl, null);
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		String errcode = jsonObj.getString("errcode") == null ? "" : jsonObj.getString("errcode").toString();
		if("0".equals(errcode)){
			JSONArray ipJsonList = jsonObj.getJSONArray("ip_list") == null ? null : jsonObj.getJSONArray("ip_list");
			for(int i = 0;i < ipJsonList.size();i++){
				JSONObject data = ipJsonList.getJSONObject(i);
				ipList.add(data.get("ip_list")==null?"":data.get("ip_list").toString());
			}
		}
		return ipList;
	}
	
	/**
	 * 
	 * userId，openId互相转换
	 * @author yang.lvsen
	 * @date 2018年5月8日 下午8:55:35
	 * @param corpId
	 * @param corpSecret
	 * @param id	userid/openid
	 * @param Type	传入的是什么数据，如果是userid，就填写U，反之则填写O
	 * @throws Exception 
	 * @return String
	 */
	public static String UserIdAndOpenIdConversion(String corpId,String corpSecret,String id,String Type) throws Exception{
		String accessToken = getToken(corpId, corpSecret);
		String postUrl = "";
		String result = "";
		JSONObject updJsonObj = new JSONObject();
		if(ErpCommon.isNotNull(Type)){
			if("U".equals(Type)){	//传递的参数是userid，求openid
				updJsonObj.put("userid", id);
				postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/convert_to_openid?access_token="+accessToken;
			}else if("O".equals(Type)){
				updJsonObj.put("openid", id);
				postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/convert_to_userid?access_token="+accessToken;
			}
		}
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		String errcode = jsonObj.getString("errcode") == null ? "" : jsonObj.getString("errcode").toString();
		if("0".equals(errcode)){
			if(ErpCommon.isNotNull(Type)){
				if("U".equals(Type)){
					result = jsonObj.getString("openid") == null ? "" : jsonObj.getString("openid").toString();
				}else if("O".equals(Type)){
					result = jsonObj.getString("userid") == null ? "" : jsonObj.getString("userid").toString();
				}
			}
		}else{
			System.out.println("-------转换失败---失败原因："+jsonObj.getString("errmsg"));
		}
		return result;
	}

	
}
