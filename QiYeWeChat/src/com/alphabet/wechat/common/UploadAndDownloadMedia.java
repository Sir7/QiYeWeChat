package com.alphabet.wechat.common;

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

import net.sf.json.JSONObject;

/** 
 * @Title: UploadAndDownloadMedia 
 * @Description: 文件上传/下载
 * @author yang.lvsen
 * @date 2018年1月14日 上午11:46:26
 *  
 */
public class UploadAndDownloadMedia {
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
        		System.out.println(sb.toString());  
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
	 * 通过mediaId从微信服务器上下载媒体数据
	 * @author yang.lvsen
	 * @date 2018年4月26日 下午9:12:56
	 * @param accessToken	接口访问凭证
	 * @param mediaId	媒体文件id
	 * @param savePath	文件下载的保存路径
	 * @return String
	 * @throws Exception
	 */
	public static String downloadMedia(String accessToken, String mediaId, String savePath) {  
         String filePath = null;  
         // 拼接请求地址  
         String requestUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";  
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
    
}
