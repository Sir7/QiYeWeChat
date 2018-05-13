package com.alphabet.wechat.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DBUtil {
//	  private static String url = "jdbc:oracle:thin:@10.10.32.6:1521:ORCL";// 数据库连接，oracle代表链接的
//	  private static String UserName = "fundevelop";// 数据库用户登陆名 ( 也有说是 schema 名字的 )  
//	  private static String Password = "fundevelop";// 密码
	static{
		 try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//获取项目管理服务器  ip\端口\用户名\密码   用于frp上传附件同步操作
	public static Map<String,String> openXmglFtp(){
		Map<String,String> map= new HashMap<String,String>();
		InputStream inputStream = DBUtil.class.getResourceAsStream("/datasource.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
			inputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		map.put("ip", p.getProperty("xmglftp.ip"));
//		String ip = ResourceBundle.getBundle("datasource").getString("xmglftp.ip");
		map.put("port", p.getProperty("xmglftp.port"));
		map.put("username", p.getProperty("xmglftp.username"));
		map.put("password", p.getProperty("xmglftp.password"));
		return map;
	}
	//获取采购管理系统  ip\端口\用户名\密码   用于frp上传附件同步操作
	public static Map<String,String> openScmFtp(){
		Map<String,String> map= new HashMap<String,String>();
		InputStream inputStream = DBUtil.class.getResourceAsStream("/datasource.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
			inputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		map.put("ip", p.getProperty("scmftp.ip"));
		map.put("port", p.getProperty("scmftp.port"));
		map.put("username", p.getProperty("scmftp.username"));
		map.put("password", p.getProperty("scmftp.password"));
		return map;
	}
	
	public static Connection openXmgl(){
		InputStream inputStream = DBUtil.class.getResourceAsStream("/datasource.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
			inputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Connection conn = null;
		try {
			if(null == conn || conn.isClosed()){
				conn = DriverManager.getConnection(p.getProperty("xmgl.jdbc.url"), p.getProperty("xmgl.jdbc.username"), p.getProperty("xmgl.jdbc.password"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	
	//
	public static Connection openAuth(){
		InputStream inputStream = DBUtil.class.getResourceAsStream("/datasource.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
			inputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Connection conn = null;
		try {
			if(null == conn || conn.isClosed()){
				conn = DriverManager.getConnection(p.getProperty("oaauth.jdbc.url"), p.getProperty("oaauth.jdbc.username"), p.getProperty("oaauth.jdbc.password"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	//cesoa
	public static Connection openCesoa(){
		InputStream inputStream = DBUtil.class.getResourceAsStream("/datasource.properties");
		Properties p = new Properties();
		try {
			p.load(inputStream);
			inputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Connection conn = null;
		try {
			if(null == conn || conn.isClosed()){
				conn = DriverManager.getConnection(p.getProperty("cesoa.jdbc.url"), p.getProperty("cesoa.jdbc.username"), p.getProperty("cesoa.jdbc.password"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	public static void close(Connection conn,Statement stt,ResultSet rs){
		try {
			
			if(null != rs){
				rs.close();
			}
			
			if(null != stt){
				stt.close();
			}
			
			if(null != conn){
				conn.close();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//System.out.println(open());
	}
}
