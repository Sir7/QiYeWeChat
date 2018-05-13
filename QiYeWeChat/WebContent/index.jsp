<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="sitemesh" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="cui" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!doctype html>
<html>
<head>
	<title>企业微信</title>
	<meta charset="utf-8">
	<link rel="icon" href="${ctx}/res/images/favicon.ico" type="image/x-icon" />
	<link rel="shortcut icon" href="${ctx}/res/images/favicon.ico" type="image/x-icon" />
	<meta http-equiv="X-UA-Compatible" content="IE=8; IE=9; IE=EDGE" />
	<meta name="renderer" content="webkit">
	<meta name="apple-mobile-web-app-capable" content="yes">
	
	<link href="${ctx}/resources/cui/cui.min.css" rel="stylesheet"/>
	<script type="text/javascript" src="${ctx}/resources/cui/cui.js"></script>
	<script type="text/javascript" src="${ctx}/static/xarch/js/Xarch.js"></script>
	<script type="text/javascript" src="${ctx}/resources/js/common.js"></script>

	<link rel="stylesheet" href="${ctx}/res/css/pm_global.css" type="text/css"></link>
	<link rel="stylesheet" href="${ctx}/res/css/widthheight100.css" type="text/css"></link>	
		
	<script type="text/javascript" src="${ctx}/res/js/pm_common.js"></script>
	
<style>
.project_header{
	position: static;
}
.project_iframe{
	margin: 0;
}
</style>
<script>

	function exit(){
		window.location = '${ctx }/logout';
	}
	
	function reloadInitData(){
		$.message('重新加载系统初始化数据执行中。。。');
		document.getElementById("btn_reloadInitData").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!reloadInitData.json',
			data:{},
			dataType:'json',
			success:function(data){
				$.message('重新加载系统初始化数执行完成');
				document.getElementById("btn_reloadInitData").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_reloadInitData").disabled=false;
				alert(msg);
			}
		});
	}

	function dingtalkSysOrgUser(){
		$.message('钉钉用户同步执行中。。。');
		document.getElementById("btn_synDingtalkOrgUser").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/dingtalk-agent!cesDingtalkSynOrgUser.json',
			data:{},
			dataType:'json',
			success:function(data){
				$.message('钉钉用户同步执行完成');
				document.getElementById("btn_synDingtalkOrgUser").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_synDingtalkOrgUser").disabled=false;
				alert(msg);
			}
		});
	}
	
	function qiyeWechatSysOrgUser(){
		$.message('企业微信用户同步执行中，请稍候。。。');
		document.getElementById("btn_synWechatOrgUser").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!qiyeWechatSynOrgUser.json',
			data:{},
			dataType:'json',
			success:function(data){
				$.message('企业微信用户同步执行完成');
				document.getElementById("btn_synWechatOrgUser").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_synWechatOrgUser").disabled=false;
				alert(msg);
			}
		});
	}
	
	function qiyeWechatSysOrgUserShoworder(){
		$.message('企业微信组织用户showorder更新中，请稍候。。。');
		document.getElementById("btn_synWechatOrgUserShoworder").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!qiyeWechatUpdateOrgUserShoworder.json',
			data:{},
			dataType:'json',
			success:function(data){
				$.message('企业微信组织用户showorder更新完成');
				document.getElementById("btn_synWechatOrgUserShoworder").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_synWechatOrgUserShoworder").disabled=false;
				alert(msg);
			}
		});
	}
	
	function OASysqiyeWeChatCheckInData(){
		$.message('OA同步企业微信打卡记录执行中，请稍候。。。');
		document.getElementById("btn_synOACheckInData").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!OASysqiyeWeChatCheckInData.json',
			data:{},
			dataType:'json',
			success:function(data){
				if(data != null && data == '0'){
					$.message('OA同步企业微信打卡记录完成！');
				}else{
					$.message('OA同步企业微信打卡记录失败！');
				}
				document.getElementById("btn_synOACheckInData").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_synOACheckInData").disabled=false;
				alert(msg);
			}
		});
	}
	
	function DownloadImageToLocal(){
		$.message('OA同步企业微信打卡记录执行中，请稍候。。。');
		document.getElementById("btn_downloadImage").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!DownloadImageToLocal.json',
			data:{},
			dataType:'json',
			success:function(data){
				if(data != null && data == '0'){
					$.message('下载头像至本地完成！');
				}else{
					$.message('下载头像至本地失败！');
				}
				document.getElementById("btn_downloadImage").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_downloadImage").disabled=false;
				alert(msg);
			}
		});
	}
	
	function DownloadImageToLocal2(){
		$.message('OA同步企业微信打卡记录执行中，请稍候。。。');
		document.getElementById("btn_downloadImage2").disabled=true;
		jQuery.ajax({
			type:'post',
			url:'${ctx}/qiye-wechat-agent!DownloadNoImageData.json',
			data:{},
			dataType:'json',
			success:function(data){
				if(data != null && data == '0'){
					$.message('下载未上传头像名单完成！');
				}else{
					$.message('下载未上传头像名单失败！');
				}
				document.getElementById("btn_downloadImage2").disabled=false;
			},
			error:function(msg){
				document.getElementById("btn_downloadImage2").disabled=false;
				alert(msg);
			}
		});
	}
	
