package com.alphabet.wechat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.UploadAndDownloadMedia;
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
	
	/**  
	 * 获取应用
	 * 企业仅可获取当前凭证对应的应用；第三方仅可获取被授权的应用。
	 * @author yang.lvsen
	 * @date 2018年5月15日下午2:11:34
	 * @param agentId 应用id
	 * @return Map<String,Object>
	 */ 
	public static Map<String,Object> getApplicationInfo(String agentId){
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/agent/get?access_token="+accessToken+"&agentid="+agentId;
			String returnMsg = HttpClientUtil.post(postUrl, null);
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				Map<String,Object> checkDatamap = new HashMap<String,Object>();
				checkDatamap.put("agentid", jsonObj.getString("agentid")==null?"":jsonObj.getString("agentid").toString());//企业应用id
				checkDatamap.put("name", jsonObj.getString("name")==null?"":jsonObj.getString("name").toString());//企业应用名称
				checkDatamap.put("square_logo_url", jsonObj.getString("square_logo_url")==null?"":jsonObj.getString("square_logo_url").toString());//企业应用方形头像
				checkDatamap.put("description", jsonObj.getString("description")==null?"":jsonObj.getString("description").toString());//企业应用详情
				String allowUserinfosJson = jsonObj.getString("allow_userinfos")==null?"":jsonObj.getString("allow_userinfos").toString();//企业应用可见范围（人员），其中包括userid
				JSONObject allowUserinfosjsonObj = JSONObject.fromObject(allowUserinfosJson);
				JSONArray allowUserinfosArr = allowUserinfosjsonObj.getJSONArray("user");
				List<String> userList = new ArrayList<String>();
				for(int i=0;i<allowUserinfosArr.size();i++){
					JSONObject data = allowUserinfosArr.getJSONObject(i);
					String userId = data.get("userid")==null?"":data.get("userid").toString();
					userList.add(userId);
				}
				checkDatamap.put("allow_userinfos", userList);
				String allow_partys = jsonObj.getString("allow_partys")==null?"":jsonObj.getString("allow_partys").toString();
				JSONObject allowPartysjsonObj = JSONObject.fromObject(allow_partys);
				JSONArray allowartysList = allowPartysjsonObj.getJSONArray("partyid");
				List<String> partyList = new ArrayList<String>();
				for(int i=0;i<allowartysList.size();i++){
					JSONObject data = allowartysList.getJSONObject(i);
					String userId = data.get("userid")==null?"":data.get("userid").toString();
					partyList.add(userId);
				}
				checkDatamap.put("allow_partys", partyList);//企业应用可见范围（部门）
				String allow_tags = jsonObj.getString("allow_tags")==null?"":jsonObj.getString("allow_tags").toString();
				JSONObject allowTagsjsonObj = JSONObject.fromObject(allow_tags);
				JSONArray tagidList = allowTagsjsonObj.getJSONArray("tagid");
				List<String> tagIdList = new ArrayList<String>();
				for(int i=0;i<tagidList.size();i++){
					JSONObject data = tagidList.getJSONObject(i);
					String userId = data.get("tagid")==null?"":data.get("tagid").toString();
					tagIdList.add(userId);
				}
				checkDatamap.put("allow_tags", tagIdList);//企业应用可见范围（标签）
				checkDatamap.put("close", "0");//企业应用是否被禁用
				checkDatamap.put("redirect_domain", "www.qq.com");//企业应用可信域名
				checkDatamap.put("report_location_flag", "0");//企业应用是否打开地理位置上报 0：不上报；1：进入会话上报；
				checkDatamap.put("isreportenter", "0");//是否上报用户进入应用事件。0：不接收；1：接收
				checkDatamap.put("home_url", "http://www.qq.com");//应用主页url
				return checkDatamap;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**  
	 * 设置应用
	 * 仅企业可调用，可设置当前凭证对应的应用；第三方不可调用。
	 * @author yang.lvsen
	 * @date 2018年5月15日下午2:28:19
	 * @param agentId 应用id
	 * @return String
	 */ 
	public static String setApplication(String agentId){
		//先获取应用在设置应用
		Map<String,Object> resultMap = getApplicationInfo(agentId);
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/agent/set?access_token="+accessToken;
			JSONObject updJsonObj = new JSONObject();
			updJsonObj.put("agentid", agentId);	//获取全部打卡类型
			updJsonObj.put("report_location_flag", "0");
			String mediaId = UploadAndDownloadMedia.uploadMedia(accessToken, "image", "图片在本地的路径");
			updJsonObj.put("logo_mediaid", mediaId);
			if(!resultMap.isEmpty()){
				updJsonObj.put("name", resultMap.get("name") == null ? "" : resultMap.get("name").toString());
				updJsonObj.put("description",  resultMap.get("description") == null ? "" : resultMap.get("description").toString());
				updJsonObj.put("redirect_domain",  resultMap.get("redirect_domain") == null ? "" : resultMap.get("redirect_domain").toString());
				updJsonObj.put("isreportenter",  resultMap.get("isreportenter") == null ? "" : resultMap.get("isreportenter").toString());
				updJsonObj.put("home_url",  resultMap.get("home_url") == null ? "" : resultMap.get("home_url").toString());
			}
			String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				return "0";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**  
	 * 获取应用列表
	 * 企业仅可获取当前凭证对应的应用；第三方仅可获取被授权的应用。
	 * @author yang.lvsen
	 * @date 2018年5月15日下午2:35:20
	 * @return List<Map<String,Object>>
	 */ 
	public static List<Map<String,Object>> getApplicationList(){
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/agent/list?access_token="+accessToken;
			String returnMsg = HttpClientUtil.post(postUrl, null);
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				JSONArray jsonDataArr = jsonObj.getJSONArray("agentlist");
				for(int i=0;i<jsonDataArr.size();i++){
					Map<String,Object> map = new HashMap<String,Object>();
					JSONObject data = jsonDataArr.getJSONObject(i);
					map.put("agentid", data.get("agentid")==null?"":data.get("agentid").toString());//企业应用id
					map.put("name", data.get("name")==null?"":data.get("name").toString());//企业应用名称
					map.put("square_logo_url", data.get("square_logo_url")==null?"":data.get("square_logo_url").toString());//企业应用方形头像url
					resultList.add(map);
				}
				return resultList;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String customMenu(){
		
		
		
		return null;
	}

}
