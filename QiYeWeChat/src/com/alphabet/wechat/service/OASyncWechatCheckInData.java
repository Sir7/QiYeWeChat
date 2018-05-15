package com.alphabet.wechat.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.ConstantCommon;
import com.alphabet.common.ErpCommon;
import com.alphabet.common.entity.CheckWorkEntity;
import com.alphabet.wechat.common.HttpClientUtil;
import com.alphabet.wechat.common.WeChatServer;
import com.alphabet.wechat.util.DBUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * OA同步企业微信打卡数据
 * @author yang.lvsen
 * @date 2018年1月16日 下午12:15:16
 */
public class OASyncWechatCheckInData {
	
	
	/**
	 * OA同步企业微信中的打卡记录，只能获取时间跨度不超过三个月的数据，有打卡记录即可获取打卡数据
	 * @return
	 * @throws Exception
	 */
	public static String OASysqiyeWeChatCheckInData() throws Exception{
		System.out.println("------------企业微信--同步打卡记录--开始--------------");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -3);//得到前3个月
		Date formNow3Month = calendar.getTime();
		long startTime = formNow3Month.getTime()/1000;
		long endTime = System.currentTimeMillis()/1000;
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CheckInCorpSecret); // 获取access token
		List<UserEntity> userList = ErpCommon.getUserList();
		List<String> userIdList = new ArrayList<String>();
		List<Map<String,Object>> somedata = new ArrayList<Map<String,Object>>();
		List<String> WechatUserIdList = QiyeWechatOrgUserSynService.getAllWechatUserIdList();
		for(UserEntity ue : userList){
			userIdList.add(ue.getUserId());
		}
		userIdList.retainAll(WechatUserIdList);
		if(userIdList != null && userIdList.size() > 0){
			int size = userIdList.size();
			List<String> tempList = new ArrayList<String>();
			for(int i = 0; i < size; i++){
				tempList.add(userIdList.get(i));
				if(tempList.size()%100==0){	//当同步的用户超过100时，分批获取打卡数据
					somedata.addAll(getCheckInDataList(accessToken, tempList, startTime, endTime));
					tempList.clear();
				}
				if(userIdList.size()%100 != 0 && i == size-1){	//当同步的用户不足100时，不分批
					somedata.addAll(getCheckInDataList(accessToken, tempList, startTime, endTime));
					tempList.clear();
				}
			}
		}
		
		String flag = actionOASysqiyeWeChatCheckInData(somedata);
		if(ErpCommon.isNotNull(flag) && flag.equals("0")){
			System.out.println("------------企业微信--同步打卡记录--结束--------------");
			return flag;
		}
		return "";
	}
	
	//获取打卡记录
	public static List<Map<String,Object>> getCheckInDataList(String accessToken,List<String> userIdList,long startTime,long endTime){
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		updJsonObj.put("opencheckindatatype", 3);	//获取全部打卡类型
		updJsonObj.put("starttime", startTime);
		updJsonObj.put("endtime", endTime);
		updJsonObj.put("useridlist", userIdList);
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		String errcode = jsonObj.getString("errcode")==null?"":jsonObj.getString("errcode").toString();
		if(ErpCommon.isNotNull(errcode) && "0".equals(errcode)){
			JSONArray jsonDataArr = jsonObj.getJSONArray("checkindata");
			for(int i=0;i<jsonDataArr.size();i++){
				JSONObject data = jsonDataArr.getJSONObject(i);
				String userId = data.get("userid")==null?"":data.get("userid").toString();
				String checkinType = data.get("checkin_type")==null?"":data.get("checkin_type").toString();//打卡类型
				String exceptionType = data.get("exception_type")==null?"":data.get("exception_type").toString();//打卡类型
				String checkTime = data.get("checkin_time")==null?"":data.get("checkin_time").toString();//打卡时间(时间戳)
				String locationTitle = data.get("location_title")==null?"":data.get("location_title").toString();//打卡地点
				String locationDetail = data.get("location_detail")==null?"":data.get("location_detail").toString();//打卡地点明细
				String remark = data.get("notes")==null?"":data.get("notes").toString();//备注
				
				if(!"".equals(userId) && ("".equals(exceptionType) || exceptionType == null)){
					Map<String,Object> checkDatamap = new HashMap<String,Object>();
					checkDatamap.put("userId", userId);
					Map<String,String> resoultMap = ErpCommon.getUserInfo(userId);
					if(!resoultMap.isEmpty()){
						checkDatamap.put("userName", resoultMap.get("userName") == null ? "" : resoultMap.get("userName"));
						checkDatamap.put("orgName", resoultMap.get("orgName") == null ? "" : resoultMap.get("orgName"));
					}
					checkDatamap.put("checkinType", checkinType);
					checkDatamap.put("exceptionType", exceptionType);
					checkDatamap.put("checkTime", checkTime);
					checkDatamap.put("locationTitle", locationTitle);
					checkDatamap.put("locationDetail", locationDetail);
					checkDatamap.put("remark", remark);
					dataList.add(checkDatamap);
				}
			}
		}
		return dataList;
	}
	
	/**
	 * OA同步企业微信打卡记录
	 * @param checkInDataList
	 */
	public static String actionOASysqiyeWeChatCheckInData(List<Map<String,Object>> checkInDataList){
		if(checkInDataList != null && checkInDataList.size() > 0){
			Connection conn = null;
			conn = DBUtil.openCesoa();
			PreparedStatement ps = null;
			for(Map<String,Object> map : checkInDataList){
				String userId = map.get("userId").toString();
				if(userId != null && !"".equals(userId)){
					CheckWorkEntity zhubiao = new CheckWorkEntity();
					//zhubiao.setId(UUID.randomUUID().toString().replaceAll("-", ""));
					zhubiao.setEmployeeid(userId);
					zhubiao.setEmployeename(map.get("userName") == null ? "" : map.get("userName").toString());
					zhubiao.setDeptname(map.get("orgName") == null ? "" : map.get("orgName").toString());
					String date = map.get("checkTime").toString();
					Long timestamp = Long.parseLong(date)*1000;    
					String date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(timestamp));   
					String[] strArr = date2.split(" ");
					zhubiao.setCheckdate(strArr[0]);
					zhubiao.setChecktime(date2);
					String locationTitle = map.get("locationTitle") == null ? "" : map.get("locationTitle").toString();
					String locationDetail = map.get("locationDetail") == null ? "" : map.get("locationDetail").toString();
					zhubiao.setPostion(locationTitle + "-"+ locationDetail);
					zhubiao.setIsPhone("3");
					zhubiao.setRemark(map.get("remark") == null ? "" : map.get("remark").toString());

					StringBuffer checksql = new StringBuffer("select count(1) from t_oa_clock_in t where to_char(t.checktime,'yyyy-MM-dd HH24:mi')='"+zhubiao.getChecktime()+"' and t.employeename='"+zhubiao.getEmployeename()+"' ");
					int cn = 0;//DatabaseHandlerDao.newInstance().queryForCount(checksql.toString());
					if(cn==0){
						StringBuffer insertsql = new StringBuffer("insert into t_oa_clock_in "
						+ "(id,"	//或是这里写sys_guid()
						+ "employeeid,"
						+ "employeename,"
						+ "deptname,"
						+ "checkdate,"
						+ "checktime,"
						+ "postion,"
						+ "is_phone,"
						+ "remark)"
						+ " values "
						//+ "('"+zhubiao.getId()+"',"
						+ "'"+zhubiao.getEmployeeid()+"',"
						+ "'"+zhubiao.getEmployeename()+"',"
						+ "'"+zhubiao.getDeptname()+"',"
						+ "to_date('"+zhubiao.getCheckdate()+"','yyyy-MM-dd HH24:mi:ss'),"
						+ "to_date('"+zhubiao.getChecktime()+"','yyyy-MM-dd HH24:mi:ss'),"
						+ "'"+zhubiao.getPostion()+"',"
						+ "'"+zhubiao.getIsPhone()+"',"
						+ "'"+zhubiao.getRemark()+"') ");
						try{
							 ps = conn.prepareStatement(insertsql.toString());
							 ps.execute();
							 conn.commit();
						 }catch(Exception e){
							 System.out.println(e);
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
					}	
				}
			}
			//关闭资源
			DBUtil.close(conn, ps, null);
		}
		return "0";
	}
	
	
	/** 
	 * @Description: 获取审批数据
	 * @author yang.lvsen
	 * @date 2018年5月7日 下午8:50:29
	 * @throws Exception 
	 * @return List<Map<String,Object>>
	 * @throws
	 */
	public static List<Map<String,Object>> getCheckData() throws Exception{
		String accessToken = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CheckCorpSecret);
		String postUrl = "https://qyapi.weixin.qq.com/cgi-bin/corp/getapprovaldata?access_token="+accessToken;
		JSONObject updJsonObj = new JSONObject();
		Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar.DATE, -1);
        long startCheckTime = calendar.getTimeInMillis();
		updJsonObj.put("starttime", startCheckTime);	//获取审批记录的开始时间
		long endCheckTime = System.currentTimeMillis();
		updJsonObj.put("endtime", endCheckTime);		//获取审批记录的结束时间
		String returnMsg = HttpClientUtil.post(postUrl, updJsonObj.toString());
		JSONObject jsonObj = JSONObject.fromObject(returnMsg);
		if("0".equals(jsonObj.getString("errcode"))){
			String count = jsonObj.getString("count") == null ? "" : jsonObj.getString("count"); //总的审批数
			String total = jsonObj.getString("total") == null ? "" : jsonObj.getString("total");
			String next_spnum = jsonObj.getString("next_spnum") == null ? "" : jsonObj.getString("next_spnum");	//最后一个审批单号，要想获取规定时间内的所有审批单，就将这个数据作为下次获取的next_spnum值，这样就可以获取全部的。
			JSONArray jsonDataArr = jsonObj.getJSONArray("data");
			for(int i = 0;i < jsonDataArr.size();i++){
				JSONObject data = jsonDataArr.getJSONObject(i);
				String spname = data.get("spname")==null?"":data.get("spname").toString();	//审批名称(请假，报销，自定义审批名称)
				String applyName = data.get("apply_name")==null?"":data.get("apply_name").toString();//申请人姓名
				String applyOrg = data.get("apply_org")==null?"":data.get("apply_org").toString();//申请人部门
				String approvalName = data.get("approval_name")==null?"":data.get("approval_name").toString();//审批人姓名
				String notifyName = data.get("notify_name")==null?"":data.get("notify_name").toString();//抄送人姓名
				String spStatus = data.get("sp_status")==null?"":data.get("sp_status").toString();//审批状态：1审批中；2 已通过；3已驳回；4已取消；6通过后撤销；10已支付
				String spNum = data.get("sp_num")==null?"":data.get("sp_num").toString();//审批单号
				String mediaids = data.get("mediaids")==null?"":data.get("mediaids").toString();//审批的附件media_id，可使用media/get获取附件
				String applyTime = data.get("apply_time")==null?"":data.get("apply_time").toString();//审批单提交时间
				String applyUserId = data.get("apply_user_id")==null?"":data.get("apply_user_id").toString();//审批单提交者的userid
				if(ErpCommon.isNotNull(spname) && "请假".equals(spname)){
					String leave = data.get("leave")==null?"":data.get("leave").toString();//请假类型
					JSONObject leaveJson = JSONObject.fromObject(leave);
					String timeunit = leaveJson.get("timeunit")==null?"":leaveJson.get("timeunit").toString();//请假时间单位：0半天；1小时
					String leaveType = leaveJson.get("leave_type")==null?"":leaveJson.get("leave_type").toString();//请假类型：1年假；2事假；3病假；4调休假；5婚假；6产假；7陪产假；8其他
					String startTime = leaveJson.get("start_time")==null?"":leaveJson.get("start_time").toString();//请假开始时间.时间戳
					String endTime = leaveJson.get("end_time")==null?"":leaveJson.get("end_time").toString();//请假结束时间。时间戳
					String duration = leaveJson.get("duration")==null?"":leaveJson.get("duration").toString();//请假时长，单位小时
				}else if(ErpCommon.isNotNull(spname) && "报销".equals(spname)){
					String expense = data.get("expense")==null?"":data.get("expense").toString();//请假类型
					JSONObject expenseJson = JSONObject.fromObject(expense);
					String expenseType = expenseJson.get("expense_type")==null?"":expenseJson.get("expense_type").toString();//请假时间单位：0半天；1小时
					String reason = expenseJson.get("reason")==null?"":expenseJson.get("reason").toString();//报销明细事由
					String item = expenseJson.get("item")==null?"":expenseJson.get("item").toString();//请假类型：1年假；2事假；3病假；4调休假；5婚假；6产假；7陪产假；8其他
					JSONObject itemJson = JSONObject.fromObject(item);
					String expenseitemType = itemJson.get("expenseitem_type")==null?"":itemJson.get("expenseitem_type").toString();//请假开始时间.时间戳
					String time = itemJson.get("time")==null?"":itemJson.get("time").toString();//请假结束时间。时间戳
					String sums = itemJson.get("sums")==null?"":itemJson.get("sums").toString();//请假时长，单位小时
					String reason2 = itemJson.get("reason")==null?"":itemJson.get("reason").toString();//报销明细事由
				}
				String comm = data.get("comm")==null?"":data.get("comm").toString();//审批模板信息
				JSONObject commJson = JSONObject.fromObject(comm);
				String applyData = commJson.get("apply_data")==null?"":commJson.get("apply_data").toString();//审批模板信息
				JSONObject applyDataJson = JSONObject.fromObject(applyData);
				String item_1490450365815 = commJson.get("item-1490450365815")==null?"":commJson.get("item-1490450365815").toString();
				JSONObject item_1490450365815Json = JSONObject.fromObject(item_1490450365815);
				String title = item_1490450365815Json.get("title")==null?"":item_1490450365815Json.get("title").toString();//类目名
				String type = item_1490450365815Json.get("type")==null?"":item_1490450365815Json.get("type").toString();//类目类型【 text: "文本", textarea: "多行文本", number: "数字", date: "日期", datehour: "日期+时间",  select: "选择框" 】
				String value = item_1490450365815Json.get("value")==null?"":item_1490450365815Json.get("value").toString();//填写的内容，只有Type是图片时，value是一个数组
				/*
				 * 接下来就是将审批数据存入到相应的审批单中。。。。
				 */
				
			}
		}
		return null;
	}
	
	
}
