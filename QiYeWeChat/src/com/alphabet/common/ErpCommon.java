package com.alphabet.common;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.service.CommonService;

/**
 * erp公共类
 * 
 * @date 2013-9-23 下午4:08:02
 */
public class ErpCommon {

	/**
	 * 服务器启动时初始化数据方法
	 * 
	 * @date 2013-11-3 下午11:04:25
	 */
	public static void init() {
		System.out.println("正在初始化数据...");
		//清空原有的数据
		getUserList();
		getOrgList();
		System.out.println("初始化数据结束");
		
		//dozerconfigs.add("com/ces/erp/common/dozer.config.xml");
		//DOZER_BEAN_MAPPER = new DozerBeanMapper(dozerconfigs);
	}
	
	//重新加载初始化数据
	public static void reloadData(){
		System.out.println("正在初始化数据...");
		ConstantCommon.LIST_USERINFO.clear();
		ConstantCommon.LIST_ORGINFO.clear();
		getUserList();
		getOrgList();
		System.out.println("初始化数据结束");
    }

	//static List<String> dozerconfigs = new ArrayList<String>();
	//public static DozerBeanMapper DOZER_BEAN_MAPPER = null;

	/*******************************操作用户**********************************/
	
	
	/**
	 * 获取用户对象列表list
	 * 
	 * @return
	 */
	public synchronized static List<UserEntity> getUserList() {
		if (ConstantCommon.LIST_USERINFO.size() == 0) {
			List<UserEntity> userInfoList = CommonService.getAllUserInfo();
			ConstantCommon.LIST_USERINFO.addAll(userInfoList);
			/*UserEntity user = new UserEntity();
			user.setUserId("1");
			user.setUserName("超级管理员");
			user.setLoginName("superadmin");
			ConstantCommon.LIST_USERINFO.add(user);*/
		}
		return ConstantCommon.LIST_USERINFO;
	}
	
	

	/**
	 * 获取用户信息map(key是id，value是loginName)
	 * 
	 * @return
	 */
	public static Map<String, String> getMapUserIdName() {
		Map<String, String> map = new HashMap<String, String>();
		List<UserEntity> list = getUserList();
		for (UserEntity u : list) {
			map.put(u.getUserId(), u.getLoginName());
		}
		return map;
	}

	/**
	 * 获取用户信息map(key是loginName，value是id)
	 * 
	 * @return
	 */
	public static Map<String, String> getMapUserNameId() {
		Map<String, String> map = new HashMap<String, String>();
		List<UserEntity> list = getUserList();
		for (UserEntity u : list) {
			map.put(u.getLoginName(), u.getUserId());
		}
		return map;
	}

	/**
	 * 获取组织信息map(key是OrganizeID，value是OrganizeName)
	 * 
	 * @return
	 */
	public static Map<String, String> getMapOrgIdName() {
		Map<String, String> map = new HashMap<String, String>();
		List<OrgEntity> list = getOrgList();
		for (OrgEntity o : list) {
			map.put(o.getOrgId(), o.getOrgName());
		}
		return map;
	}
	
	/**
	 * 获取组织信息map(key是OrganizeName，value是OrganizeID)
	 * 
	 * @return
	 */
	public static Map<String, String> getMapOrgNameId() {
		Map<String, String> map = new HashMap<String, String>();
		List<OrgEntity> list = getOrgList();
		for (OrgEntity o : list) {
			if(isNotNull(o.getParentId())){//滤掉上级部门为空的
				map.put(o.getOrgName(), o.getOrgId());
			}
		}
		return map;
	}
	
	/*******************************操作部门**********************************/
	/**
	 * 获取组织对象列表list(按showorder字段排序)
	 * 
	 * @return
	 */
	public synchronized static List<OrgEntity> getOrgList() {
		if (ConstantCommon.LIST_ORGINFO.size() == 0) {
			List<OrgEntity> orgEntityList = CommonService.getAllOrgInfo();
			ConstantCommon.LIST_ORGINFO.addAll(orgEntityList);
		}
		return ConstantCommon.LIST_ORGINFO;
	}

	
	/*******************************常用方法**********************************/

	/**
	 * 判断对象是否不为空
	 * 
	 * @date 2013-11-6 下午7:51:24
	 * @param obj
	 * @return
	 */
	public static boolean isNotNull(Object obj) {
		if (obj != null && !"".equals(obj)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @date 2013-10-23 上午12:18:28
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Object obj) {
		return obj == null || "".equals(obj);
	}
	
	//根据userId获取信息
	public static Map<String,String> getUserInfo(String userId){
		List<UserEntity> userList = ConstantCommon.LIST_USERINFO;
		if(userList == null || userList.size() == 0){
			getUserList();
		}
		Map<String,String> map = new HashMap<String,String>();
		if(ErpCommon.isNotNull(userId)){
			for(UserEntity ue : userList){
				if(userId.equals(ue.getUserId())){
					map.put("userName", ue.getUserName());
					map.put("orgName", ue.getOrgName());
				}
			}
		}
		return map;
	}
	
	/**
	 * 获取本机的ip和本机名称
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午2:08:15
	 * @return Map<String,String>
	 */
	public static Map<String,String> getIPAddress(){
		Map<String,String> map = new HashMap<String,String>();
		InetAddress addr;
		String ip = null;
		String addressName = null;
		try {
			addr = InetAddress.getLocalHost();
			ip = InetAddress.getLocalHost().toString();  //获得本机ip 譬如：wjl-PC/192.10.22.125 
			ip = ip.substring(ip.lastIndexOf('/')+1);	 //截取'/'之后的IP（不包括'/'）
			addressName = addr.getHostName().toString();	 //获得本机名称
			map.put("ip", ip);
			map.put("addressName", addressName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	/** 
	 * 时间戳转换成日期 yyyy-MM-dd HH:mm:ss
	 * @author yang.lvsen
	 * @date 2018年5月11日 下午4:16:50
	 * @param timestamp
	 * @return String
	 */
	public static String Timestamp2DateTime(long timestamp){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    Long time = new Long(timestamp);  
	    String d = format.format(time);
	    return d;
	}
	
}
