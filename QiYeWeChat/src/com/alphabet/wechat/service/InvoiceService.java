package com.alphabet.wechat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;
import com.alphabet.wechat.wxpay.WXPayConstants;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Title: InvoiceService 
 * @Description: 发票
 * @author yang.lvsen
 * @date 2018年5月11日 下午3:00:06 
 *  
 */
public class InvoiceService {
	
	/**
	 * 查询电子发票
	 * 报销方在获得用户选择的电子发票标识参数后，可以通过该接口查询电子发票的结构化信息，并获取发票PDF文件。
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午4:24:05
	 * @param dataMap
	 * @throws Exception 
	 * @return Map<String,Object>
	 */
	public static Map<String,Object> getInvoiceInfo(Map<String,Object> dataMap) throws Exception{
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/card/invoice/reimburse/getinvoiceinfo?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		updJsonObj.put("card_id", dataMap.get("card_id") == null ? "" : dataMap.get("card_id").toString());	//发票ID
		updJsonObj.put("encrypt_code", "");		//加密code
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		if("0".equals(jsonObj.getString("errcode"))){
			String card_id = jsonObj.get("card_id")==null?"":jsonObj.get("card_id").toString();	//发票id
			String begin_time = jsonObj.get("begin_time")==null?"":jsonObj.get("begin_time").toString();	//发票的有效期起始时间
			String end_time = jsonObj.get("end_time")==null?"":jsonObj.get("end_time").toString();	//发票的有效期截止时间
			String openid = jsonObj.get("openid")==null?"":jsonObj.get("openid").toString();	//用户标识
			String type = jsonObj.get("type")==null?"":jsonObj.get("type").toString();	//发票类型，如广东增值税普通发票
			String payee = jsonObj.get("payee")==null?"":jsonObj.get("payee").toString();	//发票的收款方
			String detail = jsonObj.get("detail")==null?"":jsonObj.get("detail").toString();	//发票详情
			String user_info = jsonObj.get("user_info")==null?"":jsonObj.get("user_info").toString();	//发票的用户信息，见user_info结构说明
			JSONObject jsonUserInfo = JSONObject.fromObject(user_info);	
			String fee = jsonUserInfo.get("fee")==null?"":jsonUserInfo.get("fee").toString();	//发票加税合计金额，以分为单位
			String title = jsonUserInfo.get("title")==null?"":jsonUserInfo.get("title").toString();	//发票的抬头
			String billing_time = jsonUserInfo.get("billing_time")==null?"":jsonUserInfo.get("billing_time").toString();	//开票时间，为十位时间戳
			String billing_no = jsonUserInfo.get("billing_no")==null?"":jsonUserInfo.get("billing_no").toString();	//发票代码
			String billing_code = jsonUserInfo.get("billing_code")==null?"":jsonUserInfo.get("billing_code").toString();	//发票号码
			String fee_without_tax = jsonUserInfo.get("fee_without_tax")==null?"":jsonUserInfo.get("fee_without_tax").toString();	//不含税金额，以分为单位
			String tax = jsonUserInfo.get("tax")==null?"":jsonUserInfo.get("tax").toString();	//
			String detail2 = jsonUserInfo.get("detail")==null?"":jsonUserInfo.get("detail").toString();	//发票详情，一般描述的是发票的使用说明
			String pdf_url = jsonUserInfo.get("pdf_url")==null?"":jsonUserInfo.get("pdf_url").toString();	//这张发票对应的PDF_URL
			String trip_pdf_url = jsonUserInfo.get("trip_pdf_url")==null?"":jsonUserInfo.get("trip_pdf_url").toString();	//其它消费凭证附件对应的URL，如行程单、水单等
			String reimburse_status = jsonUserInfo.get("reimburse_status")==null?"":jsonUserInfo.get("reimburse_status").toString();	//发报销状态INVOICE_REIMBURSE_INIT：发票初始状态，未锁定；INVOICE_REIMBURSE_LOCK：发票已锁定；INVOICE_REIMBURSE_CLOSURE：发票已核销
			String check_code = jsonUserInfo.get("check_code")==null?"":jsonUserInfo.get("check_code").toString();	//校验码
			String buyer_number = jsonUserInfo.get("buyer_number")==null?"":jsonUserInfo.get("buyer_number").toString();	//购买方纳税人识别号
			String buyer_address_and_phone = jsonUserInfo.get("buyer_address_and_phone")==null?"":jsonUserInfo.get("buyer_address_and_phone").toString();	//购买方地址、电话
			String buyer_bank_account = jsonUserInfo.get("buyer_bank_account")==null?"":jsonUserInfo.get("buyer_bank_account").toString();	//购买方开户行及账号
			String seller_number = jsonUserInfo.get("seller_number")==null?"":jsonUserInfo.get("seller_number").toString();	//销售方纳税人识别号
			String seller_address_and_phone = jsonUserInfo.get("seller_address_and_phone")==null?"":jsonUserInfo.get("seller_address_and_phone").toString();	//销售方地址、电话
			String seller_bank_account = jsonUserInfo.get("seller_bank_account")==null?"":jsonUserInfo.get("seller_bank_account").toString();	//销售方开户行及账号
			String remarks = jsonUserInfo.get("remarks")==null?"":jsonUserInfo.get("remarks").toString();	//备注
			String cashier = jsonUserInfo.get("cashier")==null?"":jsonUserInfo.get("cashier").toString();	//收款人，发票左下角处
			String maker = jsonUserInfo.get("maker")==null?"":jsonUserInfo.get("maker").toString();	//开票人，发票有下角处
			String info = jsonUserInfo.get("info")==null?"":jsonUserInfo.get("info").toString();	//商品信息结构
			JSONArray jsonDataArr = jsonObj.getJSONArray("info");
			List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
			for(int i=0;i<jsonDataArr.size();i++){
				Map<String,String> map = new HashMap<String,String>();
				JSONObject data = jsonDataArr.getJSONObject(i);
				String name = data.get("name")==null?"":data.get("name").toString();	//项目（商品）名称
				String num = data.get("num")==null?"":data.get("num").toString();	//项目数量
				String unit = data.get("unit")==null?"":data.get("unit").toString();	//项目单位
				String price = data.get("price")==null?"":data.get("price").toString();	//单价，以分为单位
				map.put("name", name);
				map.put("num", num);
				map.put("unit", unit);
				map.put("price", price);
				resultList.add(map);
			}
			resultMap.put("card_id", card_id);
			resultMap.put("begin_time", ErpCommon.Timestamp2DateTime(Long.parseLong(begin_time)));
			resultMap.put("end_time", ErpCommon.Timestamp2DateTime(Long.parseLong(end_time)));
			resultMap.put("openid", openid);
			resultMap.put("type", type);
			resultMap.put("payee", payee);
			resultMap.put("detail", detail);
			resultMap.put("fee", fee);
			resultMap.put("title", title);
			resultMap.put("billing_time", ErpCommon.Timestamp2DateTime(Long.parseLong(billing_time)));
			resultMap.put("billing_no", billing_no);
			resultMap.put("billing_code", billing_code);
			resultMap.put("fee_without_tax", fee_without_tax == null ? "0": fee_without_tax);
			resultMap.put("tax", tax);
			resultMap.put("detail2", detail2);
			resultMap.put("pdf_url", pdf_url);
			resultMap.put("trip_pdf_url", trip_pdf_url);
			resultMap.put("reimburse_status", reimburse_status);
			resultMap.put("check_code", check_code);
			resultMap.put("buyer_number", buyer_number);
			resultMap.put("buyer_address_and_phone", buyer_address_and_phone);
			resultMap.put("buyer_bank_account", buyer_bank_account);
			resultMap.put("seller_number", seller_number);
			resultMap.put("seller_address_and_phone", seller_address_and_phone);
			resultMap.put("seller_bank_account", seller_bank_account);
			resultMap.put("remarks", remarks);
			resultMap.put("cashier", cashier);
			resultMap.put("maker", maker);
			resultMap.put("info", resultList);
		}
		return resultMap;
	}
	
	
	/** 
	 * 更新发票状态
	 * 报销企业和报销服务商可以通过该接口对某一张发票进行锁定、解锁和报销操作。各操作的业务含义及在用户端的表现如下：
	 * 锁定：电子发票进入了企业的报销流程时应该执行锁定操作，执行锁定操作后的电子发票仍然会存在于用户卡包内，但无法重复提交报销。
	 * 解锁：当电子发票由于各种原因，无法完成报销流程时，应执行解锁操作。执行锁定操作后的电子发票将恢复可以被提交的状态。
	 * 报销：当电子发票报销完成后，应该使用本接口执行报销操作。执行报销操作后的电子发票将从用户的卡包中移除，用户可以在卡包的消息中查看到电子发票的核销信息。注意，报销为不可逆操作，请开发者慎重调用。
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午4:34:55
	 * @param dataMap
	 * @throws Exception 
	 * @return String
	 */
	public static String updateInvoiceStatus(Map<String,String> dataMap) throws Exception{
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/card/invoice/reimburse/updateinvoicestatus?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		updJsonObj.put("card_id", dataMap.get("card_id") == null ? "" : dataMap.get("card_id").toString());	//发票ID
		updJsonObj.put("encrypt_code", "");		//加密code
		//发票报销状态 INVOICE_REIMBURSE_INIT：发票初始状态，未锁定；INVOICE_REIMBURSE_LOCK：发票已锁定，无法重复提交报销;INVOICE_REIMBURSE_CLOSURE:发票已核销，从用户卡包中移除
		updJsonObj.put("reimburse_status", dataMap.get("reimburse_status") == null ? "" : dataMap.get("reimburse_status").toString());
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		if("0".equals(jsonObj.getString("errcode"))){
			return "0";
		}
		return null;
	}
	
	
	public static String updateInvoiceStatusBatch(List<Map<String,String>> list, String status){
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/card/invoice/reimburse/updatestatusbatch?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		String openid = WeChatServer.UserIdAndOpenIdConversion(ConstantCommon.CorpID, ConstantCommon.CorpSecret, "这里填写需要接收人的userid", "U");
		updJsonObj.put("openid", openid);		//用户openid,
		updJsonObj.put("reimburse_status", status);		//用户openid,
		
		
		updJsonObj.put("card_id", dataMap.get("card_id") == null ? "" : dataMap.get("card_id").toString());	//发票ID
		updJsonObj.put("encrypt_code", "");		//加密code
		//发票报销状态 INVOICE_REIMBURSE_INIT：发票初始状态，未锁定；INVOICE_REIMBURSE_LOCK：发票已锁定，无法重复提交报销;INVOICE_REIMBURSE_CLOSURE:发票已核销，从用户卡包中移除
		updJsonObj.put("reimburse_status", dataMap.get("reimburse_status") == null ? "" : dataMap.get("reimburse_status").toString());
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		if("0".equals(jsonObj.getString("errcode"))){
			return "0";
		}
		
		
		
		
		return null;
	}

}
