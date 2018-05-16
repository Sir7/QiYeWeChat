package com.alphabet.wechat.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.aes.AesException;
import com.aes.WXBizMsgCrypt;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;

import net.sf.json.JSONObject;

/** 
* @Title: SendMessageToWeChat 
* @Description: 企业与企业微信回复信息
* @author yang.lvsen
* @date 2018年4月25日 下午9:30:36 
*  
*/
public class SystemSendMsgToWeChat {
	
	//token，可自定义也可随机生成
	private final static String token = "abcdefghijklmnopqrstuvwxyz";
	//encodingAESKey,随机生成
	private final static String encodingAESKey = "z2W9lyOAR1XjY8mopEmiSqib0TlBZzCFiCLp6IdS2Iv";
	//企业微信企业唯一标识ID
	private static final String corpId = "ww4fa9d32099c5d219";
	//应用的凭证密钥
	private static final String corpSecret = "_d5_ktgj2UFwXndJXMiRCua8D3iIknVmmMsHAZ1j2HU";
	
	
	
	/** 
	 * @Description: 企业后台主动向企业微信中的自建/第三方应用发送信息
	 * @author yang.lvsen
	 * @date 2018年5月4日 下午4:25:40
	 * @param list 由企业后台系统页面所需要发送的信息集合
	 * @return void
	 * @throws
	 */
	public void ActiveResponseInformation(List<Map<String,Object>> list) throws Exception {
		String accessToken = WeChatServer.getToken(corpId, corpSecret); // 获取access token
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="+accessToken;
		String returnMsg = HttpClientUtil.post(postUrl, null);
		if(list != null && list.size() > 0){
			JSONObject updJsonObj = new JSONObject();
			//接收者userid，这里可以是前端发送者勾选的的接收人，这样需要用“|”来分割userid；也可以是企业所有人，那么可以直接用“@all”代替
			//大多数情况是全体成员接收，这里我们使用@all，touser，toparty，totag三者不能同时为空！！！
			updJsonObj.put("touser", "@all");	//接收者，全体成员，选填。
			String msgtype = list.get(0).get("msgtype").toString();
			//updJsonObj.put("toparty", "@all");	//接受部门，当接收者是全体成员时，这个参数会忽略
			//updJsonObj.put("totag", "@all");	//接收信息的标签，当接收者是全体成员时，这个参数会忽略
			updJsonObj.put("msgtype", msgtype);	//消息类型。必填
			if("text".equals(msgtype)){		//文本信息
				updJsonObj.put("content", "这是一条测试信息，收到请忽略！\n测试该链接<a href=\"www.baidu.com\">点我试试<a/>");	//消息类型。必填
			}else if("image".equals(msgtype) || "voice".equals(msgtype) || "file".equals(msgtype)){		//图片信息/语音信息/文件信息
				//要先获取临时素材
				String mediaId = MaterialManagementService.uploadMedia(accessToken, msgtype, "文件所在的路径");
				updJsonObj.put("media_id", mediaId);
			}else if("video".equals(msgtype)){		//视频信息
				//要先获取临时素材
				String mediaId = MaterialManagementService.uploadMedia(accessToken, msgtype, "文件所在的路径");
				updJsonObj.put("media_id", mediaId);
				updJsonObj.put("title", list.get(0).get("title").toString());
				updJsonObj.put("description", list.get(0).get("description").toString());
			}else if("textcard".equals(msgtype)){		//文本卡片信息
				updJsonObj.put("title", list.get(0).get("title").toString());
				updJsonObj.put("description", list.get(0).get("description").toString());
				updJsonObj.put("url", list.get(0).get("url").toString());
				updJsonObj.put("btntxt", list.get(0).get("btntxt").toString());
			}else if("news".equals(msgtype) || "mpnews".equals(msgtype)){		//图文信息
				updJsonObj.put("articles", list);
			}
			updJsonObj.put("agentid", "1000002");	//应用id。必填
			JSONObject msgObj = JSONObject.fromObject(returnMsg);
			if(ErpCommon.isNotNull("errcode") && "0".equals(msgObj.getString("errcode"))){
				/*String invaliduser = msgObj.getString("invaliduser");
				String[] invaliduserArr = invaliduser.split("|");	//获取userid数组
				String invalidparty = msgObj.getString("invalidparty");
				String[] invalidpartyArr = invalidparty.split("|");
				String invalidtag = msgObj.getString("invalidtag");
				String[] invalidtagArr = invalidtag.split("|");*/
			}
		}
	}
	
	
	/** 
	 * @Description: 企业后台接收到微信服务器发来的信息，被动回复该信息。
	 * @author yang.lvsen
	 * @date 2018年5月7日 下午5:05:37
	 * @return void
	 * @throws
	 */
	public void PassiveResponseInformation(){
		/**
		 * 这里可以先接收微信服务器发送过来的加密信息，再将密文解密，在对信息进行回复。这里我们就不写如何获取服务器发送信息，并解析信息。直接写如何恢复信息的过程
		 * 企业被动回复用户的消息也需要进行加密，并且拼接成密文格式的xml串。
			假设企业需要回复用户的明文如下：
			<xml>
			<ToUserName><![CDATA[mycreate]]></ToUserName>
			<FromUserName><![CDATA[wx5823bf96d3bd56c7]]></FromUserName>
			<CreateTime>1348831860</CreateTime>
			<MsgType><![CDATA[text]]></MsgType>
			<Content><![CDATA[this is a test]]></Content>
			<MsgId>1234567890123456</MsgId>
			<AgentID>128</AgentID>
			</xml>
	
			为了将此段明文回复给用户，企业应：
			1.自己生成时间时间戳(timestamp),随机数字串(nonce)以便生成消息体签名，也可以直接用从企业微信的post url上解析出的对应值。
			2.将明文加密得到密文。	
			3.用密文，步骤1生成的timestamp,nonce和企业在企业微信设定的token生成消息体签名。
			4.将密文，消息体签名，时间戳，随机数字串拼接成xml格式的字符串，发送给企业。
			以上2，3，4步可以用企业微信提供的库函数EncryptMsg来实现。
		 */
		String toUserName = "";		//接收者的userid
		String fromUserNmae = corpId;	//企业微信的CorpId
		Long createTime = System.currentTimeMillis();		//消息的创建时间
		String msgType = "text";	//消息类型，有text,images,video,
		String content = "this is a test";	//消息内容
		String media_id = "";
		if("images".equals(msgType) || "voice".equals(msgType) || "video".equals(msgType)){
			media_id = "zaqwsxcderfvbgtyhn";
		}
		String vedio = "";
		if("video".equals(msgType)){
			String title = "测试";
			String dedcription = "描述";
			vedio = "<Video>"
				       +"<MediaId><![CDATA["+media_id+"]]></MediaId>"
				       +"<Title><![CDATA["+title+"]]></Title>"
				     +"  <Description><![CDATA["+dedcription+"]]></Description>"
				   +"</Video>";
		}
		String items = "";
		if("news".equals(msgType)){			//图文信息
			String title = "测试";
			String dedcription = "描述";
			String picurl = "";
			String url = "";
			items = "<item>"
		           +"<Title><![CDATA["+title+"]]></Title> "
		           +"<Description><![CDATA["+dedcription+"]]></Description>"
		           +"<PicUrl><![CDATA["+picurl+"]]></PicUrl>"		//图文消息的图片链接
		           +"<Url><![CDATA["+url+"]]></Url>"		//点击后跳转的链接
		       +"</item>";
		}
		
		//回复的信息
		String sRespData = "<xml>"
				+ "<ToUserName><![CDATA[mycreate]]></ToUserName>"
				+ "<FromUserName><![CDATA["+fromUserNmae+"]]></FromUserName>"
				+ "<CreateTime>"+createTime+"</CreateTime>"
				+ "<MsgType><![CDATA["+msgType+"]]></MsgType>"
				+ "<MediaId><![CDATA["+media_id+"]]></MediaId>"	//当msgType是images是才使用该属性
				+ "<Content><![CDATA["+content+"]]></Content>"
				+ "</xml>";
		try {
			WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token, encodingAESKey, corpId);
			//加密后的信息
			String sEncryptMsg = wxcpt.EncryptMsg(sRespData, String.valueOf(createTime), String.valueOf(UUID.randomUUID()));
			//加密后就将信息发送给微信服务器。。。
			
		} catch (AesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
