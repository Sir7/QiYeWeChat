package com.alphabet.wechat.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.alphabet.common.ErpCommon;


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
            	//一定要设置 Content-Type 要不然服务端接收不到参数 
            	httpPost.addHeader("Content-type","application/json; charset=utf-8");
            	//用于请求json格式的参数
            	httpPost.setHeader("Accept", "application/json");
            	//添加参数
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
    
    
    /**
     * Get请求
     * @author yang.lvsen
     * @date 2018年5月17日上午11:05:15
     * @param url	请求路径
     * @param map 参数集合
     * @return String
     */
    public static String HttpRequestGet(String url, Map<String,Object> map) {
        String body = null;
		//HttpClient httpClient = new DefaultHttpClient();
		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        try {
        	Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        	StringBuffer buffer = new StringBuffer();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                Object key = entry.getKey();
                buffer.append(key);
                buffer.append('=');
                Object value = entry.getValue();
                buffer.append(value);
                if (it.hasNext()) {
                    buffer.append("&");
                }
            }
            if(ErpCommon.isNotNull(buffer)){
            	url = url + "?" + buffer;
            }
        	//带参数请求
        	HttpGet httpGet = new HttpGet(url);
        	httpGet.addHeader("Content-type","application/json; charset=utf-8");
        	httpGet.setHeader("Accept", "application/json");
        	
        	//执行请求
        	CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
            //状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Method failed:" + response.getStatusLine());
            }
            body = EntityUtils.toString(response.getEntity());// 获取响应内容
        } catch (IOException e) {
            e.printStackTrace();// 网络错误
        }
        return body;
    }
    
    /**  
     * Post请求
     * @author yang.lvsen
     * @date 2018年5月17日上午11:05:49
     * @param url
     * @param parameters	请求参数
     * @return String
     */ 
    public static String httpRequestPost(String url, String parameters){
    	String body = null;
        if(parameters == null || "".equals(parameters)){
        	parameters = "";
        }
 		//HttpClient httpClient = new DefaultHttpClient();
 		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();  
    	HttpPost httpPost = new HttpPost(url);
		if(httpPost != null){
            try {
                // 建立一个NameValuePair数组，用于存储欲传送的参数  
            	httpPost.addHeader("Content-type","application/json; charset=utf-8");
            	httpPost.setHeader("Accept", "application/json");
            	httpPost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
            	//执行请求
            	CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
                //状态码
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    logger.error("Method failed:" + response.getStatusLine());
                }
                body = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();// 网络错误
            }
        }
    	return body;
    }
    
    
}
