package com.alphabet.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;

/** 
 * @Title: ConstantCommon 
 * @Description: 系统常量表
 * @author yang.lvsen
 * @date 2018年1月5日 下午3:18:16 
 *  
 */
public interface ConstantCommon {

	// 系统用户列表
	public List<UserEntity> LIST_USERINFO = new ArrayList<UserEntity>();
	
	// 系统部门列表
	public ArrayList<OrgEntity> LIST_ORGINFO = new ArrayList<OrgEntity>();
	
	// 微信支付请求时需要的数据集合
	public Map<String,String> MAP_WXPAY = new HashMap<String,String>();
	
	// 企业支付请求时需要的数据集合
	public Map<String,String> MAP_QYWXPAY = new HashMap<String,String>();
	
	//企业唯一标识ID
	public static final String CorpID = "ww4fa9d32099c5d219";
	//通讯录同步凭证密钥
	public static final String CorpSecret = "7DSujq5Uu3yepUkscrFGpV9FIoeF0cvenmv7jnJVPMo";
	//打卡应用密钥
	public static final String CheckInCorpSecret = "WPdAzht2o7eniqGjVKHfLYglWHeFPyc_4xdjLRrLv_c";
	//审批应用密钥
	public static final String CheckCorpSecret = "YpOTw2jkZvTUO9ckoDB36xuFFTN4Fvp8_5VmZvUQFso";
	//自建应用群聊密钥
	public static final String GroupChatSecret = "_d5_ktgj2UFwXndJXMiRCua8D3iIknVmmMsHAZ1j2HU";
	//外部联系人密钥
	public static final String ExternalContactSecret = "SGL2DIKcorCsI-Mul3BrYg_QuuAaIROtB3OY2jELohU";
	
	
}
