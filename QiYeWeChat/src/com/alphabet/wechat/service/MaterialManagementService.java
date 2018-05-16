package com.alphabet.wechat.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.alphabet.common.ErpCommon;

import net.sf.json.JSONObject;

/** 
 * @Title: MaterialManagementService 
 * @Description: 素材管理
 * @author yang.lvsen
 * @date 2018年1月14日 上午11:46:26
 */
public class MaterialManagementService {
	/** 
     * 上传媒体文件 ，不可从企业服务器中将图片上传到企业微信中，暂时只支持从本地上传图片，所以要同步头像要先将图片下载至本地。
     * 将文件上传至微信服务器，服务器返回一个媒体id，之后再将媒体id上传至微信服务器。
     * @param accessToken 接口访问凭证 
     * @param type 媒体文件类型，分别有图片（image）、语音（voice）、视频（video），普通文件(file) 
     * @param media form-data中媒体文件标识，有filename、filelength、content-type等信息
     * @param mediaFileUrl 文件的url 
     * 上传的媒体文件限制
     * 图片（image）:1MB，支持JPG格式 
     * 语音（voice）：2MB，播放长度不超过60s，支持AMR格式 
     * 视频（video）：10MB，支持MP4格式 
     * 普通文件（file）：10MB 
     * */
    public static String uploadMedia(String accessToken, String type, String mediaFileUrl) {  
        // 拼装请求地址  
        String uploadMediaUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
        uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);

