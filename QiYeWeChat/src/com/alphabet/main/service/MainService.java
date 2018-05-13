package com.alphabet.main.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.util.DBUtil;

public class MainService {

	//获取�?新OA的所有用户数�?
	public static List<UserEntity> getAllUserInfo(){
		List<UserEntity> userlist = new ArrayList<UserEntity>();
		Connection conn = null;
		conn = DBUtil.openAuth();
		PreparedStatement ps = null;
		String sql = "select u.*,d.id orgid,d.name orgname from T_USER u left join T_LINK_USER_ORGANIZATION ou on u.id=ou.user_id and ou.typing='0' "
				+ "	left join T_ORGANIZATION d on d.id=ou.organization_id order by u.show_order";
		try {
			ps = conn.prepareStatement(sql);
			ResultSet result = ps.executeQuery();
			while (result.next()){
				UserEntity userEntity = new UserEntity();
	          //用户ID
				userEntity.setUserId(result.getString("id"));
				//登录�?
				userEntity.setLoginName(result.getString("login_name"));		
				//警号
				//userEntity.setPoliceId(userInfo.get("JOB_NO")==null?null:userInfo.get("JOB_NO").toString());
				//姓名
				userEntity.setUserName(result.getString("name"));		
				//职务
				//userEntity.setPost(userInfo.get("TITLE")==null?null:userInfo.get("TITLE").toString());
				//岗位
				//userEntity.setStation(userInfo.get("STATION")==null?null:userInfo.get("STATION").toString());
				//电话号码
				userEntity.setMobile(result.getString("mobile"));		
				//固定电话
				userEntity.setTelephone(result.getString("telephone"));		
				//邮箱地址
				userEntity.setEmail(result.getString("email"));		
				//是否是超�?
				userEntity.setIsSuperadmin(result.getString("is_superadmin"));		
				//备注
				//userEntity.setComments(userInfo.get("COMMENTS")==null?null:userInfo.get("COMMENTS").toString());
				//在职状�??
				//userEntity.setOnJob(userInfo.get("ON_JOB")==null?null:userInfo.get("ON_JOB").toString());
				userEntity.setStatus(result.getString("status"));//状�??
				//照片路径
				userEntity.setUrlPath(result.getString("pic_url"));
				//部门ID
				userEntity.setOrgId(result.getString("orgid"));
				//部门名称
				userEntity.setOrgName(result.getString("orgname"));
				userlist.add(userEntity);
	        } 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 if (ps != null){
				 try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		}
		//关闭资源
		DBUtil.close(conn, ps, null);
		
		return userlist;
	}
	//获取�?新OA的所有组�?
	public static List<OrgEntity> getAllOrgInfo(){
		List<OrgEntity> orgEntityList = new ArrayList<OrgEntity>();
		Connection conn = null;
		conn = DBUtil.openAuth();
		PreparedStatement ps = null;
		String sql = "select t.* from T_ORGANIZATION t where t.status='0'";
		try {
			ps = conn.prepareStatement(sql);
			ResultSet result = ps.executeQuery();
			while (result.next()){
				OrgEntity orgEntity=new OrgEntity();
				//组织ID
				orgEntity.setOrgId(result.getString("id"));
				//组织key
				//orgEntity.setOrgKey(orgInfo.getOrganizeCode());
				//组织�?
				orgEntity.setOrgName(result.getString("name"));
				//组织父ID
				orgEntity.setParentId(result.getString("parent_id"));
				if(!"101".equals(result.getString("id")) && ErpCommon.isNull(result.getString("parent_id"))){
					orgEntity.setParentId("101");
				}
				//排序
				orgEntity.setShowOrder(result.getString("show_order"));
				//组织类型
				//orgEntity.setOrgTypeId(orgInfo.getOrganizeTypeID());
				//备注
				//orgEntity.setComments(orgInfo.getComments());
				orgEntityList.add(orgEntity);
	        } 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 if (ps != null){
				 try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		}
		//关闭资源
		DBUtil.close(conn, ps, null);
		
		return orgEntityList;
	}
	
}
