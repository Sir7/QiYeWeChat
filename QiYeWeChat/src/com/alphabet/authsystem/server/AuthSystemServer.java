package com.alphabet.authsystem.server;

import java.util.List;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;

/**
 * 
 * @Title: AuthSystemServer 
 * @Description: 系统管理平台SDK
 * @author yang.lvsen
 * @date 2018年1月8日 下午3:24:28 
 *
 */
public interface AuthSystemServer {
	
	/**
	根据组织ID取该组织以下子组织信息（�?有）（不建议使用）
	**/
	public List<OrgEntity> getAllChildrenByParentID(String parentID) throws Exception;	
	
	/**
	取所有用户信息
	**/
	public List<UserEntity> getAllUserInfo() throws Exception;
	
	
}