        File file = new File(mediaFileUrl);
        if(file.length() > 0){
        	try{
        		//1.建立连接
        		URL url = new URL(uploadMediaUrl);
        		HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();  //打开链接
        		
        		//1.1输入输出设置
        		httpUrlConn.setDoInput(true);
        		httpUrlConn.setDoOutput(true);
        		httpUrlConn.setUseCaches(false); // post方式不能使用缓存
        		
        		//1.2设置请求头信息
        		httpUrlConn.setRequestProperty("Connection", "Keep-Alive");
        		httpUrlConn.setRequestProperty("Charset", "UTF-8");
        		//1.3设置边界
        		String boundary = "----------" + System.currentTimeMillis();
        		httpUrlConn.setRequestProperty("Content-Type","multipart/form-data; boundary="+ boundary);
        		
        		// 请求正文信息
        		//2.将文件头输出到微信服务器
        		StringBuilder sb = new StringBuilder();
        		sb.append("--"+boundary+"\r\n"); // 必须多两道线
        		sb.append("Content-Disposition: form-data;name=\"media\"; filelength=\"" + file.length() + "\";filename=\""+ file.getName() + "\"\r\n");
        		sb.append("Content-Type:application/octet-stream\r\n\r\n");
        		byte[] head = sb.toString().getBytes("utf-8");
        		// 获得输出流
        		OutputStream outputStream = new DataOutputStream(httpUrlConn.getOutputStream());
        		// 将表头写入输出流中：输出表头
        		outputStream.write(head);
        		
        		//3.将文件正文部分输出到微信服务器
        		// 把文件以流文件的方式 写入到微信服务器上
        		DataInputStream in = new DataInputStream(new FileInputStream(file));
        		int bytes = 0;
        		byte[] bufferOut = new byte[1024];
        		while ((bytes = in.read(bufferOut)) != -1) {
        			outputStream.write(bufferOut, 0, bytes);
        		}
        		in.close();
        		
        		//4.将结尾部分输出到微信服务器
        		byte[] foot = ("\r\n--" + boundary + "--\r\n").getBytes("utf-8");// 定义�?后数据分隔线
        		outputStream.write(foot);
        		outputStream.flush();
        		outputStream.close();
        		
        		//5.将微信服务器返回的输入流转换成字符串  
        		InputStream inputStream = httpUrlConn.getInputStream();  
        		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
        		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
        		
        		String str = null;
        		StringBuffer buffer = new StringBuffer();  
        		while ((str = bufferedReader.readLine()) != null) {  
        			buffer.append(str);  
        		}
        		
        		bufferedReader.close();  
        		inputStreamReader.close();  
        		// 释放资源  
        		inputStream.close();  
        		inputStream = null;  
        		httpUrlConn.disconnect();
        		
        		JSONObject jsonObject = JSONObject.fromObject(buffer.toString());  
        		if (jsonObject.has("media_id")) {
        			if ("thumb".equals(type)){
        				return jsonObject.getString("thumb_media_id");
        			}else{
        				return jsonObject.getString("media_id");
        			}
        		} else {
        			System.out.println(jsonObject.toString());
        		}
        		
        	} catch (IOException e) {
        		String error = String.format("上传媒体文件失败："+mediaFileUrl, e);  
        		System.out.println(error);  
        		e.printStackTrace();
        	}
        }
        return null;
    }
    
    
    /** 
	 * 获取临时素材/高清语音素材，通过mediaId从微信服务器上下载媒体数据
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午9:12:56
	 * @param accessToken	接口访问凭证
	 * @param mediaId	媒体文件id/通过JSSDK的uploadVoice接口上传的语音文件id
	 * @param savePath	文件下载的保存路径
	 * @return String
	 * @throws Exception
	 */
	public static String downloadMedia(String accessToken, String mediaId, String savePath, String type) {  
         String filePath = null;
         // 拼接请求地址
         //获取临时素材，媒体文件id
    	 String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
    	 if(ErpCommon.isNotNull(type) && "HD".equals(type)){//获取高清语音素材，mediaId是通过JSSDK的uploadVoice接口上传的语音文件id
    		 /**
    		  * 仅企业微信2.4及以上版本支持。
    		  * 可以使用本接口获取从JSSDK的uploadVoice接口上传的临时语音素材，格式为speex，16K采样率。
    		  * 该音频比上文的临时素材获取接口（格式为amr，8K采样率）更加清晰，适合用作语音识别等对音质要求较高的业务。
    		  */
    		 requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/get/jssdk?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
    	 }
    	 requestUrl = requestUrl.replace("ACCESS_TOKEN", accessToken).replace("MEDIA_ID", mediaId);
         
         try {
             URL url = new URL(requestUrl);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoInput(true);
             conn.setRequestMethod("GET");
  
             if (!savePath.endsWith("/")) {
                 savePath += "/";
             }  
             // 根据内容类型获取扩展名 
             String fileName = conn.getHeaderField("Content-Type");
             String fileExt = fileName.substring(fileName.lastIndexOf("."),fileName.length()); 
             // 将mediaId作为文件名  
             filePath = savePath + mediaId + fileExt;  
  
             /**
              * 注：获取临时素材时，微信返回的结果是一个流形式的临时素材。
              * 我们需要做的就是调用接口，获取http连接的输入流中数据，再将输入流中的数据写入到输出流，再通过输出流生成一张图片。这张图片就是微信返回的临时素材了。
              */
             BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());  
             FileOutputStream fos = new FileOutputStream(new File(filePath));  
             byte[] buf = new byte[8096];  
             int size = 0;  
             while ((size = bis.read(buf)) != -1) {
            	 fos.write(buf, 0, size);  
             }
             fos.close();  
             bis.close();  
  
             conn.disconnect();  
             String info = String.format("下载媒体文件成功，filePath=" + filePath);  
             System.out.println(info);
         } catch (Exception e) {  
             filePath = null;  
             String error = String.format("下载媒体文件失败：%s", e);  
             System.out.println(error);  
         }
         return filePath;  
    }
	
	/**
	 * 上传图片
	 * 上传图片得到图片URL，该URL永久有效
	 * 返回的图片URL，仅能用于图文消息（mpnews）正文中的图片展示；若用于非企业微信域名下的页面，图片将被屏蔽。
	 * 每个企业每天最多可上传100张图片
	 * @author yang.lvsen
	 * @date 2018年5月16日下午5:01:47
	 * @param accessToken
	 * @param mediaFileUrl 图片的上传路径
	 * @return String
	 */
	public static String uploadImg(String accessToken, String mediaFileUrl) {
        // 拼装请求地址  
        String uploadMediaUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN";
        uploadMediaUrl = uploadMediaUrl.replace("ACCESS_TOKEN", accessToken);

        File file = new File(mediaFileUrl);
        if(file.length() > 0){
        	try{
        		//1.建立连接
        		URL url = new URL(uploadMediaUrl);
        		HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();  //打开链接
        		
        		//1.1输入输出设置
        		httpUrlConn.setDoInput(true);
        		httpUrlConn.setDoOutput(true);
        		httpUrlConn.setUseCaches(false); // post方式不能使用缓存
        		
        		//1.2设置请求头信息
        		httpUrlConn.setRequestProperty("Connection", "Keep-Alive");
        		httpUrlConn.setRequestProperty("Charset", "UTF-8");
        		//1.3设置边界
        		String boundary = "----------" + System.currentTimeMillis();
        		httpUrlConn.setRequestProperty("Content-Type","multipart/form-data; boundary="+ boundary);
        		
        		// 请求正文信息
        		//2.将文件头输出到微信服务器
        		StringBuilder sb = new StringBuilder();
        		sb.append("--"+boundary+"\r\n"); // 必须多两道线
        		sb.append("Content-Disposition: form-data;name=\"media\"; filelength=\"" + file.length() + "\";filename=\""+ file.getName() + "\"\r\n");
        		sb.append("Content-Type:image/png\r\n\r\n");
        		byte[] head = sb.toString().getBytes("utf-8");
        		// 获得输出流
        		OutputStream outputStream = new DataOutputStream(httpUrlConn.getOutputStream());
        		// 将表头写入输出流中：输出表头
        		outputStream.write(head);
        		
        		//3.将文件正文部分输出到微信服务器
        		// 把文件以流文件的方式 写入到微信服务器上
        		DataInputStream in = new DataInputStream(new FileInputStream(file));
        		int bytes = 0;
        		byte[] bufferOut = new byte[1024];
        		while ((bytes = in.read(bufferOut)) != -1) {
        			outputStream.write(bufferOut, 0, bytes);
        		}
        		in.close();
        		
        		//4.将结尾部分输出到微信服务器
        		byte[] foot = ("\r\n--" + boundary + "--\r\n").getBytes("utf-8");// 定义�?后数据分隔线
        		outputStream.write(foot);
        		outputStream.flush();
        		outputStream.close();
        		
        		//5.将微信服务器返回的输入流转换成字符串  
        		InputStream inputStream = httpUrlConn.getInputStream();  
        		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
        		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
        		
        		String str = null;
        		StringBuffer buffer = new StringBuffer();  
        		while ((str = bufferedReader.readLine()) != null) {  
        			buffer.append(str);  
        		}
        		
        		bufferedReader.close();  
        		inputStreamReader.close();  
        		// 释放资源  
        		inputStream.close();  
        		inputStream = null;  
        		httpUrlConn.disconnect();
        		
        		JSONObject jsonObject = JSONObject.fromObject(buffer.toString());  
        		return jsonObject.getString("url"); //上传后得到的图片URL。永久有效
        	} catch (IOException e) {
        		String error = String.format("上传图片失败："+mediaFileUrl, e);  
        		System.out.println(error);  
        		e.printStackTrace();
        	}
        }
        return null;
    }
    
	
}
