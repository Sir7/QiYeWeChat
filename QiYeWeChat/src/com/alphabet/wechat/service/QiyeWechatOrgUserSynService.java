package com.alphabet.wechat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.alphabet.authsystem.entity.OrgEntity;
import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;
import com.alphabet.wechat.entity.WechatDepartment;
import com.alphabet.wechat.entity.WechatUser;
import com.alphabet.wechat.util.DownloadImage;
import com.alphabet.wechat.util.DownloadNoImage;
import com.alphabet.wechat.util.PropertyUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Title: QiyeWechatOrgUserSynService  
 * @Description: 企业微信的组织人员同步服务类
 * @author yang.lvsen
 * @date 2018年4月26日 下午10:09:28 
 *  
 */
public class QiyeWechatOrgUserSynService {
	
	//头像保存地址
	private static String downloadLocal = PropertyUtil.getByPropName("myapp", "download.images.local");

	
	// 获取access_token
	public static String getToken() throws Exception {
		return WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret); 
	}
	
	
	/**
	 * 检查部门在企业微信里是否存在，不存在则创建（上级部门若不存在则级联创建,返回部门id
	 * @param orgList		企业里的部门
	 * @author fyb
	 * @date 2016-12-05
	 */
	public static String wechatCheckAndCreateDept(List<OrgEntity> orgList, Map<String,String> wechatDeptIdNameMap, String accessToken, String curOrgId) throws Exception{
		
		Boolean isDeptExist = false;
		String curDeptId = null;
		if(wechatDeptIdNameMap.get(curOrgId)!=null || "101".equals(curOrgId)){
			isDeptExist = true;
			curDeptId = "101".equals(curOrgId)?"1":curOrgId;
		}
		
		
		if(!isDeptExist){//部门不存在，则级联创建
			//查找当前部门
			OrgEntity curOrg = null;
			for(OrgEntity org: orgList){
				if(curOrgId.equals(org.getOrgId())){
					curOrg = org;
					break;
				}
			}
			//查找上级部门
			String curOrgParentId = curOrg.getParentId();
			OrgEntity parentOrg = null;
			for(OrgEntity org: orgList){
				if(curOrgParentId.equals(org.getOrgId())){
					parentOrg = org;
					break;
				}
			}
			//查企业微信里上级部门是否存在
			//cesoa里的根部门"CES"(id为"101",parentId为"-1"),对应企业微信里的根部门（id�?1�?
			Boolean isParentDeptExist = false;
			if("101".equals(curOrgParentId) || wechatDeptIdNameMap.get(curOrgParentId)!=null){
				isParentDeptExist = true;
			}
			if(isParentDeptExist==false){ //递归创建上级部门
				wechatCheckAndCreateDept(orgList, wechatDeptIdNameMap, accessToken, curOrgParentId);
			}
			//创建当前部门
			Integer deptId = Integer.valueOf(curOrgId);
			String deptName = curOrg.getOrgName();
			//上级部门为根部门时,parentid直接指定�?1
			Integer parentId = Integer.valueOf("101".equals(curOrgParentId)?"1":curOrgParentId);
			Integer order = Integer.valueOf("10000");
			JSONObject addJsonObj = new JSONObject();
			addJsonObj.put("id", deptId);
			addJsonObj.put("name", deptName);
			addJsonObj.put("parentid", parentId);
			addJsonObj.put("order", order);
			
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token="+accessToken;
			String returnMsg = HttpClientUtil.post(postUrl, addJsonObj.toString());
			curDeptId = curOrgId;
			wechatDeptIdNameMap.put(curOrgId, deptName);
		}
		
		return curDeptId;
	}
	
	/**
	 * 同步部门,返回待删除的部门列表
	 * @return
	 * @throws Exception 
	 * @author fyb
	 * @date 2016-12-02
	 */
	public static List<WechatDepartment> qiyeWechatSynOrgs() throws Exception{
		String accessToken = getToken(); //获取access token
		
		List<OrgEntity> orgList = ErpCommon.getOrgList();				//cesoa 里部门组织
		Map<String,String> orgIdNameMap = ErpCommon.getMapOrgIdName();	//cesoa 里部门组织 id-name map
		List<OrgEntity> orgChildList = orgList;	// = new ArrayList<OrgEntity>();//没有子部门，递归查询
		
//		//测试用例  只同步个别部门 ------------------------
//		for(OrgEntity org:orgList){
//			if("总裁办公室-信息技术部".equals(org.getOrgName()) || "信息技术部-综合组".equals(org.getOrgName())){
//				orgChildList.add(org);
//			}
//		}
//		//---------------------------------------
		
		Map<String,String> wechatDeptIdNameMap = new HashMap<String,String>();		//企业微信 里部门组织 id-name map
		List<WechatDepartment> toDelDeptList = new ArrayList<WechatDepartment>();	//企业微信里待删除的部门列表
		List<WechatDepartment> wechatDeptList = null;								//企业微信里部门列表
		//获取企业微信里的部门
		wechatDeptList = getAllWechatDeptList(wechatDeptIdNameMap,accessToken);
		
		//全部同步 i<orgList.size()�? 测试时仅同步部分orgChildList
		for(int i=0;i<orgChildList.size();i++){
			OrgEntity curOrg = orgChildList.get(i); //cesoa中的�?个部门
			String curOrgId = curOrg.getOrgId();
			String curOrgName = curOrg.getOrgName();
			String curOrgParentId = curOrg.getParentId();
			try {
				if(wechatDeptIdNameMap.get(curOrgId)!=null){ //存在这个部门，若部门名称或上级部门变了则进行更新
					WechatDepartment curDept = null;
					for(int j=0;j<wechatDeptList.size();j++){
						if(curOrgId.equals(wechatDeptList.get(j).getId().toString())){
							curDept = wechatDeptList.get(j);
							break;
						}
					}
					if(curDept==null){
						continue;
					}
					//部门名称或上级部门改变了
					if(!curOrgName.equals(curDept.getName()) || (!curOrgParentId.equals(curDept.getParentid().toString()) && !"101".equals(curOrgParentId))){
						//检查企业微信里上级部门是否存在，不存在则级联创建上级部门
						String wechatCurOrgParentId = wechatCheckAndCreateDept(orgList, wechatDeptIdNameMap, accessToken, curOrgParentId);
						//更新部门--id不能更改
						Integer deptId = curDept.getId();
						String deptName = curOrgName.equals(curDept.getName())?curDept.getName():curOrgName;
						Integer parentId = Integer.valueOf(wechatCurOrgParentId); //curDept.getParentid().toString();
						Integer order = curDept.getOrder();
						JSONObject updJsonObj = new JSONObject();
						updJsonObj.put("id", deptId);
						updJsonObj.put("name", deptName);
						updJsonObj.put("parentid", parentId);
						updJsonObj.put("order", order);
						
						String postUrl2 = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token="+accessToken;
						String returnMsg2 = HttpClientUtil.post(postUrl2, updJsonObj.toString());
						wechatDeptIdNameMap.put(deptId.toString(), deptName);
					}
				}else{
					//企业微信里部门不存在，则创建  --递归创建，若上级部门不存在则先创建上级部门
					wechatCheckAndCreateDept(orgList, wechatDeptIdNameMap, accessToken, curOrgId);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("----企业微信--通讯录同步，部门 "+curOrgName+" 同步失败！--");
			}
		}
		
		//部门创建修改完成后，再循环遍历，列出已经删除了的部门
		for(int i=0;i<wechatDeptList.size();i++){
			WechatDepartment curDept = wechatDeptList.get(i);
			String curDeptId = curDept.getId().toString();
			if(!"1".equals(curDeptId) && orgIdNameMap.get(curDeptId)==null){
				toDelDeptList.add(curDept);
			}
		}
		return toDelDeptList;
	}
	
	//获取企业微信里的所有部门,以及部门 id-name map
	public static List<WechatDepartment> getAllWechatDeptList(Map<String,String> wechatDeptIdNameMap,String accessToken){
		List<WechatDepartment> wechatDeptList = new ArrayList<WechatDepartment>();
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+accessToken+"&id=1";
		String returnMsg = HttpClientUtil.post(postUrl, "");
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		JSONArray jsonDeptArr = jsonObj.getJSONArray("department");
		
		for(int i=0;i<jsonDeptArr.size();i++){
			JSONObject dept = jsonDeptArr.getJSONObject(i);
			String deptId = dept.get("id")==null?"":dept.get("id").toString();
			String deptName = dept.get("name")==null?"":dept.get("name").toString();
			String parentId = dept.get("parentid")==null?"":dept.get("parentid").toString();
			String order = dept.get("order")==null?"":dept.get("order").toString();
			if(!"".equals(deptId)){
				wechatDeptIdNameMap.put(deptId, deptName);
				WechatDepartment wdept = new WechatDepartment();
				wdept.setId(Integer.valueOf(deptId));
				wdept.setName(deptName);
				if(!"".equals(parentId)){
					wdept.setParentid(Integer.valueOf(parentId));
				}
				wdept.setOrder(Integer.valueOf("".equals(order)?"10000":order));
				wechatDeptList.add(wdept);
			}
		}
		return wechatDeptList;
	}
	
	//获取企业微信里所有用户,以及人员 id-name map
	public static List<WechatUser> getAllWechatUserList(Map<String,String> wechatUserIdNameMap, String accessToken) throws Exception{
		List<WechatUser> wechatUserList = new ArrayList<WechatUser>();
		//获取用户信息
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+accessToken+"&department_id=1&fetch_child=1";
		String returnMsg = HttpClientUtil.post(postUrl, "");//HttpXmlClient.post(postUrl, paramsMap);
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		if(jsonObj!= null){
			JSONArray userList = jsonObj.getJSONArray("userlist");
			for(int i=0;i<userList.size();i++){
				JSONObject user = userList.getJSONObject(i);
				String userId = user.getString("userid");
				String userName = user.getString("name");
				wechatUserIdNameMap.put(userId, userName);
				
				List<Integer> departmentlist = new ArrayList<Integer>();
				List<Integer> orderlist = new ArrayList<Integer>();
				JSONArray userDeptArr = user.getJSONArray("department");
				//JSONArray userOrderArr = user.getJSONArray("order");
				for(int k=0;k<userDeptArr.size();k++){
					Integer tdeptId = userDeptArr.getInt(k);
					departmentlist.add(tdeptId);
				}
				//for(int k=0;k<userDeptArr.size();k++){
				//	Integer torder = userOrderArr.getInt(k);
				//	orderlist.add(torder);
				//}
				WechatUser wuser = new WechatUser();
				wuser.setUserid(userId);
				wuser.setName(userName);
				wuser.setDepartment(departmentlist);
				//wuser.setOrder(orderlist);
				wuser.setMobile(user.getString("mobile"));
				wuser.setGender(user.getString("gender"));
				wuser.setEmail(user.getString("email"));
				wuser.setTelephone(user.getString("telephone"));
				wechatUserList.add(wuser);
			}
		}
		return wechatUserList;
	}
	
	//获取企业微信中所有的userId
	public static List<String> getAllWechatUserIdList() throws Exception{
		List<String> userIdList = new ArrayList<String>();
		Map<String,String> wechatUserIdNameMap = new HashMap<String,String>();
		String accessToken = getToken(); // 获取access token
		List<WechatUser> list = getAllWechatUserList(wechatUserIdNameMap, accessToken);
		if(list != null && list.size() > 0){
			for(WechatUser wu : list){
				userIdList.add(wu.getUserid());
			}
			return userIdList;
		}
		return null;
	}
	
	/**
	 * 同步用户,已离职的直接删除
	 * @return
	 * @author fyb
	 * @throws Exception 
	 * @date 2016-12-05
	 */
	public static void qiyeWechatSynUsers() throws Exception{
		//先下载头像
		downloadImagesToLocal();
		
		String accessToken = getToken(); // 获取access token
		List<UserEntity> userList = getToAddUsers();//ErpCommon.getUserList(); //-->获取的是数据库中所有员工
		Map<String,String> wechatUserIdNameMap = new HashMap<String,String>(); //企业微信里所有部门人员 name-id map
		List<WechatUser> wechatUserList = getAllWechatUserList(wechatUserIdNameMap,accessToken);//获取企业微信里的用户
		boolean outofLimit = false;//{"errcode":45009,"errmsg":"api freq out of limit"} 若报这个错则终止新增人员
		
		//测试用例  list --------------------------------
		/*List<UserEntity> testUserList = new ArrayList<UserEntity>();
		for(UserEntity user:userList){
			if("张超2".equals(user.getUserName()) ){
				testUserList.add(user);
			}
		}*/
		//------------------------------------------
		
		//全部同步 使用userList, 测试时仅同步个别人员 用testUserList
		for(UserEntity user:userList){ //testUserList
			try {
				String curUserId = user.getUserId();
				String curUserName = user.getUserName();
				String userMobile = user.getMobile()==null?"":user.getMobile();
				String userEmail = user.getEmail()==null?"":user.getEmail();
				String userOrgId = user.getOrgId()==null?"":user.getOrgId();
				String userTelephone = user.getTelephone()==null?"":user.getTelephone().trim();
				String regexp = "^([0-9]{3,4}\\-[0-9]{7,8}+(\\-[0-9]{1,4})?)|([0-9]{3,4})$";	//书写格式：xxx(x)-xxxxxxx(x)-xxxx
				boolean b = Pattern.matches(regexp, userTelephone);  
				String userUrlpath = user.getUrlPath()==null?"":user.getUrlPath();
				
				if("0".equals(user.getIsSuperadmin())){
					continue;
				}
				if("1".equals(user.getStatus()) && wechatUserIdNameMap.get(curUserId)!=null){ //已离职人员，直接删除
					deleteWechatUser(accessToken, curUserId);
					continue;
				}
				if(wechatUserIdNameMap.get(curUserId)!=null){ //人员存在，按�?更新相关字段 �? 部门
					WechatUser wechatUser = null;
					for(int k=0;k<wechatUserList.size();k++){
						if(curUserId.equals(wechatUserList.get(k).getUserid())){
							wechatUser = wechatUserList.get(k);
							break;
						}
					}
					//座机号更改
					boolean isChangeTel = false;	//默认无需更新
					if((ErpCommon.isNotNull(userTelephone) && b && !userTelephone.equals(wechatUser.getTelephone()) && ErpCommon.isNull(wechatUser.getTelephone())) 
						//|| (ErpCommon.isNull(userTelephone) && ErpCommon.isNotNull(wechatUser.getTelephone()))
						){
						isChangeTel = true;
					}
					boolean isChangeImg = false;
					String mediaId = "";
					if(ErpCommon.isNotNull(userUrlpath) && !"default.jpg".equals(userUrlpath)){
						String url = downloadLocal+"\\"+curUserId+".jpg";
						mediaId = getMediaId(accessToken, "image", url);
						if(ErpCommon.isNotNull(userUrlpath) && ErpCommon.isNotNull(mediaId) && !mediaId.equals(wechatUser.getAvatar_mediaid())){
							isChangeImg = true;
						}
					}
					//更新人员的基本信息或所在的部门
					if(!curUserName.equals(wechatUser.getName())  || !wechatUser.getDepartment().contains(Integer.valueOf(userOrgId)) 
							|| ( wechatUser.getDepartment().contains(1) && !("方艳宾".equals(curUserName) || "张杰".equals(curUserName)))
							|| (ErpCommon.isNull(wechatUser.getEmail()) && ErpCommon.isNotNull(userEmail))	//邮箱变化
							//|| !userMobile.equals(wechatUser.getMobile()) || !userEmail.equals(wechatUser.getEmail())
							|| isChangeTel 
							|| isChangeImg 
						){
						wechatUser.setName(curUserName);
						//wechatUser.setMobile(userMobile);
						if(ErpCommon.isNull(wechatUser.getEmail()) && ErpCommon.isNotNull(userEmail)){
							wechatUser.setEmail(userEmail);
						}
						//座机
						wechatUser.setTelephone(userTelephone);
						//头像
						wechatUser.setAvatar_mediaid(mediaId);
						//boolean isContainsRoot = wechatUser.getDepartment().contains(1);//是否包含根部�?
						//在企业微信里,当前人已经设置了的部门id列表不包含此 部门id才 更新
						wechatUser.getDepartment().clear();
						//if(isContainsRoot){
							//wechatUser.getDepartment().add(1);
						//}
						wechatUser.getDepartment().add(Integer.valueOf(userOrgId));
						updateWechatUser(accessToken, wechatUser);
					}
					
				}else{ //人员不存在，则新增(必须有手机号)
					String msg = "";
					if(outofLimit==false && ErpCommon.isNotNull(userOrgId) && ErpCommon.isNotNull(userMobile)){
						WechatUser wechatUser = new WechatUser();
						wechatUser.setUserid(curUserId);
						wechatUser.setName(curUserName);
						wechatUser.setMobile(userMobile);
						wechatUser.setEmail(userEmail);
						wechatUser.setTelephone(b == true ? userTelephone : "");
						if(ErpCommon.isNotNull(userUrlpath) && !"default.jpg".equals(userUrlpath)){
							String url = downloadLocal+"\\"+curUserId+".jpg";
							String mediaId = getMediaId(accessToken, "image", url);
							wechatUser.setAvatar_mediaid(mediaId);
						}
						List<Integer> department = new ArrayList<Integer>();
						//List<Integer> order = new ArrayList<Integer>();
						department.add(Integer.valueOf(userOrgId));
						wechatUser.setDepartment(department);
						msg = createWechatUser(accessToken, wechatUser);
						if(msg != null && !msg.equals("success")){
							outofLimit = true;
							//continue;
						}
					}
					if(outofLimit == true){
						System.out.println("---"+curUserName+"-----未能加入,错误原因："+msg+"-----");
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("----企业微信--通讯录同步，用户 "+user.getLoginName()+"同步失败！--");
			}
		}
		
		List<UserEntity> toDelUserList = getToDelUsers(); //已离职人员，直接删除
		for(UserEntity user:toDelUserList){
			try {
				String userId = user.getUserId();
				if(wechatUserIdNameMap.get(userId)!=null){
					deleteWechatUser(accessToken, userId);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("----企业微信--通讯录同步，删除用户 "+user.getUserName()+"失败！--");
			}
		}
		
	}
	
	/**
	 * 删除CES企业微信中不再需要的部门
	 * @author fyb
	 * @throws Exception 
	 * @date 2016-12-05
	 */
	public static void qiyeWechatDelNotExistOrgs(List<WechatDepartment> toDelDeptList) throws Exception{
		String accessToken = getToken();
		for(int i=toDelDeptList.size()-1; i>=0; i--){
			WechatDepartment dept = toDelDeptList.get(i);
			try {
				String deptId = String.valueOf(dept.getId());
				// 获取部门成员（详情）
				List<WechatUser> deptUserList = getWechatDeptUser(accessToken, deptId, 1);
				//若部门下还有未删除的人员，人员都挪到根部门下
				for(WechatUser wechatUser:deptUserList){
					wechatUser.getDepartment().clear();
					wechatUser.getDepartment().add(1);
					updateWechatUser(accessToken, wechatUser);
				}
				
				// 删除部门
				deleteWechatDepartment(accessToken, deptId);
				
			} catch (Exception e) {
				System.out.println("----企业微信--通讯录同步，部门 --"+dept.getName()+"--删除失败！------");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 取已离职(待删除)人员列表
	 */
	public static List<UserEntity> getToDelUsers(){
		List<UserEntity> toDealUserList = new ArrayList<UserEntity>();
		//获取所有离职员工
		List<UserEntity> list = ConstantCommon.LIST_USERINFO;
		for(UserEntity ue: list){
			UserEntity user = new UserEntity();
			String status = ue.getStatus();
			if(ErpCommon.isNotNull(status) && !"0".equals(status)){
				user.setUserId(ue.getUserId());
				user.setLoginName(ue.getLoginName());
				user.setOnJob(status);
				toDealUserList.add(user);
			}
		}
		return toDealUserList;
	}
	
	//取在职员工
	public static List<UserEntity> getToAddUsers(){
		List<UserEntity> toAddUserList = new ArrayList<UserEntity>();
		//获取所有离职员工
		List<UserEntity> list = ConstantCommon.LIST_USERINFO;
		for(UserEntity ue: list){
			String status = ue.getStatus();
			if(ErpCommon.isNotNull(status) && "0".equals(status)){
				toAddUserList.add(ue);
			}
		}
		return toAddUserList;
	}
	
	//新增人员
	public static String createWechatUser(String accessToken, WechatUser wechatUser){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token="+accessToken;
		JSONObject newJsonObj = new JSONObject();
		newJsonObj.put("userid", wechatUser.getUserid());
		newJsonObj.put("name", wechatUser.getName());
		newJsonObj.put("gender", "0");
		newJsonObj.put("mobile", wechatUser.getMobile());
		newJsonObj.put("email", wechatUser.getEmail());
		newJsonObj.put("department", wechatUser.getDepartment());
		String regexp = "^([0-9]{3,4}\\-[0-9]{7,8}+(\\-[0-9]{1,4})?)|([0-9]{3,4})$";	//书写格式：xxx(x)-xxxxxxx(x)-xxxx
		boolean b = Pattern.matches(regexp, wechatUser.getTelephone()); 
		if(b){
			newJsonObj.put("telephone", wechatUser.getTelephone());
		}
		if(ErpCommon.isNotNull(wechatUser.getAvatar_mediaid())){
			newJsonObj.put("avatar_mediaid", wechatUser.getAvatar_mediaid());
		}
		String returnMsg = HttpClientUtil.post(postUrl, newJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		System.out.println("---新增异常："+wechatUser.getName()+"  "+msgObj.toString());
		return msgObj.getString("errmsg");
	}
	//更新人员信息
	public static String updateWechatUser(String accessToken, WechatUser wechatUser){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject(); //JSONObject.fromObject(wechatUser);
		updJsonObj.put("userid", wechatUser.getUserid());
		updJsonObj.put("name", wechatUser.getName());
		updJsonObj.put("mobile", wechatUser.getMobile());
		updJsonObj.put("email", wechatUser.getEmail());
		updJsonObj.put("department", wechatUser.getDepartment());
		String regexp = "^([0-9]{3,4}\\-[0-9]{7,8}+(\\-[0-9]{1,4})?)|([0-9]{3,4})$";	//书写格式：xxx(x)-xxxxxxx(x)-xxxx
		boolean b = Pattern.matches(regexp, wechatUser.getTelephone()); 
		if(b){
			updJsonObj.put("telephone", wechatUser.getTelephone());
		}
		if(ErpCommon.isNotNull(wechatUser.getAvatar_mediaid())){
			updJsonObj.put("avatar_mediaid", wechatUser.getAvatar_mediaid());
		}
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		System.out.println("---更新异常："+wechatUser.getName()+"  "+msgObj.toString());
		return msgObj.getString("errmsg");
	}
	//删除人员
	public static String deleteWechatUser(String accessToken, String userId){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token="+accessToken+"&userid="+userId; 
		String returnMsg = HttpClientUtil.post(postUrl, null);
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		System.out.println("---删除异常："+userId+"----"+msgObj.toString());
		return msgObj.getString("errmsg");
	}
	//获取临时素材
	public static String getMediaId(String accessToken,String type,String urlPath){
		String mediaId = MaterialManagementService.uploadMedia(accessToken, type, urlPath);
		if(mediaId != null){
			return mediaId;
		}
		return null;
	}
	
	//创建部门
	public static String createWechatDepartment(String accessToken, WechatDepartment wechatDept){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token="+accessToken;
		JSONObject updJsonObj = JSONObject.fromObject(wechatDept);
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		return msgObj.getString("errmsg");
	}
	
	//更新部门信息
	public static String updateWechatDepartment(String accessToken, WechatDepartment wechatDept){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token="+accessToken;
		JSONObject updJsonObj = JSONObject.fromObject(wechatDept);
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		return msgObj.getString("errmsg");
	}
	
	//删除部门
	public static String deleteWechatDepartment(String accessToken, String deptId){
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/delete?access_token="+accessToken+"&id="+deptId; 
		String returnMsg = HttpClientUtil.post(postUrl, null);
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		if("0".equals(msgObj.getString("errcode"))){
			return "success";
		}
		return msgObj.getString("errmsg");
	}
	
	/**
	 * 获取部门成员详情
	 * @param accessToken
	 * @param deptId
	 * @param fetchChild	1/0:是否递归获取子部门下面的成员
	 * @return
	 */
	public static List<WechatUser> getWechatDeptUser(String accessToken, String deptId, int fetchChild){
		List<WechatUser> deptUserList = new ArrayList<WechatUser>();
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+accessToken+"&department_id="+deptId+"&fetch_child="+fetchChild;
		String returnMsg = HttpClientUtil.post(postUrl, null);
		JSONObject msgObj = JSONObject.fromObject(returnMsg);
		JSONArray userList = msgObj.getJSONArray("userlist");
		for(int i=0;i<userList.size();i++){
			JSONObject user = userList.getJSONObject(i);
			WechatUser wuser = new WechatUser();
			wuser.setUserid(user.getString("userid"));
			wuser.setName(user.getString("name"));
			wuser.setMobile(user.getString("mobile"));
			wuser.setEmail(user.getString("email"));
			List<Integer> department = new ArrayList<Integer>();
			//List<Integer> order = new ArrayList<Integer>();
			JSONArray userDeptArr = user.getJSONArray("department");
			//JSONArray userOrderArr = user.getJSONArray("order");
			for(int k=0;k<userDeptArr.size();k++){
				department.add(userDeptArr.getInt(k));
			}
			//for(int k=0;k<userOrderArr.size();k++){
			//	order.add(userOrderArr.getInt(k));
			//}
			wuser.setDepartment(department);
			//wuser.setOrder(order);
			deptUserList.add(wuser);
		}
		return deptUserList;
	}
	
	
	/**
	 * 更新企业微信里部门的显示顺序
	 * @throws Exception 
	 */
	public static void updateDeptShoworder() throws Exception{
		//查询部门的showorder
		String sql = "select o.organize_id,o.organize_name,showorder,(100000000-nvl(showorder,0)) neworder "+ 
						" from t_org@auth_init o where parent_id is not null and parent_id<>'-1' order by showorder ";
		List<Object[]> orgList = null;//DatabaseHandlerDao.newInstance().queryForList(sql);
		
		System.out.println("------------企业微信--组织部门更新showorder--开始--------------");
		Map<String,String> wechatDeptIdNameMap = new HashMap<String,String>();		//企业微信 里部门组�? id-name map
		List<WechatDepartment> wechatDeptList = null;								//企业微信里部门列�?
		String accessToken = getToken(); //获取access token
		wechatDeptList = getAllWechatDeptList(wechatDeptIdNameMap,accessToken);		//获取企业微信里的部门
		Map<String,String> wechatDeptIdOrderMap = new HashMap<String,String>();
		for(WechatDepartment dept:wechatDeptList){
			wechatDeptIdOrderMap.put(String.valueOf(dept.getId()), String.valueOf(dept.getOrder()));
		}
		for(Object[] obj:orgList){
			String deptId = String.valueOf(obj[0]);
			String deptName = String.valueOf(obj[1]);
			String neworder = String.valueOf(obj[3]);
			try {
				//新显示序号与旧序号不同时更新
				if(!neworder.equals(wechatDeptIdOrderMap.get(deptId))){
					String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/department/update?access_token="+accessToken;
					JSONObject updJsonObj = new JSONObject();
					updJsonObj.put("id", Integer.valueOf(deptId));
					updJsonObj.put("order", Integer.valueOf(neworder));
					String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
					JSONObject msgObj = JSONObject.fromObject(returnMsg);
					if("0".equals(msgObj.getString("errcode"))){
						//System.out.println("----更新showorder--成功--"+deptName+"----");
					}else{
						System.out.println("----更新showorder--失败--"+deptName+"---"+msgObj.getString("errmsg"));
					}
				}
			} catch (Exception e) {
				System.out.println("----更新showorder--失败--执行报错--"+deptName);
			}
		}
		System.out.println("------------企业微信--组织部门更新showorder--结束--------------");
	}
	
	/**
	 * 更新企业微信里用户的显示顺序
	 * @throws Exception 
	 */
	public static void updateUserShoworder() throws Exception{
		//查询用户的showorder
		String sql = "select t.user_id,t.login_name,t.showorder,(100000000-nvl(showorder,0)) neworder "+ 
						" from t_user@auth_init t where status is null order by showorder ";
		List<Object[]> userList = null;//DatabaseHandlerDao.newInstance().queryForList(sql);
		
		System.out.println("------------企业微信--用户更新showorder--开始--------------");
		String accessToken = getToken(); // 获取access token
		Map<String,String> wechatUserIdNameMap = new HashMap<String,String>(); //企业微信里所有部门人�? name-id map
		List<WechatUser> wechatUserList = getAllWechatUserList(wechatUserIdNameMap,accessToken);
		Map<String,List<Integer>> wechatUserIdDeptArrMap = new HashMap<String,List<Integer>>();
		//Map<String,List<Integer>> wechatUserIdOrderArrMap = new HashMap<String,List<Integer>>();
		for(WechatUser wechatUser:wechatUserList){
			//wechatUserIdOrderArrMap.put(wechatUser.getUserid(), wechatUser.getOrder());
			wechatUserIdDeptArrMap.put(wechatUser.getUserid(), wechatUser.getDepartment());
		}
		
		for(Object[] obj:userList){
			String userId = String.valueOf(obj[0]);
			String userName = String.valueOf(obj[1]);
			String neworder = String.valueOf(obj[3]);
			try {
				if(wechatUserIdNameMap.get(userId)!=null){
					//新显示序号与旧序号不同时更新(由于目前企业微信获取用户时返回数据没有包含顺序号order信息，这里就无法比较，只能全部更�?)
					//List<Integer> tmporder = wechatUserIdOrderArrMap.get(userId);
					//if(tmporder!=null && !tmporder.contains(Integer.valueOf(neworder))){}
					
					String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token="+accessToken;
					JSONObject updJsonObj = new JSONObject();
					updJsonObj.put("userid", userId);
					List<Integer> tmpDept = wechatUserIdDeptArrMap.get(userId);
					List<Integer> tmporder = new ArrayList<Integer>();
					for(int i=0;i<tmpDept.size();i++){
						tmporder.add(Integer.valueOf(neworder));
					}
					updJsonObj.put("department", tmpDept);
					updJsonObj.put("order", tmporder);
					String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
					JSONObject msgObj = JSONObject.fromObject(returnMsg);
					if("0".equals(msgObj.getString("errcode"))){
						//System.out.println("----更新showorder--成功--"+userName+"----");
					}else{
						System.out.println("----更新showorder--失败--"+userName+"---"+msgObj.getString("errmsg"));
					}
				}
			} catch (Exception e) {
				System.out.println("----更新showorder--失败--执行报错--"+userName);
			}
		}
		System.out.println("------------企业微信--用户更新showorder--结束--------------");
	}
	
	//下载OA头像至本地
	public static String downloadImagesToLocal(){
		return DownloadImage.downloadImageToLocal();
	}
	
	//下载在职未上传头像名称
	public static void downloadNoImageData(){
		DownloadNoImage.downloadNoImageData();
	}
	
	
	/**  
	 * 邀请成员
	 * 企业可通过接口批量邀请成员使用企业微信，邀请后将通过短信或邮件下发通知。须拥有指定成员、部门或标签的查看权限。
	 * @author yang.lvsen
	 * @date 2018年5月14日下午5:03:46
	 * @param list
	 * @param type
	 * @return List<String>
	 */ 
	public static List<String> inviteMember(List<String> list,String type){
		/**
		 * user, party, tag三者不能同时为空；
		 * 如果部分接收人无权限或不存在，邀请仍然执行，但会返回无效的部分（即invaliduser或invalidparty或invalidtag）;
		 * 非认证企业每天邀请人数不能超过1000, 认证企业每天邀请人数不能超过5000；
		 * 同一用户只须邀请一次，被邀请的用户如果未安装企业微信，在3天内每天会收到一次通知，最多持续3天。
		 * 因为邀请频率是异步检查的，所以调用接口返回成功，并不代表接收者一定能收到邀请消息（可能受上述频率限制无法接收）。
		 */
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/batch/invite?access_token="+accessToken;
			JSONObject updJsonObj = new JSONObject();
			if(ErpCommon.isNotNull(type)){
				if("U".equals(type)){
					updJsonObj.put("user", list);	//成员ID列表, 最多支持1000个。
				}else if("P".equals(type)){
					updJsonObj.put("party", list);	//部门ID列表，最多支持100个。
				}else{
					updJsonObj.put("tag", list);	//标签ID列表，最多支持100个。
				}
			}
			String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
			JSONObject jsonObj = JSONObject.fromObject(returnMsg);
			String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
			if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
				if(ErpCommon.isNotNull(type)){
					String param = "";
					if("U".equals(type)){
						param = "invaliduser";
					}else if("P".equals(type)){
						param = "invalidparty";
					}else{
						param = "invalidtag";
					}
					JSONArray jsonDataArr = jsonObj.getJSONArray(param);
					if(jsonDataArr != null && jsonDataArr.size() != 0){
						List<String> dataList = new ArrayList<String>();
						for(int i =0; i < jsonDataArr.size();i++){
							JSONObject data = jsonDataArr.getJSONObject(i);
							dataList.add(data.toString());
						}
						return dataList;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/** 
	 * 二次验证
	 * @author yang.lvsen
	 * @date 2018年5月16日 下午9:17:59
	 * @param code	当成员登录企业微信或关注微工作台（原企业号）加入企业时，所传递过来的数据
	 * @return String
	 */
	public static String secondaryValidation(String code){
		try {
			String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
			Map<String,String> map = OAuth2Service.getUserInfoByCode(accessToken, code);
			if(!map.isEmpty()){
				String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/authsucc?access_token="+accessToken+"&userid="+map.get("userId");
				String returnMsg = HttpClientUtil.post(postUrl, null);
				JSONObject jsonObj = JSONObject.fromObject(returnMsg);
				String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
				if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
					return "0";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 同步组织用户到企业微信
	 * 同步过程：
	 * 1.同步部门组织(已删除的部门单独列出)
	 * 2.同步人员(已离职的人员直接删除)
	 * 3.删除不再存在的部门
	 */
	public static void qiyeWechatSynOrgUser() throws Exception {
		System.out.println("=========--企业微信--通讯录同步--开始==================");
		List<WechatDepartment> toDelDeptList = qiyeWechatSynOrgs();
		Thread.sleep(300);
		qiyeWechatSynUsers();
		Thread.sleep(300);
		System.out.println("------待删部门：---"+toDelDeptList.size()+"个------");
		qiyeWechatDelNotExistOrgs(toDelDeptList);
		System.out.println("=========--企业微信--通讯录同步--结束===================");
	}

}
