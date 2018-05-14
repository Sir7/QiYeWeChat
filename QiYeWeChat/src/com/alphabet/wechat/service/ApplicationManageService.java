package com.alphabet.wechat.service;

import java.util.HashMap;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Title: ApplicationManageService
 * @Description: 应用管理
 * @author yang.lvsen
 * @date 2018年5月14日 下午5:10:18
 */
public class ApplicationManageService {
	
	public static Map<String,String> getApplicationInfo(String agentId){
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/agent/get?access_token="+accessToken+"&agentid="+agentId;
			String returnMsg = HttpClientUtil.post(postUrl, null);
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				Map<String,String> checkDatamap = new HashMap<String,String>();
				checkDatamap.put("agentid", jsonObj.getString("agentid")==null?"":jsonObj.getString("agentid").toString());
				checkDatamap.put("name", jsonObj.getString("name")==null?"":jsonObj.getString("name").toString());
				checkDatamap.put("square_logo_url", jsonObj.getString("square_logo_url")==null?"":jsonObj.getString("square_logo_url").toString());
				checkDatamap.put("description", jsonObj.getString("description")==null?"":jsonObj.getString("description").toString());
				String allow_userinfos = jsonObj.getString("allow_userinfos")==null?"":jsonObj.getString("allow_userinfos").toString();
				JSONArray jsonDataArr = jsonObj.getJSONArray("allow_userinfos");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
