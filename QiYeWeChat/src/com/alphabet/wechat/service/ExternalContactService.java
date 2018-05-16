package com.alphabet.wechat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Title: ExternalContactService
 * @Description: 外部联系人管理
 * @author yang.lvsen
 * @date 2018年5月16日 下午2:44:22
 */
public class ExternalContactService {
	
	// 获取access_token
	public static String getToken() throws Exception {
		return WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.ExternalContactSecret); 
	}

	/**  
	 * 获取企业客户列表
	 * 通过userid获取企业成员在企业微信中添加的所有标记为企业客户的外部联系人。
	 * @author yang.lvsen
	 * @date 2018年5月16日下午3:18:56
	 * @param userId	员工userid
	 * @return List<Map<String,Object>>
	 */ 
	public static List<Map<String,Object>> getExternalContact(String userId){
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			/**
			 * 需要使用”外部联系人”的accesstoken调用，并且userid不在限制名单内。注意：只有在被标记为企业客户的情况下,接口才会返回相应的成员信息
			 */
			String accessToken = getToken();
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/crm/get_external_contact_list?access_token="+accessToken+"&userid="+userId;
			String returnMsg = HttpClientUtil.post(postUrl, null);
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				JSONArray jsonDataArr = jsonObj.getJSONArray("contact");	//成员的外部联系人列表，包含外部微信用户以及外部企业微信用户两种类型
				return getConcat(jsonDataArr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultList;
	}
	
	
	/**  
	 * 关联企业客户库
	 * @author yang.lvsen
	 * @date 2018年5月16日下午3:28:09
	 * @param code
	 * @return List<Map<String,Object>>
	 */ 
	public static List<Map<String,Object>> AssociatedEnterpriseCustomerBase(String code){
		/**
		 * 企业可以快捷的将企业的已有的企业客户库接入到企业微信内，方便企业成员查看外部联系人信息页时可快速进入企业的客户库。如需使用，需进行以下几步配置：
		 * 1、在管理后台配置企业客户库的url
		 * 2、配置后，成员在外部联系人信息页点击查看详情时，企业微信会颁发一个code并跳转到企业客户库的url，例如：https://crm.qq.com?code=CODE， 企业可以通过该code调用以下接口获取该成员的外部联系人信息。
		 */
		try {
			String accessToken = getToken();
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/crm/get_external_contact?access_token="+accessToken+"&code="+code;
			String returnMsg = HttpClientUtil.post(postUrl, null);
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				JSONArray jsonDataArr = jsonObj.getJSONArray("contact");	//成员的外部联系人列表，包含外部微信用户以及外部企业微信用户两种类型
				return getConcat(jsonDataArr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 读取返回的外部联系人列表
	 * @author yang.lvsen
	 * @date 2018年5月16日下午3:27:35
	 * @param jsonDataArr
	 * @return List<Map<String,Object>>
	 */
	public static List<Map<String,Object>> getConcat(JSONArray jsonDataArr){
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		for(int i=0;i<jsonDataArr.size();i++){
			JSONObject data = jsonDataArr.getJSONObject(i);
			Map<String,Object> externalMap = new HashMap<String,Object>();
			externalMap.put("openid", data.get("openid")==null?"":data.get("openid").toString());//外部联系人的openid
			String createtime = data.get("createtime")==null?"":data.get("createtime").toString();//添加外部联系人的时间
			createtime = ErpCommon.Timestamp2DateTime(Long.parseLong(createtime));
			externalMap.put("createtime",createtime);
			externalMap.put("name", data.get("name")==null?"":data.get("name").toString());//外部联系人的姓名
			externalMap.put("remark", data.get("remark")==null?"":data.get("remark").toString());//对外部联系人的备注
			externalMap.put("description", data.get("description")==null?"":data.get("description").toString());//对外部联系人的描述
			externalMap.put("avatar", data.get("avatar")==null?"":data.get("avatar").toString());//外部联系人头像
			externalMap.put("type", data.get("type")==null?"":data.get("type").toString());//外部联系人的类型，1表示该外部联系人是微信用户（暂时仅内测企业有此类型），2表示该外部联系人是企业微信用户
			externalMap.put("gender", data.get("gender")==null?"":data.get("gender").toString());//外部联系人的性别 0-未知 1-男性 2-女性
			externalMap.put("mobile", data.get("mobile")==null?"":data.get("mobile").toString());//外部联系人的手机号码，如果外部企业或用户选择隐藏手机号，则不返回，仅当联系人类型是企业微信用户时有此字段；不支持第三方获取该字段
			externalMap.put("email", data.get("email")==null?"":data.get("email").toString());//外部联系人的电子邮箱地址，如果外部企业或用户选择隐藏邮箱，则不返回，仅当联系人类型是企业微信用户时有此字段；不支持第三方获取该字段
			externalMap.put("position", data.get("position")==null?"":data.get("position").toString());//外部联系人的职位，如果外部企业或用户选择隐藏职位，则不返回，仅当联系人类型是企业微信用户时有此字段
			externalMap.put("corp_name", data.get("corp_name")==null?"":data.get("corp_name").toString());//外部联系人所在企业的简称，仅当联系人类型是企业微信用户时有此字段
			externalMap.put("corp_full_name", data.get("corp_full_name")==null?"":data.get("corp_full_name").toString());//外部联系人所在企业的主体名称，仅当联系人类型是企业微信用户时有此字段
			String external_profile = data.get("external_profile")==null?"":data.get("external_profile").toString();//外部联系人的自定义展示信息，可以有多个字段和多种类型，包括文本，网页和小程序，仅当联系人类型是企业微信用户时有此字段，字段详情见对外属性
			JSONObject externalProfileObj = JSONObject.fromObject(external_profile);
			JSONArray externalAttr = externalProfileObj.getJSONArray("external_attr");
			for(int j = 0;j < externalAttr.size(); j++){
				Map<String,Object> externalProfileMap = new HashMap<String,Object>();
				JSONObject external = jsonDataArr.getJSONObject(j);
				externalProfileMap.put("type", external.get("type")==null?"":external.get("type").toString());
				externalProfileMap.put("name", external.get("name")==null?"":external.get("name").toString());
				String text = external.get("text")==null?"":external.get("text").toString();
				if(ErpCommon.isNotNull(text)){
					JSONObject textObj = JSONObject.fromObject(text);
					String value = textObj.getString("value")==null?"":textObj.getString("value").toString();
					externalProfileMap.put("value",value);
				}
				
				String web = external.get("web")==null?"":external.get("web").toString();
				if(ErpCommon.isNotNull(web)){
					JSONObject webObj = JSONObject.fromObject(web);
					String url = webObj.getString("url")==null?"":webObj.getString("url").toString();
					String title = webObj.getString("title")==null?"":webObj.getString("title").toString();
					externalProfileMap.put("url", url);
					externalProfileMap.put("title", title);
				}

				String miniprogram = external.get("miniprogram")==null?"":external.get("miniprogram").toString();
				if(ErpCommon.isNotNull(miniprogram)){
					JSONObject miniprogramObj = JSONObject.fromObject(miniprogram);
					String appid = miniprogramObj.getString("appid")==null?"":miniprogramObj.getString("appid").toString();
					String pagepath = miniprogramObj.getString("pagepath")==null?"":miniprogramObj.getString("pagepath").toString();
					String title = miniprogramObj.getString("title")==null?"":miniprogramObj.getString("title").toString();
					externalProfileMap.put("appid", appid);
					externalProfileMap.put("pagepath", pagepath);
					externalProfileMap.put("title", title);
				}
				externalMap.put("externalProfileMap", externalProfileMap);
			}
			resultList.add(externalMap);
		}
		return resultList;
	}
}
