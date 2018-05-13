package com.alphabet.wechat.common;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
 
@SuppressWarnings("resource")
public class HttpClientUtil {
	
    private static Log logger = LogFactory.getLog(HttpClientUtil.class);
    
    /**
     * 调用企业微信服务器 API接口
     * 
     * @author yang.lvsen
     * @param url	请求域名
     * @param parameters	请求参数
     * @return String	服务器返回的数据
     */
    public static String post(String url, String parameters) {
        String body = null;
        //logger.info("parameters:" + parameters);
        if(parameters==null){
        	parameters = "";
        }
		HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        
        if(httpPost != null){	//&& parameters != null && !"".equals(parameters.trim())
            try {
                // 建立一个NameValuePair数组，用于存储欲传送的参数  
            	httpPost.addHeader("Content-type","application/json; charset=utf-8");
            	httpPost.setHeader("Accept", "application/json");
            	httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));

            	//执行请求
                HttpResponse response = httpClient.execute(httpPost);
                
                //状态码
                int statusCode = response.getStatusLine().getStatusCode();
                
                //logger.info("statusCode:" + statusCode);
                if (statusCode != HttpStatus.SC_OK) {
                    logger.error("Method failed:" + response.getStatusLine());
                }
                
                // Read the response body
                body = EntityUtils.toString(response.getEntity());
            
            } catch (IOException e) {
                e.printStackTrace();// 网络错误
            }
        }
        return body;
    }

}