</script>
</head>
<body>

	<!--========= 用户设置user_setup_menu start ========-->
	<%-- <div class="pop_usersetup">
		<ul class="usersetup_list">
			<li class="row3" onClick="exit()">注销</li>
		</ul>
	</div>
	
	<div class="project_struct fill" id="project_struct">
		<div class="project_header">
			<table width="100%" border="0" cellspacing="0" cellpadding="0" class="project_header_table">
		    	<tbody>
		       		<tr>
            			<td class="project_h_col1">
    						<img src="${ctx}/res/images/project_logo.png" alt="" title="" border="0" />
            			</td>
            			<td class="project_h_col2">&nbsp;</td>
           	 			<td class="project_h_col3">
			                <ul class="p_head_menu_lll clearfix">
			                    <li class="login_headphoto">
			                    	<span class="userheadpic">
			                    		<img src="${ctx}/res/images/birthman/default_face.jpg" alt="" border="0" />
			                    	</span>
			                    	<em class="user_name"><div class="usernameword"><sec:authentication property="principal.name"/></div></em><i class="usermore">&nbsp;</i>
			                    </li>
			                    <li class="p_headm_but head_menu_icon01" onclick="javascript:frames['iframe_big'].location='${ctx}/pages/project_flowinfo.jsp'">手机版</li>
	                		</ul>
	            		</td>
	          		</tr>
        		</tbody>
    		</table>
		</div> --%>

		<div class="project_iframe coral-adjusted" id="project_iframe">
			<!--  
			<iframe src="${ctx}/pages/index_workbench.jsp" id="project_iframe_in" class="fill" name="iframe_big" border="0" marginwidth="0" 
					marginheight="0" scrolling="yes" frameborder="no" width="100%" height="100%"></iframe>
			-->
			
			<br />
			<ul class="oasys_headmenu_lll clearfix"  id="oa_main_menu">
          		<li><span><a href="javascript:void(0)" >企业微信  API</a></span></li>
          	</ul>
          	<br />
          		<input type ="button" id="btn_reloadInitData" value="重新加载初始化数据(组织用户等)" onclick="reloadInitData()" style=""/>
          	<br />
          	<br />
          	<br />
          		<input type ="button" id="btn_synDingtalkOrgUser" value="同步钉钉组织用户" onclick="dingtalkSysOrgUser()" style=""/>
          	<br />
          	<br />
          	<br />
          		<input type ="button" id="btn_synWechatOrgUser" value="同步企业微信组织用户" onclick="qiyeWechatSysOrgUser()" style=""/>
          	<br />
          	<br />
          	<br />
          		<input type ="button" id="btn_synWechatOrgUserShoworder" value="更新企业微信组织用户显示序号showorder" onclick="qiyeWechatSysOrgUserShoworder()" style=""/>
          	<br />
          	<br />
          	<br />
          		<input type ="button" id="btn_synOACheckInData" value="OA同步企业微信用户打卡记录" onclick="OASysqiyeWeChatCheckInData()" style=""/>
          	<br />
          	<br />
          	<br />
          		<input type ="button" id="btn_downloadImage" value="下载头像至本地(用于同步头像)" onclick="DownloadImageToLocal()" style=""/>
          	<br />
          	<br />
          	<!-- <br />
          	<br />
          		<input type ="button" id="btn_downloadImage2" value="在职未上传名单" onclick="DownloadImageToLocal2()" style=""/>
          	<br />
          	<br /> -->
		</div>
	</div>

</body>
</html>