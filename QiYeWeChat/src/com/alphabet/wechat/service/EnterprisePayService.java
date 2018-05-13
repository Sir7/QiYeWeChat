package com.alphabet.wechat.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.UploadAndDownloadMedia;
import com.alphabet.wechat.common.WeChatServer;
import com.alphabet.wechat.wxpay.WXPayConstants;
import com.alphabet.wechat.wxpay.WXPayConstants.Check;
import com.alphabet.wechat.wxpay.WXPayRequest;
import com.alphabet.wechat.wxpay.WXPayUtil;
import com.alphabet.wechat.wxpay.impl.WXPayConfigImpl;

/** 
 * @Title: EnterprisePayService 
 * @Description: 企业支付
 * @author yang.lvsen
 * @date 2018年5月7日 下午8:56:02 
 *  
 */
public class EnterprisePayService {
	
	/** 
	 * 微信支付/企业微信签名算法所需参数集合，以下参数均是必填项,且参数的值不能为空，若为空则不传递该参数！
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:26:22
	 * @param map	获取签名的数据参数
	 * @param type	所要求取的是什么签名算法，type=wxpay则是求微信支付的签名算法，type=qywxpay是求企业微信的签名算法
	 * @throws Exception 
	 * @return Map<String,String>
	 */
	public static Map<String,String> getPAYData(Map<String,String> map,String type) throws Exception{
		Map<String,String> data = new HashMap<String,String>();
		if(ErpCommon.isNotNull(type)){
			if(WXPayConstants.WXPAY.equals(type)){	//微信支付签名算法
				data.put("appid", ConstantCommon.CorpID);//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId）
				data.put("mch_id", WXPayConstants.mchId);	//商户号,微信支付分配的商户号
				data.put("body", map.get("body") == null ? "测试":map.get("body"));
				data.put("nonce_str", WXPayUtil.generateNonceStr());	//随机生成的32位字符串
				//data.put("device_info", map.get("device_info") == null ? "100":map.get("device_info"));	//支付的金额
			}else if(WXPayConstants.QYWXPAY.equals(type)){	//企业微信签名算法
				data.put("act_name", map.get("act_name") == null ? "猜灯谜抢红包活动":map.get("act_name"));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
				String ydm = sdf.format(new Date());
				data.put("mch_billno", WXPayConstants.mchId+ydm+UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10));	//商户订单号，可以随机生成，长度28位
				data.put("mch_id", WXPayConstants.mchId);//商户号,微信支付分配的商户号，必填
				data.put("nonce_str", WXPayUtil.generateNonceStr());
				String openid = WeChatServer.UserIdAndOpenIdConversion(ConstantCommon.CorpID, WXPayConstants.redPacketSecret, "这里填写需要接收人的userid", "U");
				data.put("re_openid", openid);	//用户openid
				//data.put("total_amount", map.get("total_amount") == null ? "100":map.get("total_amount"));
				data.put("wxappid", ConstantCommon.CorpID);//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId）
			}
		}
		return data;
	}
	
	
	/*----------------------------------------------------------------企业红包---------------------------------------------------------------*/
	/** 
	 * 发送企业红包，每次红包最大金额200元，最少1分
	 * @author yang.lvsen
	 * @date 2018年5月10日 下午3:23:22
	 * @param packetData 前端页面填写的红包信息
	 * @return String
	 * @throws Exception
	 */
	public static String HandingOutEnterpriseRedPacket(Map<String,String> packetData) throws Exception{
		String nonce_str = WXPayUtil.generateNonceStr();	//生成随机32为字符串，必填
		Map<String,String> wxpayMap = new HashMap<String,String>();
		wxpayMap.put("body", "微信支付");
		wxpayMap.put("device_info", "100");
		Map<String,String> wxpaydata = getPAYData(wxpayMap, WXPayConstants.WXPAY);	//获取微信支付请求时所需的数据集合
		wxpaydata.put("signClass", WXPayConstants.WXPAY);
		String sign = WXPayUtil.generateSignature(wxpaydata, WXPayConstants.KEY);	//生成微信支付签名，必填
		String mch_id = WXPayConstants.mchId;		//商户号,微信支付分配的商户号，必填
		String mch_billno = WXPayUtil.getBillno(mch_id);	//商户订单号,mch_id+yyyymmdd+10位一天内不能重复的数字组成	
		String wxappid = ConstantCommon.CorpID;	//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId），必填
		String sender_name = URLEncoder.encode("xxx活动", "UTF-8");	//发送者名称.需要utf-8格式。与agentid互斥，二者只能填一个。选填
		long agentid = Long.parseLong(WXPayConstants.redPacketAgentId);	//以企业应用的名义发红包.企业应用id，整型。与sender_name互斥，二者只能填一个。选填
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, WXPayConstants.redPacketSecret);
		String mediaFileUrl = "";	//头像图片在本地的路径
		String mediaId = UploadAndDownloadMedia.uploadMedia(accessToken, "image", mediaFileUrl);
		String sender_header_media_id = mediaId;		//发送者头像。发送者头像素材id，通过企业微信开放上传素材接口获取，获取临时素材。选填
		String openid = WeChatServer.UserIdAndOpenIdConversion(ConstantCommon.CorpID, WXPayConstants.redPacketSecret, "这里填写需要接收人的userid", "U");
		String re_openid = openid;	//用户openid。接受红包的用户.用户在wxappid下的openid。必填
		int total_amount = 100;	//金额，单位分，单笔最小金额默认为1元，100 == 1元钱 ，也就是说 这里的 1 相当于1分钱。必填
		String wishing = "感谢您参加猜灯谜活动，祝您元宵节快乐！";	//红包祝福语。128字符长度必填
		String act_name = "猜灯谜抢红包活动";		//项目名称。32字符长度必填
		String remark = "猜越多得越多，快来抢！";	//备注信息。256字符长度必填
		String scene_id = "PRODUCT_1";	//场景发放红包使用场景，红包金额大于200时必传 。PRODUCT_1:商品促销 PRODUCT_2:抽奖 PRODUCT_3:虚拟物品兑奖 PRODUCT_4:企业内部福利	 PRODUCT_5:渠道分润 PRODUCT_6:保险回馈 PRODUCT_7:彩票派奖 PRODUCT_8:税务刮奖 
		Map<String,String> qywxpayMap = new HashMap<String,String>();
		qywxpayMap.put("act_name", "猜灯谜抢红包活动");
		qywxpayMap.put("total_amount", "100");
		Map<String,String> qywxdata = getPAYData(qywxpayMap, WXPayConstants.QYWXPAY);	//获取企业微信签名请求时所需的数据集合
		qywxdata.put("signClass", WXPayConstants.QYWXPAY);
		String workwx_sign = WXPayUtil.generateSignature(qywxdata, WXPayConstants.redPacketSecret);	//生成微信支付签名，必填
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("nonce_str", nonce_str);
		data.put("sign", sign);
		data.put("mch_billno", mch_billno);
		data.put("mch_id", mch_id);
		data.put("wxappid", wxappid);
		data.put("sender_name", sender_name);
		data.put("sender_header_media_id", sender_header_media_id);
		data.put("re_openid", re_openid);
		data.put("total_amount", total_amount+"");
		data.put("wishing", wishing);
		data.put("act_name", act_name);
		data.put("remark", remark);
		data.put("workwx_sign", workwx_sign);
		String xmlData = WXPayUtil.mapToXml(data);
		
		WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        WXPayRequest wxPayRequest = new WXPayRequest(config);
        String result = wxPayRequest.requestWithCert("mmpaymkttransfers/sendworkwxredpack", "1213uuid", xmlData, 10000, 10000, true);
		Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
		String return_code = resultMap.get("return_code");	//返回状态码
		String result_code = resultMap.get("result_code");	//业务结果
		if(WXPayConstants.SUCCESS.equals(return_code) && WXPayConstants.SUCCESS.equals(result_code)){	//通讯成功且交易成功，红包发送成功
			String returnmch_billno = resultMap.get("mch_billno");
			String send_listid = resultMap.get("send_listid");
			
		}else{	//红包发送失败
			String return_msg = resultMap.get("return_msg");
			return return_msg;
		}
		return null;
	}
	
	
	/** 
	 * 查询红包记录
	 * @author yang.lvsen
	 * @date 2018年5月10日 下午4:46:54
	 * @param map	前端页面填写的需要查询的红包单号
	 * @return Map<String,String>
	 * @throws Exception
	 */
	public static Map<String,String> getRedPacketInfo(Map<String,String> map) throws Exception{
		Map<String,String> wxpayMap = new HashMap<String,String>();
		wxpayMap.put("body", "微信支付");
		Map<String,String> wxpaydata = getPAYData(wxpayMap, WXPayConstants.WXPAY);	//获取微信支付请求时所需的数据集合
		wxpaydata.put("signClass", WXPayConstants.WXPAY);
		String sign = WXPayUtil.generateSignature(wxpaydata, WXPayConstants.KEY);	//生成微信支付签名，必填
		String nonce_str = WXPayUtil.generateNonceStr();	//生成随机32为字符串，必填
		String mch_billno = map.get("mch_billno");
		String mch_id = WXPayConstants.mchId;		//商户号,微信支付分配的商户号，必填
		String appid = ConstantCommon.CorpID;	//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId），必填

		Map<String,String> data = new HashMap<String,String>();
		data.put("nonce_str", nonce_str);
		data.put("sign", sign);
		data.put("mch_billno", mch_billno);
		data.put("mch_id", mch_id);
		data.put("appid", appid);
		String xmlData = WXPayUtil.mapToXml(data);
		
		WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        WXPayRequest wxPayRequest = new WXPayRequest(config);
        String result = wxPayRequest.requestWithCert("mmpaymkttransfers/queryworkwxredpack", "1213uuid", xmlData, 10000, 10000, true);
		Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
		String return_code = resultMap.get("return_code");	//返回状态码
		String result_code = resultMap.get("result_code");	//业务结果
		data.clear();
		if(WXPayConstants.SUCCESS.equals(return_code) && WXPayConstants.SUCCESS.equals(result_code)){	//通讯成功且交易成功，红包发送成功
			data.put("mch_billno", resultMap.get("mch_billno") == null ? "" : resultMap.get("mch_billno"));	//商户订单号
			data.put("mch_id", resultMap.get("mch_id") == null ? "" : resultMap.get("mch_id"));	//商户号
			data.put("detail_id", resultMap.get("detail_id") == null ? "" : resultMap.get("detail_id"));	//红包单号	
			data.put("status", resultMap.get("status") == null ? "" : resultMap.get("status"));	//红包状态。SENDING:发放 SENT:已发放待领取 FAILED：发放失败 RECEIVED:已领取 RFUND_ING:退款中 REFUND:已退款
			data.put("send_type", resultMap.get("send_type") == null ? "" : resultMap.get("send_type"));	//发放类型。API:通过API接口发放
			data.put("total_amount", resultMap.get("total_amount") == null ? "" : resultMap.get("total_amount"));	//红包金额。红包总金额（单位分）
			data.put("reason", resultMap.get("reason") == null ? "" : resultMap.get("reason"));	//失败原因	
			data.put("send_time", resultMap.get("send_time") == null ? "" : resultMap.get("send_time"));	//红包发送时间	
			data.put("refund_time", resultMap.get("refund_time") == null ? "" : resultMap.get("refund_time"));	//红包退款时间。红包的退款时间（如果其未领取的退款）
			data.put("refund_amount", resultMap.get("refund_amount") == null ? "" : resultMap.get("refund_amount"));	//红包退款金额	
			data.put("wishing", resultMap.get("wishing") == null ? "" : resultMap.get("wishing"));	//祝福语	
			data.put("remark", resultMap.get("remark") == null ? "" : resultMap.get("remark"));	//活动描述	
			data.put("act_name", resultMap.get("act_name") == null ? "" : resultMap.get("act_name"));	//活动名称	
			data.put("openid", resultMap.get("openid") == null ? "" : resultMap.get("openid"));	//领取红包的Openid	
			data.put("amount", resultMap.get("amount") == null ? "" : resultMap.get("amount"));	//金额
			data.put("rcv_time", resultMap.get("rcv_time") == null ? "" : resultMap.get("rcv_time"));	//领取红包的时间
			data.put("sender_name", resultMap.get("sender_name") == null ? "" : resultMap.get("sender_name"));	//发送者名称	
			data.put("sender_header_media_id", resultMap.get("sender_header_media_id") == null ? "" : resultMap.get("sender_header_media_id"));	//发送者头像素材
		}else{	//获取红包信息失败
			data.put("return_msg", resultMap.get("return_msg") == null ? "" : resultMap.get("return_msg"));	//返回信息	
		}
		return data;
	}
	
	
	/*-------------------------------------------------------------------向员工付款------------------------------------------------------------------------*/
	/**
	 * 接口调用规则：
	 * 给同一个实名用户付款，单笔单日限额2W/2W
	 * 不支持给非实名用户打款
	 * 一个商户同一日付款总额限额100W
	 * 单笔最小金额默认为1元
	 * 每个用户每天最多可付款10次，可以在商户平台—API安全进行设置
	 * 给同一个用户付款时间间隔不得低于15秒
	 */
	/** 
	 * 向员工付款
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午2:49:13
	 * @param map	//前端页面传递需要付款的信息
	 * @throws Exception 
	 * @return Map<String,String>
	 */
	public static Map<String,String> payMoney2Staff(Map<String,String> map) throws Exception{
		String appid = ConstantCommon.CorpID;	//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId），必填
		String mch_id = WXPayConstants.mchId;		//商户号,微信支付分配的商户号，必填
		String device_info = WXPayConstants.deviceInfo;		//设备号	String(32)	微信支付分配的终端设备号，选填
		String nonce_str = WXPayUtil.generateNonceStr();	//生成随机32为字符串，必填
		Map<String,String> wxpayMap = new HashMap<String,String>();
		wxpayMap.put("body", "向员工付款");
		Map<String,String> wxpaydata = getPAYData(wxpayMap, WXPayConstants.WXPAY);	//获取微信支付请求时所需的数据集合
		wxpaydata.put("signClass", WXPayConstants.WXPAY);
		String sign = WXPayUtil.generateSignature(wxpaydata, WXPayConstants.KEY);	//生成微信支付签名，必填
		String partner_trade_no = WXPayUtil.getBillno(mch_id);	//商户订单号,mch_id+yyyymmdd+10位一天内不能重复的数字组成
		String openid = WeChatServer.UserIdAndOpenIdConversion(ConstantCommon.CorpID, WXPayConstants.redPacketSecret, "这里填写需要接收人的userid", "userid");
		String check_name = Check.FORCE_CHECK.toString();	//校验用户姓名选项.NO_CHECK：不校验真实姓名 FORCE_CHECK：强校验真实姓名
		String re_user_name = URLEncoder.encode("马花花", "UTF-8");	//收款用户真实姓名。 如果check_name设置为FORCE_CHECK，则必填用户真实姓名
		int amount = 100;	//金额，单位分，单笔最小金额默认为1元，100 == 1元钱 ，也就是说 这里的 1 相当于1分钱。必填
		String desc = "六月出差报销费用";		//向员工付款说明信息。最长100个utf8字符，必填
		String spbill_create_ip = ErpCommon.getIPAddress().get("ip");	//调用接口的机器Ip地址。必填
		Map<String,String> qywxpayMap = new HashMap<String,String>();
		qywxpayMap.put("act_name", "猜灯谜抢红包活动");
		qywxpayMap.put("total_amount", "100");
		Map<String,String> qywxdata = getPAYData(qywxpayMap, WXPayConstants.QYWXPAY);	//获取企业微信签名请求时所需的数据集合
		qywxdata.put("signClass", WXPayConstants.QYWXPAY);
		String workwx_sign = WXPayUtil.generateSignature(qywxdata, WXPayConstants.redPacketSecret);	//生成微信支付签名，必填
		String ww_msg_type = "NORMAL_MSG";	//NORMAL_MSG：普通付款消息 ,APPROVAL_MSG：审批付款消息，必填
		if(ErpCommon.isNotNull(ww_msg_type) && "APPROVAL_MSG".equals(ww_msg_type)){
			String approval_number = ""; 	//审批单号	,ww_msg_type为APPROVAL _MSG时，需要填写approval_number，选填
			String approval_type = "1";	//审批类型	，ww_msg_type为APPROVAL _MSG时，需要填写1,选填
		}
		String act_name = URLEncoder.encode("产品部门报销", "UTF-8");	//项目名称，最长50个utf8字符，必填
		String agentid = WXPayConstants.redPacketAgentId;	//付款的应用id,以企业应用的名义付款，企业应用id，整型.选填
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("appid", appid);
		data.put("mch_id", mch_id);
		data.put("device_info", device_info);
		data.put("nonce_str", nonce_str);
		data.put("sign", sign);
		data.put("partner_trade_no", partner_trade_no);
		data.put("openid", openid);
		data.put("check_name", check_name);
		data.put("re_user_name", re_user_name);
		data.put("amount", amount+"");
		data.put("desc", desc);
		data.put("spbill_create_ip", spbill_create_ip);
		data.put("workwx_sign", workwx_sign);
		data.put("act_name", act_name);
		data.put("agentid", agentid);
		String xmlData = WXPayUtil.mapToXml(data);
		
		WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        WXPayRequest wxPayRequest = new WXPayRequest(config);
        //请求需要双向证书
        String result = wxPayRequest.requestWithCert("mmpaymkttransfers/promotion/paywwsptrans2pocket", "1213uuid", xmlData, 10000, 10000, true);
		Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
		String return_code = resultMap.get("return_code");	//返回状态码
		String result_code = resultMap.get("result_code");	//业务结果
		data.clear();
		if(WXPayConstants.SUCCESS.equals(return_code) && WXPayConstants.SUCCESS.equals(result_code)){	//通讯成功且交易成功，红包发送成功
			String payment_no = resultMap.get("payment_no");	//企业微信企业付款成功，返回的微信订单号
			String payment_time = resultMap.get("payment_time");	//企业微信企业付款成功时间
			data.put("payment_no", payment_no);
			data.put("payment_time", payment_time);
		}else{	//红包发送失败
			String return_msg = resultMap.get("return_msg");
			data.put("return_msg", return_msg);
		}
		return data;
	}
	
	
	/** 
	 * 查询付款记录，查询企业微信企业付款API只支持查询30天内的订单，30天之前的订单请登录商户平台查询。
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午2:48:07
	 * @param map	//前端页面需要查询的数据
	 * @throws Exception 
	 * @return Map<String,String>
	 */
	public static Map<String,String> getPayMoney2StaffIno(Map<String,String> map) throws Exception{
		String nonce_str = WXPayUtil.generateNonceStr();	//生成随机32为字符串，必填
		String appid = ConstantCommon.CorpID;	//公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId），必填
		String mch_id = WXPayConstants.mchId;		//商户号,微信支付分配的商户号，必填
		Map<String,String> wxpayMap = new HashMap<String,String>();
		wxpayMap.put("body", "微信支付");
		Map<String,String> wxpaydata = getPAYData(wxpayMap, WXPayConstants.WXPAY);	//获取微信支付请求时所需的数据集合
		wxpaydata.put("signClass", WXPayConstants.WXPAY);
		String sign = WXPayUtil.generateSignature(wxpaydata, WXPayConstants.KEY);	//生成微信支付签名，必填
		String partner_trade_no = WXPayUtil.getBillno(mch_id);	//商户订单号,商户调用企业微信企业付款API时使用的商户订单号，必填
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("appid", appid);
		data.put("mch_id", mch_id);
		data.put("nonce_str", nonce_str);
		data.put("sign", sign);
		data.put("partner_trade_no", partner_trade_no);
		String xmlData = WXPayUtil.mapToXml(data);
		
		WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        WXPayRequest wxPayRequest = new WXPayRequest(config);
        //请求需要双向证书
        String result = wxPayRequest.requestWithCert("mmpaymkttransfers/promotion/querywwsptrans2pocket", "1213uuid", xmlData, 10000, 10000, true);
		Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
		String return_code = resultMap.get("return_code");	//返回状态码
		String result_code = resultMap.get("result_code");	//业务结果
		data.clear();
		if(WXPayConstants.SUCCESS.equals(return_code) && WXPayConstants.SUCCESS.equals(result_code)){	//通讯成功且交易成功，红包发送成功
			String partnerTradeNo = resultMap.get("payment_no");	//商户订单号。商户使用查询API填写的单号的原路返回
			String mchId = resultMap.get("mch_id");	//商户号。微信支付分配的商户号
			String detail_id = resultMap.get("detail_id");	//付款单号	。调用企业微信企业付款API时，微信系统内部产生的单号
			String status = resultMap.get("status");	//转账状态。SUCCESS:转账成功 FAILED:转账失败 PROCESSING:处理中
			String reason = resultMap.get("reason");	//失败原因	。如果失败则有失败原因
			String openid = resultMap.get("openid");	//收款用户。转账的openid
			String transfer_name = resultMap.get("transfer_name");	//收款用户姓名。收款用户姓名
			String payment_amount = resultMap.get("payment_amount");	//付款金额	。付款金额单位分
			String transfer_time = resultMap.get("transfer_time");	//转账时间	。发起转账的时间
			String desc = resultMap.get("desc");	//付款描述。付款时候的描述
			data.put("partnerTradeNo", partnerTradeNo);
			data.put("mchId", mchId);
			data.put("detail_id", detail_id);
			data.put("status", status);
			data.put("reason", reason);
			data.put("openid", openid);
			data.put("transfer_name", transfer_name);
			data.put("payment_amount", payment_amount);
			data.put("transfer_time", transfer_time);
			data.put("desc", desc);
		}else{	//获取数据失败
			String return_msg = resultMap.get("return_msg");
			data.put("return_msg", return_msg);
		}
		return data;
	}
	

}
