package com.alphabet.wechat.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aes.AesException;
import com.aes.WXBizMsgCrypt;
import com.alphabet.wechat.common.WeChatServer;
import com.alphabet.wechat.util.PropertyUtil;

/** 
 * @Title: WeChatToSystem 
 * @Description: 企业后台接收企业微信发送的信息
 * @author yang.lvsen
 * @date 2018年4月26日 下午2:58:50 
 *  
 */
public class WeChatSendMsgToSystem {
	
	//token，可自定义也可随机生成
	private final static String token = "abcdefghijklmnopqrstuvwxyz";
	//encodingAESKey,随机生成
	private final static String encodingAESKey = "z2W9lyOAR1XjY8mopEmiSqib0TlBZzCFiCLp6IdS2Iv";
	//企业ID
	private final static String corpId = "ww4fa9d32099c5d219";
	//应用的凭证密钥
	private final static String corpSecret = "_d5_ktgj2UFwXndJXMiRCua8D3iIknVmmMsHAZ1j2HU";
	
	//媒体文件下载保存路径
	private static String downloadLocal = PropertyUtil.getByPropName("myapp", "download.medias.local");

	// 获取access_token
	public static String getToken() throws Exception {
		return WeChatServer.getToken(corpId, corpSecret); 
	}
	
