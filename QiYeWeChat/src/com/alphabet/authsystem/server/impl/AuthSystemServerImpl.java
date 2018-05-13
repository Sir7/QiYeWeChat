package com.alphabet.authsystem.server.impl;

import java.util.List;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.authsystem.server.AuthSystemServer;
import com.alphabet.main.service.MainService;

public class AuthSystemServerImpl implements AuthSystemServer {
	
	//获取所有组织
	@Override
	public List<OrgEntity> getAllChildrenByParentID(String parentID) {
		List<OrgEntity> orgEntityList = MainService.getAllOrgInfo();
		/*List<Map<String,String>> list = ErpCommon.getService(MainService.class).getAllOrgInfo();
		List<OrgEntity> orgEntityList=new ArrayList<OrgEntity>();
		if(list != null && list.size() > 0){
			for(Map<String,String> map : list){
				OrgEntity orgEntity=new OrgEntity();
				//组织ID
				orgEntity.setOrgId(map.get("orgId") == null ? "" : map.get("orgId").toString());
				//组织key
				//orgEntity.setOrgKey(orgInfo.getOrganizeCode());
				//组织�?
				orgEntity.setOrgName(map.get("orgName") == null ? "" : map.get("orgName").toString());
				//组织父ID
				orgEntity.setParentId(map.get("parentId") == null ? "" : map.get("parentId").toString());
				//组织类型
				//orgEntity.setOrgTypeId(orgInfo.getOrganizeTypeID());
				//备注
				//orgEntity.setComments(orgInfo.getComments());
				
				orgEntityList.add(orgEntity);
			}
		}*/
		return orgEntityList;
	}

	
	/**
	 * 获取�?有用户信�?
	 */
	@Override
	public List<UserEntity> getAllUserInfo() {
		List<UserEntity> userlist = MainService.getAllUserInfo();
		/*List<Map<String,String>> list = ErpCommon.getService(MainService.class).getAllUserInfo();
		List<UserEntity> userlist = new ArrayList<UserEntity>();
		
		for(Map<String,String> userInfo : list){
			UserEntity userEntity = new UserEntity();
			//用户ID
			userEntity.setUserId(userInfo.get("userId")==null?null:userInfo.get("userId").toString());
			//登录�?
			userEntity.setLoginName(userInfo.get("loginName")==null?null:userInfo.get("loginName").toString());		
			//警号
			//userEntity.setPoliceId(userInfo.get("JOB_NO")==null?null:userInfo.get("JOB_NO").toString());
			//姓名
			userEntity.setUserName(userInfo.get("userName")==null?null:userInfo.get("userName").toString());		
			//职务
			//userEntity.setPost(userInfo.get("TITLE")==null?null:userInfo.get("TITLE").toString());
			//岗位
			//userEntity.setStation(userInfo.get("STATION")==null?null:userInfo.get("STATION").toString());
			//电话号码
			userEntity.setMobile(userInfo.get("mobile")==null?null:userInfo.get("mobile").toString());		
			//固定电话
			userEntity.setTelephone(userInfo.get("telephone")==null?null:userInfo.get("telephone").toString());		
			//邮箱地址
			userEntity.setEmail(userInfo.get("email")==null?null:userInfo.get("email").toString());		
			//是否是超�?
			userEntity.setIsSuperadmin(userInfo.get("isSuperadmin")==null?null:userInfo.get("isSuperadmin").toString());		
			//备注
			//userEntity.setComments(userInfo.get("COMMENTS")==null?null:userInfo.get("COMMENTS").toString());
			//在职状�??
			//userEntity.setOnJob(userInfo.get("ON_JOB")==null?null:userInfo.get("ON_JOB").toString());
			userEntity.setStatus(userInfo.get("status")==null?null:userInfo.get("status").toString());//状�??
			//照片路径
			userEntity.setUrlPath(userInfo.get("urlPath")==null?null:(FacadeUtil.getUserInfoFacade().getUserOriginUrlPath()+userInfo.get("urlPath").toString()));
			//部门ID
			userEntity.setOrgId(userInfo.get("orgId")==null?null:userInfo.get("orgId").toString());
			//部门名称
			userEntity.setOrgName(userInfo.get("orgName")==null?null:userInfo.get("orgName").toString());
			
			userlist.add(userEntity);
		}*/
		return userlist;
	}

	
}

