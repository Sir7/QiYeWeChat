package com.alphabet.wechat.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.service.EnterprisePayService;
import com.alphabet.wechat.service.InvoiceService;
import com.alphabet.wechat.service.OASyncWechatCheckInData;
import com.alphabet.wechat.service.QiyeWechatOrgUserSynService;
import com.alphabet.wechat.service.WeChatSendMsgToSystem;

/** 
 * @Title: QiyeWechatAgentController 
 * @Description: 企业微信的代理控制器
 * @author yang.lvsen
 * @date 2016年5月11日 下午2:54:13 
 *  
 */
public class QiyeWechatAgentController {
	
	private static final long serialVersionUID = 1L;

	
	/** 
	 * 向ces企业微信同步组织机构和用户
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:03:22
	 * @return void
	 */
	public void qiyeWechatSynOrgUser(){
		try {
			QiyeWechatOrgUserSynService.qiyeWechatSynOrgUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/** 
	 * 更新企业微信里部门和用户的显示顺序showorder
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:03:08
	 * @return Object
	 */
	public Object qiyeWechatUpdateOrgUserShoworder(){
		try {
			QiyeWechatOrgUserSynService.updateDeptShoworder();
			QiyeWechatOrgUserSynService.updateUserShoworder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/** 
	 * OA同步企业微信打卡记录
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:02:44
	 * @return Object
	 */
	public Object OASysqiyeWeChatCheckInData(){
		String msg = "";
		try {
			msg = OASyncWechatCheckInData.OASysqiyeWeChatCheckInData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	
	/** 
	 * 下载头像至本地
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:03:55
	 * @return Object
	 */
	public Object DownloadImageToLocal(){
		String msg = QiyeWechatOrgUserSynService.downloadImagesToLocal();
		return msg;
	}
	
	public Object DownloadNoImageData(){
		QiyeWechatOrgUserSynService.downloadNoImageData();
		return null;
	}
	
	
	/** 
	 * 重新加载系统初始化数据（部门组织、用户等）
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午8:04:09
	 * @return
	 */
	public void reloadInitData(){
		ErpCommon.reloadData();
	}
	
	/**
	 * 验证URL
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午6:06:56
	 * @param request
	 * @param response
	 * @return Object
	 * @throws
	 */
	public Object VerifyURL(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		String sEchoStr = null;
		try {
			out = response.getWriter();  
			sEchoStr = WeChatSendMsgToSystem.VerifyURL(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.print(sEchoStr);  
		out.close();  
		out = null;
		return sEchoStr;
	}
	
	
	
	/** 
	 * 模拟企业后台页面发红包
	 * @author yang.lvsen
	 * @date 2018年5月9日 下午7:56:59
	 * @return Object
	 */
	public Object HandingOutEnterpriseRedPacket(){
		//map集合存放前端页面填写的红包信息。
		Map<String,String> map = new HashMap<String,String>();
		map.put("sender_name", "XX活动");	//发送者名称
		map.put("total_amount", "1000");//金额，单位分，单笔最小金额默认为1元
		map.put("wishing", "感谢您参加猜灯谜活动，祝您元宵节快乐！");//红包祝福语
		map.put("act_name", "猜灯谜抢红包活动	");//项目名称
		map.put("remark", "猜越多得越多，快来抢！");//备注信息
		
		//将红包信息传递至后台，发红包
		String msg = "";
		try {
			msg = EnterprisePayService.HandingOutEnterpriseRedPacket(map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}
	
	
	/** 
	 * 查询发送红包记录
	 * @author yang.lvsen
	 * @date 2018年5月10日 下午3:47:35
	 * @return Object
	 */
	public Object getRedPacketInfo(){
		//map集合存放前端页面填写的要查询的信息。
		Map<String,String> map = new HashMap<String,String>();
		map.put("mch_billno", "123456");	//发送者名称
		
		Map<String, String> resultMap = new HashMap<String,String>();
		try {
			resultMap.putAll(EnterprisePayService.getRedPacketInfo(map));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	
	
	/** 
	 * 向员工付款
	 * @author yang.lvsen
	 * @date 2018年5月10日 下午5:07:23
	 * @return Object
	 * @throws
	 */
	public Object payMoney2Staff(){
		//map集合存放前端页面填写的要查询的信息。
		Map<String,String> map = new HashMap<String,String>();
		map.put("re_user_name", "小明");	//收款用户姓名
		map.put("amount", "100");	//金额
		
		Map<String, String> resultMap = new HashMap<String,String>();
		try {
			resultMap.putAll(EnterprisePayService.payMoney2Staff(map));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/** 
	 * 查询付款记录
	 * @author yang.lvsen
	 * @date 2018年5月10日 下午5:07:23
	 * @return Object
	 * @throws
	 */
	public Object payMoney2StafInfof(){
		//map集合存放前端页面填写的要查询的信息。
		Map<String,String> map = new HashMap<String,String>();
		map.put("partner_trade_no", "这里填写需要查询的商户订单号");	//商户订单号	
		
		Map<String, String> resultMap = new HashMap<String,String>();
		try {
			resultMap.putAll(EnterprisePayService.getPayMoney2StaffIno(map));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/**
	 * 查询电子发票
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午4:26:50
	 * @return Object
	 * @throws
	 */
	public Object getInvoiceInfo(){
		//map集合存放前端页面填写的要查询的信息。
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("card_id", "这里填写需要查询的发票ID");	//发票ID	
		
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			resultMap.putAll(InvoiceService.getInvoiceInfo(map));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	

}
