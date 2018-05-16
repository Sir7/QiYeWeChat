<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>  
  <head>  
    <base href="<%=basePath%>">  
      
    <title>This is WeiXinEnterprises @author Engineer-Jsp</title>  
    <meta http-equiv="pragma" content="no-cache">  
    <meta http-equiv="cache-control" content="no-cache">  
    <meta http-equiv="expires" content="0">      
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">  
    <meta http-equiv="description" content="This is my page">  
    <!--  
    <link rel="stylesheet" type="text/css" href="styles.css">  
    -->  
  </head>  
    
  <body>  
  <center>  
    This is WeiXinEnterprises @author Engineer-Jsp. <br>  
    <%  
    String UserID = request.getAttribute("UserID").toString();   
    out.print("UserID:"+UserID);  
     %>  
  </center>  
  </body>  
</html>  