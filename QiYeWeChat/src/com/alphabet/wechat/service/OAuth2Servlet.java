package com.alphabet.wechat.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alphabet.common.ConstantCommon;
import com.alphabet.wechat.common.WeChatServer;

/** 
 * @Title: OAuth2Servlet
 * @Description: 
 * @author yang.lvsen
 * @date 2018年5月16日 下午5:26:26
 */
public class OAuth2Servlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;  
     
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        request.setCharacterEncoding("UTF-8");  
        response.setCharacterEncoding("UTF-8");   
        PrintWriter out = response.getWriter();  
        String code = request.getParameter("code");   
        if (!"authdeny".equals(code)) { 
			try {
				String access_token = WeChatServer.getToken(ConstantCommon.CorpID, ConstantCommon.CorpSecret);
				// agentid 跳转链接时所在的企业应用ID 管理员须拥有agent的使用权限；agentid必须和跳转链接时所在的企业应用ID相同  
				String UserID = Oauth2Service.GetUserID(access_token, code, "您的agentid");  
				request.setAttribute("UserID", UserID);  
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
        } else {  
        	out.print("授权获取失败，至于为什么，自己找原因。。。");  
        }  
        // 跳转到index.jsp  
        request.getRequestDispatcher("index.jsp").forward(request, response);  
    }

}
