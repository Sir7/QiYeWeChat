package com.alphabet.wechat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import com.alibaba.fastjson.JSONObject;
import com.alphabet.common.ErpCommon;

/** 
 * @Title: AccessTokenUtil
 * @Description: 缓存和刷新access_token
 * @author yang.lvsen
 * @date 2018年5月17日 上午11:18:24
 */
public class AccessTokenUtil {
	
	/**
	 * 开发者需要缓存access_token，用于后续接口的调用（注意：不能频繁调用gettoken接口，否则会受到频率拦截）。
	 *	当access_token失效或过期时，需要重新获取。
	 * 	access_token的有效期通过返回的expires_in来传达，正常情况下为7200秒（2小时），有效期内重复获取返回相同结果，过期后获取会返回新的access_token。
	 * 	由于企业微信每个应用的access_token是彼此独立的，所以进行缓存时需要区分应用来进行存储。
	 * 	access_token至少保留512字节的存储空间。
	 * 	企业微信可能会出于运营需要，提前使access_token失效，开发者应实现access_token失效时重新获取的逻辑。
	 * 
	 * @author yang.lvsen
	 * @date 2018年5月17日下午1:55:40
	 * @param type
	 * @return String
	 */
    public synchronized static String getAccessToken(String type) {//synchronized static可以防止同时被多实例化
        //保存access_token文件名字
        String FileName = "WxTokenUtil.properties";
        try {
            // 从文件中获取token值及时间  
            Properties prop = new Properties();// 属性集合对象
             //获取文件流  
            InputStream fis = AccessTokenUtil.class.getClassLoader().getResourceAsStream(FileName);
            prop.load(fis);// 将属性文件流装载到Properties对象中
            fis.close();// 关闭流
            
            //获取CropID，Secret
            String CropID = prop.getProperty("CropID");
            String corpSecret = ""; 
            if(ErpCommon.isNotNull(type)){
            	if("contcat".equals(type)){
            		corpSecret = "contactSecret";	//通讯录secret
            	}else if("".equals(type)){

            	}
            }
            String Secret = prop.getProperty(corpSecret);
            // 获取accesstoken，初始值为空，第一次调用之后会把值写入文件  
            String access_token = prop.getProperty("access_token");  
            String expires_in = prop.getProperty("expires_in");  
            String last_time = prop.getProperty("last_time");  //上次保存accessToken的时间

            int int_expires_in = Integer.parseInt(expires_in);  
            long long_last_time = Long.parseLong(last_time);
            //得到当前时间 
            long current_time = System.currentTimeMillis();  

            // 每次调用，判断expires_in是否过期，如果token时间超时，重新获取，expires_in有效期为7200  
            if ((current_time - long_last_time) / 1000 >= int_expires_in) {  
                //获取token url
                String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+CropID+"&corpsecret="+Secret;  
                //发送http请求得到json流  
                JSONObject jobject = httpRequest(url);  
                //从json流中获取access_token
                String  j_access_token = jobject.getString("access_token");  
                String  j_expires_in = jobject.getString("expires_in");  
  
                //保存access_token
                if (ErpCommon.isNotNull(j_access_token) && ErpCommon.isNotNull(j_expires_in)) {  
                    prop.setProperty("access_token", j_access_token);  
                    prop.setProperty("expires_in", j_expires_in);  
                    prop.setProperty("last_time", System.currentTimeMillis() + "");  

                    URL url_ = AccessTokenUtil.class.getClassLoader().getResource(FileName);  
                    FileOutputStream fos = new FileOutputStream(new File(url_.toURI()));  
                    prop.store(fos, null);  
                    fos.close();// 关闭流  
                }
                //如果已经过期返回获取到的access_token
                return j_access_token;
            } else {
                //如果没有过期，返回从文件中读取的access_token
                return access_token;  
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }  
  
    
    /**  
     * 请求获取accessToken
     * @author yang.lvsen
     * @date 2018年5月17日下午1:59:17
     * @param requestUrl
     * @return JSONObject
     */ 
    public synchronized static JSONObject httpRequest(String requestUrl) {  
        JSONObject jsonObject = null;  
        StringBuffer buffer = new StringBuffer();  
        try {  
            URL url = new URL(requestUrl);  
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
  
            httpUrlConn.setDoOutput(true);  
            httpUrlConn.setDoInput(true);  
            httpUrlConn.setUseCaches(false);  
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod("GET");  
  
            httpUrlConn.connect();  
  
            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();  
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  
  
            String str = null;  
            while ((str = bufferedReader.readLine()) != null) {  
                buffer.append(str);  
            }  
            bufferedReader.close();  
            inputStreamReader.close();  
            // 释放资源  
            inputStream.close();  
            inputStream = null;  
            httpUrlConn.disconnect();  
            jsonObject = JSONObject.parseObject(buffer.toString());  
        } catch (Exception e) {  
            e.printStackTrace();
        }  
        return jsonObject;  
    }

}
