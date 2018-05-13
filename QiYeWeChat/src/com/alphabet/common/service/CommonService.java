package com.alphabet.common.service;

import java.util.ArrayList;
import java.util.List;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.authsystem.server.AuthSystemServer;


public class CommonService {

	private static AuthSystemServer authSystemServer;
	
	/**
	 * 获取所有用户列表
	 * @return
	 */
	public static List<UserEntity> getAllUserInfo(){
		List<UserEntity> userInfoList = new ArrayList<UserEntity>();
		try {
			userInfoList = authSystemServer.getAllUserInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userInfoList;
	}
	
	/**
	 * 获取所有部门列表
	 * @return
	 */
	public static List<OrgEntity> getAllOrgInfo(){
		List<OrgEntity> orgInfoList = new ArrayList<OrgEntity>();
		try {
			orgInfoList = authSystemServer.getAllChildrenByParentID(101+"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orgInfoList;
	}
	
	
}
