package com.alphabet.wechat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.alphabet.authsystem.entity.UserEntity;
import com.alphabet.common.ErpCommon;
import com.alphabet.wechat.service.QiyeWechatOrgUserSynService;

/**
 * 下载在职员工已上传的有效头像
 * @author yang.lvsen
 * @date 2018年1月17日 下午3:54:23
 */
public class DownloadImage {
	//获取服务器头像存储路径
	private static String urlPathPrefix = PropertyUtil.getByPropName("myapp", "visit.ip.authsystem");
	//头像下载保存路径
	private static String downloadLocal = PropertyUtil.getByPropName("myapp", "download.images.local");
	
	public static String downloadImageToLocal(){
		List<UserEntity> userList = QiyeWechatOrgUserSynService.getToAddUsers();	//只下载在职员工的头像
		for(UserEntity ue : userList){
			if(ErpCommon.isNull(ue.getUrlPath()) || "default.jpg".equals(ue.getUrlPath())){
				continue;
			}
			String photoUrl = urlPathPrefix + ue.getUrlPath();                                      
			String filePath = downloadLocal;		//文件保存地址
			
		    //创建不同的文件夹目录  
		    File file = new File(filePath);  
		    //判断文件夹是否存在
		    if (!file.exists()) {
		        //如果文件夹不存在，则创建新的的文件夹  
		        file.mkdirs();  
		    }
		    FileOutputStream fileOut = null;  
		    HttpURLConnection conn = null;  
		    InputStream inputStream = null;  
		    try {
		        // 建立链接  
		        URL httpUrl=new URL(photoUrl);  
		        conn=(HttpURLConnection) httpUrl.openConnection();  
		        //以Post方式提交表单，默认get方式  ，请求方法，包括POST和GET  
		        conn.setRequestMethod("GET");  
		        conn.setDoInput(true);    
		        conn.setDoOutput(true);  
		        // post方式不能使用缓存   
		        conn.setUseCaches(false);  
		        //连接指定的资源
		        conn.connect();  
		        //获取网络输入
		        inputStream=conn.getInputStream();  
		        BufferedInputStream bis = new BufferedInputStream(inputStream);  
		        //判断文件的保存路径后面是否以/结尾  
		        if (!filePath.endsWith("/")) {  
		            filePath += "/";  
		        }
		        //写入到文件（注意文件保存路径的后面一定要加上文件的名称）  
		        fileOut = new FileOutputStream(filePath + ue.getUserId() + ".jpg");  
		        BufferedOutputStream bos = new BufferedOutputStream(fileOut);  
		          
		        byte[] buf = new byte[4096];  
		        int length = bis.read(buf);  
		        //保存文件  
		        while(length != -1){
		            bos.write(buf, 0, length);  
		            length = bis.read(buf);  
		        }
		        bos.close();  
		        bis.close();  
		        conn.disconnect();  
		    } catch (Exception e)  {  
		        e.printStackTrace();  
		        System.out.println("抛出异常！！！----userId:---"+ue.getUserId()+"---姓名："+ue.getUserName()+"---头像下载失败！---");  
		    }
		}
	    return "0";
	}
	
}
