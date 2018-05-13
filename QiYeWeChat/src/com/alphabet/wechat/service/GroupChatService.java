package com.alphabet.wechat.service;

import java.util.List;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Title: GroupChatService 
 * @Description: 群聊
 * @author yang.lvsen
 * @date 2018年5月7日 下午5:12:10 
 *  
 */
public class GroupChatService {
	
	// 获取access_token
	public static String getToken() throws Exception {
		return WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.GroupChatSecret); 
	}
	
	/** 
	 * 创建群聊会话
	 * @author yang.lvsen
	 * @date 2018年5月7日 下午5:24:57
	 * @param map	页面填写的群聊相关数据，通过map集合传递这个方法，通过接口，将企业后台添加的群聊上传至微信服务器。
	 * @throws Exception 
	 * @return String
	 */
	public static String createGroupChat(Map<String,Object> map) throws Exception{
		String accessToken = getToken();
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/appchat/create?access_token="+accessToken;
		JSONObject newJsonObj = new JSONObject();
		newJsonObj.put("name", map.get("name"));		//群聊名
		newJsonObj.put("owner", map.get("owner"));	//群主，指定的群主id
		newJsonObj.put("userlist", map.get("userlist"));		//成员userid，至少两人之多500人
		newJsonObj.put("chatid", map.get("chatid"));		//群聊的唯一标志，不能与已有的群重复；字符串类型，最长32个字符。只允许字符0-9及字母a-zA-Z。如果不填，系统会随机生成群id
		String returnMsg = HttpClientUtil.post(postUrl, newJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		String chatId = msgObj.getString("chatid");
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		System.out.println("---新增异常："+map.get("name").toString()+"  "+msgObj.toString());
		return msgObj.getString("errmsg");
	}
	
	/** 
	 * 修改群聊会话
	 * @author yang.lvsen
	 * @date 2018年5月7日 下午5:31:02
	 * @param map
	 * @throws Exception 
	 * @return String
	 */
	public static String updateGroupChat(Map<String,Object> map) throws Exception{
		String accessToken = getToken();
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/appchat/update?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		updJsonObj.put("chatid", map.get("chatid").toString());
		updJsonObj.put("name", map.get("name"));		//新的群聊名。若不需更新，请忽略此参数
		updJsonObj.put("owner", map.get("owner"));	//新群主的id。若不需更新，请忽略此参数
		updJsonObj.put("add_user_list", map.get("add_user_list"));	//添加成员的id列表
		updJsonObj.put("del_user_list", map.get("del_user_list"));	//踢出成员的id列表
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		System.out.println("---更新异常："+map.get("name").toString()+"  "+msgObj.toString());
		return msgObj.getString("errmsg");
	}
	
	
	/** 
	 * 获取群聊会话
	 * @author yang.lvsen
	 * @date 2018年5月7日 下午5:46:36
	 * @throws Exception 
	 * @return String
	 */
	public static String getGroupChatConversation() throws Exception{
		String accessToken = getToken();
		String chatId = "";	//群聊id
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/appchat/get?access_token=ACCESS_TOKEN&chatid=CHATID";
		postUrl = postUrl.replace("ACCESS_TOKEN", accessToken).replace("CHATID", chatId);
		String returnMsg = HttpClientUtil.post(postUrl, null);
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		String errcode = jsonObj.getString("errcode") == null? "" : jsonObj.getString("errcode").toString();
		if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
			JSONArray jsonDataArr = jsonObj.getJSONArray("chat_info");	//群聊信息
			for(int i=0;i<jsonDataArr.size();i++){
				JSONObject data = jsonDataArr.getJSONObject(i);
				//String chatid = data.get("chatid")==null?"":data.get("chatid").toString();
				String name = data.get("name")==null?"":data.get("name").toString();
				String owner = data.get("owner")==null?"":data.get("owner").toString();
				List userlist = (List)(data.get("userlist")==null?"":data.get("userlist"));
				/**
				 * 后面再将信息经过处理，返回到企业后台的前端。。。。
				 */
			}
		}
		return null;
	}

}