	/** 
	 * 验证回调URL
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午4:30:06
	 * @param msgSignature 企业微信加密签名
	 * @param timestamp 时间戳
	 * @param nonce 随机数
	 * @param echostr 加密的字符串
	 * @return String
	 * @throws
	 */
	public static String VerifyURL(HttpServletRequest request) {
		// 微信加密签名  
        String msg_signature = request.getParameter("msg_signature");
        // 时间戳  
        String timestamp = request.getParameter("timestamp");
        // 随机数  
        String nonce = request.getParameter("nonce");
        // 随机字符串  
        String echostr = request.getParameter("echostr");
        
        String sEchoStr = "";
		if(msg_signature != null && !"".equals(msg_signature)){
			try {
				WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token, encodingAESKey, corpId);
				sEchoStr = wxcpt.VerifyURL(msg_signature, timestamp, nonce, echostr);  //验证请求路径
			} catch (AesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return sEchoStr;
	}
	
	
	/** 
	 * 企业微信将用户发送的信息转发至企业后台
	 * @author yang.lvsen
	 * @date 2018年4月27日 下午4:34:58
	 * @param request 接收到微信服务器发送过来的加密信息
	 * @return String
	 * @throws
	 */
	public static String getInformation(HttpServletRequest request){
		//1、解析出url上的参数，包括消息体签名(msg_signature)，时间戳(timestamp)以及随机数字串(nonce)
		//微信加密签名  
        String msg_signature = request.getParameter("msg_signature");
        //时间戳  
        String timestamp = request.getParameter("timestamp");
        //随机数  
        String nonce = request.getParameter("nonce");
        //post请求的密文数据
        String data = request.getParameter("data");
        
		/**
		 * 2.验证消息体签名的正确性。
		 * 3.将post请求的数据进行xml解析，并将<Encrypt>标签的内容进行解密，解密出来的明文即是用户回复消息的明文，明文格式请参考官方文档
		 * 第2，3步可以用企业微信提供的库函数DecryptMsg来实现。
		 */
        String sMsg = null;
		try {
			//解密企业微信发送来的密文信息。
			WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token, encodingAESKey, corpId);
			//解密后的信息
			sMsg = wxcpt.DecryptMsg(msg_signature, timestamp, nonce, data);
		} catch (AesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sMsg;
	}
	
	
	/** 
	 * 用户发送至企业微信服务器，企业微信服务器再将信息加密转发至企业后台。
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午9:35:15
	 * @param request 
	 * @return void
	 * @throws
	 */
	public static void ReceiveInformation(HttpServletRequest request) {
		//先获取企业微信发送至企业后台的加密信息
		String sMsg = getInformation(request);
		
		//解析出明文xml标签的内容进行处理，使用xml解析，将信息解析出来
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader sr = new StringReader(sMsg);
			InputSource is = new InputSource(sr);
			Document document = db.parse(is);
			Element root = document.getDocumentElement();
			NodeList CorpIDList = root.getElementsByTagName("ToUserName");	//企业微信CorpID
			String CorpID = CorpIDList.item(0).getTextContent();
			NodeList fromUserIdList = root.getElementsByTagName("FromUserName");	//发送者userid
			String fromUserId = fromUserIdList.item(0).getTextContent();
			NodeList createTimeList = root.getElementsByTagName("CreateTime");	//消息发送的时间
			String createTime = createTimeList.item(0).getTextContent();
			NodeList msgTypeList = root.getElementsByTagName("MsgType");	//消息类型
			String msgType = msgTypeList.item(0).getTextContent();
			if("text".equals(msgType)){		//文本信息
				NodeList contentList = root.getElementsByTagName("Content");	//消息内容
				String content = contentList.item(0).getTextContent();
			}else if("image".equals(msgType)){		//图片信息
				NodeList picUrlList = root.getElementsByTagName("PicUrl");	//图片链接
				String picUrl = picUrlList.item(0).getTextContent();
				NodeList MediaIdList = root.getElementsByTagName("MediaId");	//图片媒体id
				String mediaId = MediaIdList.item(0).getTextContent();
				String filePath = MaterialManagementService.downloadMedia(getToken(), mediaId, downloadLocal,null);	//下载下来的媒体文件保存路径
			}else if("voice".equals(msgType)){		//语音信息
				NodeList mediaIdList = root.getElementsByTagName("MediaId");	//语音媒体id
				String mediaId = mediaIdList.item(0).getTextContent();
				String filePath = MaterialManagementService.downloadMedia(getToken(), mediaId, downloadLocal,"HD");	//下载下来的媒体文件保存路径
				NodeList formatList = root.getElementsByTagName("Format");	//语音格式
				String format = formatList.item(0).getTextContent();
			}else if("video".equals(msgType)){		//视频信息
				NodeList mediaIdList = root.getElementsByTagName("MediaId");	//视频媒体id
				String mediaId = mediaIdList.item(0).getTextContent();
				String filePath = MaterialManagementService.downloadMedia(getToken(), mediaId, downloadLocal,null);	//下载下来的媒体文件保存路径
				NodeList thumbMediaIdList = root.getElementsByTagName("ThumbMediaId");	//视频媒体缩略图id
				String thumbMediaId = thumbMediaIdList.item(0).getTextContent();
				String filePath2 = MaterialManagementService.downloadMedia(getToken(), thumbMediaId, downloadLocal,null);	//下载下来的媒体文件保存路径
			}else if("location".equals(msgType)){		//位置信息
				NodeList location_XList = root.getElementsByTagName("Location_X");	//纬度
				String location_X = location_XList.item(0).getTextContent();
				NodeList location_YList = root.getElementsByTagName("Location_Y");	//经度
				String location_Y = location_YList.item(0).getTextContent();
				NodeList scaleList = root.getElementsByTagName("Scale");	//缩放大小
				String scale = scaleList.item(0).getTextContent();
				NodeList labelList = root.getElementsByTagName("Label");	//缩放大小
				String label = labelList.item(0).getTextContent();		//位置信息
			}else if("link".equals(msgType)){		//链接信息
				NodeList titleList = root.getElementsByTagName("Title");	//纬度
				String title = titleList.item(0).getTextContent();
				NodeList descriptionList = root.getElementsByTagName("Description");	//经度
				String description = descriptionList.item(0).getTextContent();
				NodeList picUrlList = root.getElementsByTagName("PicUrl");	//封面缩略图的url
				String picUrl = picUrlList.item(0).getTextContent();
			}
			NodeList msgIdList = root.getElementsByTagName("MsgId");	//消息id
			String msgId = msgIdList.item(0).getTextContent();
			NodeList agentIdList = root.getElementsByTagName("AgentID");	//应用id
			String agentId = agentIdList.item(0).getTextContent();
			
			
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/*-------------------------------------------------------事件消息-----------------------------------------------------------*/
	
	/** 
	 * 接收成员操作一用出发的事件信息。开启接收消息模式后，可以配置接收事件消息。当企业成员通过企业微信APP或微信插件触发进入应用、上报地理位置、点击菜单等事件时，企业微信会将这些事件消息发送给企业后台。
	 * @author yang.lvsen
	 * @date 2018年4月27日 下午1:29:07
	 * @param  request
	 * @return void
	 * @throws
	 */
	public static void EventInformation(HttpServletRequest request) {
		//先获取企业微信发送至企业后台的加密信息
		String sMsg = getInformation(request);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader sr = new StringReader(sMsg);
			InputSource is = new InputSource(sr);
			Document document = db.parse(is);
			Element root = document.getDocumentElement();
			NodeList CorpIDList = root.getElementsByTagName("ToUserName");	//企业微信CorpID
			String CorpID = CorpIDList.item(0).getTextContent();
			NodeList fromUserIdList = root.getElementsByTagName("FromUserName");	//发送者userid
			String fromUserId = fromUserIdList.item(0).getTextContent();
			NodeList createTimeList = root.getElementsByTagName("CreateTime");	//消息发送的时间
			String createTime = createTimeList.item(0).getTextContent();
			NodeList msgTypeList = root.getElementsByTagName("MsgType");	//消息类型
			String msgType = msgTypeList.item(0).getTextContent();
			NodeList eventList = root.getElementsByTagName("Event");	//事件类型,subscribe(订阅)、unsubscribe(取消订阅)
			String event = eventList.item(0).getTextContent();
			
			if("event".equals(msgType)){
				if("subscribe".equals(event) || "unsubscribe".equals(event) 	//关注/取消关注事件
						|| "enter_agent".equals(event) 		//进入应用
						|| "".equals(event)){
					NodeList eventKeyList = root.getElementsByTagName("EventKey");	//事件类型
					String eventKey = eventKeyList.item(0).getTextContent();
				}else if("LOCATION".equals(event)){		//地理位置
					NodeList latitudeList = root.getElementsByTagName("Latitude");	//纬度
					String latitude = latitudeList.item(0).getTextContent();
					NodeList longitudeList = root.getElementsByTagName("Longitude");	//经度
					String longitude = longitudeList.item(0).getTextContent();
					NodeList precisionList = root.getElementsByTagName("Precision");	//精度
					String precision = precisionList.item(0).getTextContent();
				}
				NodeList agentIDList = root.getElementsByTagName("AgentID");	//	企业应用的id，整型
				String agentID = agentIDList.item(0).getTextContent();
				if("batch_job_result".equals(event)){
					NodeList batchJobList = root.getElementsByTagName("BatchJob");	//任务列表
					for(int i = 0; i < batchJobList.getLength();i++){
						Node node = batchJobList.item(i);
						//判断当前元素是否是标签元素
						if(node.getNodeType() == Node.ELEMENT_NODE){
							NamedNodeMap attrs = node.getAttributes();
							for(int j = 0; j < attrs.getLength(); j++){
								Node attr = attrs.item(j);
								String attrName = attr.getNodeName();
								System.out.println("属性名："+attrName);
								if("JobId".equals(attrName)){
									String attrValue = attr.getNodeValue();
								}else if("JobType".equals(attrName)){	//操作类型，字符串,sync_user(增量更新成员)、 replace_user(全量覆盖成员）、invite_user(邀请成员关注）、replace_party(全量覆盖部门)
									String attrValue = attr.getNodeValue();
								}else if("ErrCode".equals(attrName)){
									String attrValue = attr.getNodeValue();
								}else if("ErrMsg".equals(attrName)){
									String attrValue = attr.getNodeValue();
								}
							}
						}
					}
				}
				
				if("change_contact".equals(event)){		//通讯录变更事件，新增成员事件/更新成员事件
					List<String> userList = new ArrayList<String>();
					userList.add("create_user");
					userList.add("update_user");
					userList.add("delete_user");
					List<String> deptList = new ArrayList<String>();
					deptList.add("create_party");
					deptList.add("update_party");
					deptList.add("delete_party");
					List<String> tagList = new ArrayList<String>();
					tagList.add("update_tag");
					NodeList changeTypeList = root.getElementsByTagName("ChangeType");	
					String changeType = changeTypeList.item(0).getTextContent();
					if(userList.contains(changeType)){
						NodeList userIDList = root.getElementsByTagName("UserID");	
						String userID = userIDList.item(0).getTextContent();
						if("delete_user".equals(changeType)){		//删除成员
							//这里根据userid来删除成员
							
							
						}else{		//新增/更新成员
							NodeList nameList = root.getElementsByTagName("Name");	
							String name = nameList.item(0).getTextContent();
							NodeList departmentList = root.getElementsByTagName("Department");	
							String department = departmentList.item(0).getTextContent();
							NodeList mobileList = root.getElementsByTagName("Mobile");	
							String mobile = mobileList.item(0).getTextContent();
							NodeList positionList = root.getElementsByTagName("Position");	
							String position = positionList.item(0).getTextContent();
							NodeList genderList = root.getElementsByTagName("Gender");	
							String gender = genderList.item(0).getTextContent();
							NodeList emailList = root.getElementsByTagName("Email");	
							String email = emailList.item(0).getTextContent();
							NodeList statusList = root.getElementsByTagName("Status");	
							String status = statusList.item(0).getTextContent();
							NodeList avatarList = root.getElementsByTagName("Avatar");	
							String avatar = avatarList.item(0).getTextContent();
							NodeList englishNameList = root.getElementsByTagName("EnglishName");	
							String englishName = englishNameList.item(0).getTextContent();
							NodeList isLeaderList = root.getElementsByTagName("IsLeader");	
							String isLeader = isLeaderList.item(0).getTextContent();
							NodeList telephoneList = root.getElementsByTagName("Telephone");	
							String telephone = telephoneList.item(0).getTextContent();
							NodeList extAttrList = root.getElementsByTagName("ExtAttr");	
							String extAttr = extAttrList.item(0).getTextContent();
							
							if("create_user".equals(changeType)){		//新增成员
								
							}else{		//更新成员
								NodeList newUserIDList = root.getElementsByTagName("NewUserID");	
								String newUserID = newUserIDList.item(0).getTextContent();
							}
						}
					}else if(deptList.contains(changeType)){		//部门变更
						NodeList idList = root.getElementsByTagName("Id");	
						String id = idList.item(0).getTextContent();
						if("create_party".equals(changeType) || "update_party".equals(changeType)){		//新增部门/修改部门
							NodeList nameList = root.getElementsByTagName("Name");	
							String name = nameList.item(0).getTextContent();
							NodeList parentIdList = root.getElementsByTagName("ParentId");	
							String parentId = parentIdList.item(0).getTextContent();
							if("create_party".equals(changeType)){
								NodeList orderList = root.getElementsByTagName("Order");	
								String order = orderList.item(0).getTextContent();
							}
						}else{
							//这里删除部门，根据id
						}
					}else if(tagList.contains(changeType)){		//标签变更
						NodeList tagIdList = root.getElementsByTagName("TagId");	
						String tagId = tagIdList.item(0).getTextContent();
						NodeList addUserItemsList = root.getElementsByTagName("AddUserItems");	
						String addUserItems = addUserItemsList.item(0).getTextContent();
						String[] addUserItemsArr = addUserItems.split(",");
						NodeList delUserItemsList = root.getElementsByTagName("DelUserItems");	
						String delUserItems = delUserItemsList.item(0).getTextContent();
						String[] delUserItemsArr = delUserItems.split(",");
						NodeList addPartyItemsList = root.getElementsByTagName("AddPartyItems");	
						String addPartyItems = addPartyItemsList.item(0).getTextContent();
						String[] addPartyItemsArr = addPartyItems.split(",");
						NodeList delPartyItemsList = root.getElementsByTagName("DelPartyItems	");	
						String delPartyItems = delPartyItemsList.item(0).getTextContent();
						String[] delPartyItemsArr = delPartyItems.split(",");
						
						//保存变更内容操作。。。
						
					}
				}
			}else if("click".equals(msgType)){			//点击菜单拉取消息的事件推送
				NodeList eventKeyList = root.getElementsByTagName("EventKey");		//事件KEY值，与自定义菜单接口中KEY值对应
				String eventKey = eventKeyList.item(0).getTextContent();
				
			}else if("view".equals(msgType)){			//点击菜单跳转链接的事件推送
				NodeList eventKeyList = root.getElementsByTagName("EventKey");		//事件KEY值，设置的跳转URL
				String eventKey = eventKeyList.item(0).getTextContent();
				
			}else if("scancode_push".equals(msgType) || "scancode_waitmsg".equals(msgType)){			//扫码推事件的事件推送/扫码推事件且弹出“消息接收中”提示框的事件推送
				NodeList eventKeyList = root.getElementsByTagName("EventKey");		//事件KEY值，与自定义菜单接口中KEY值对应
				String eventKey = eventKeyList.item(0).getTextContent();
				NodeList scanTypeList = root.getElementsByTagName("ScanType");	//任务列表
				String scanType = scanTypeList.item(0).getTextContent();
				NodeList scanResultList = root.getElementsByTagName("ScanResult");	//任务列表
				String scanResult = scanResultList.item(0).getTextContent();
				
			}else if("pic_sysphoto".equals(msgType)){			//弹出系统拍照发图的事件推送
				NodeList eventKeyList = root.getElementsByTagName("EventKey");		//事件KEY值，与自定义菜单接口中KEY值对应
				String eventKey = eventKeyList.item(0).getTextContent();
				NodeList scanCodeInfoList = root.getElementsByTagName("ScanCodeInfo");		
				String scanCodeInfo = scanCodeInfoList.item(0).getTextContent();
				NodeList countList = root.getElementsByTagName("Count");		
				String count = countList.item(0).getTextContent();
				NodeList picLists = root.getElementsByTagName("PicList");		
				String picList = picLists.item(0).getTextContent();
				NodeList picMd5SumList = root.getElementsByTagName("PicMd5Sum");		
				String picMd5Sum = picMd5SumList.item(0).getTextContent();
				
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
