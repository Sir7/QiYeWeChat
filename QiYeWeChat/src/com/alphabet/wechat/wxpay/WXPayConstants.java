package com.alphabet.wechat.wxpay;

/**
 * 常量
 */
public class WXPayConstants {

	//加密方式
    public enum SignType {
        MD5, HMACSHA256
    }
    
    //校验用户姓名选项
    public enum Check {
    	NO_CHECK, FORCE_CHECK
    }
    
    public static final String DOMAIN_API = "api.mch.weixin.qq.com";	//主域名
    public static final String DOMAIN_API2 = "api2.mch.weixin.qq.com";	//备用域名
    public static final String DOMAIN_APIHK = "apihk.mch.weixin.qq.com";	//香港
    public static final String DOMAIN_APIUS = "apius.mch.weixin.qq.com";	//美国
    
    public static final String mchId = "10000098";	//商户号,微信支付分配的商户号
    public static final String deviceInfo = "013467007045764";	//设备号,微信支付分配的终端设备号
    
    //企业支付应用
    public static final String redPacketAgentId = "3010046";	//企业支付应用AgentId
    public static final String redPacketSecret = "fEZKNZF0UFaoz5_xgxC0rIQxx2VqEAohuUijNR9ZbkI";	//企业支付应用密钥

  	public final static String KEY = "192006250b4c09247ec02edce69f6a2d";	//商户平台API密钥里面设置的key

    public static final String FAIL     = "FAIL";	//失败标识
    public static final String SUCCESS  = "SUCCESS";	//成功标识
    public static final String HMACSHA256 = "HMAC-SHA256";	//使用SHA256加密方式
    public static final String MD5 = "MD5";		//使用MD5加密方式

    public static final String FIELD_SIGN = "sign";
    public static final String FIELD_SIGN_TYPE = "sign_type";
    
    public static final String WXPAY = "wxpay";		//使用微信支付
    public static final String QYWXPAY = "qywxpay";		//使用企业微信
    
    public static final String NO_CHECK = "NO_CHECK";	//校验用户姓名选项,不校验真实姓名
    public static final String FORCE_CHECK = "FORCE_CHECK";		//校验用户姓名选项,强校验真实姓名
    
    public static final String MICROPAY_URL_SUFFIX     = "/pay/micropay";
    public static final String UNIFIEDORDER_URL_SUFFIX = "/pay/unifiedorder";
    public static final String ORDERQUERY_URL_SUFFIX   = "/pay/orderquery";
    public static final String REVERSE_URL_SUFFIX      = "/secapi/pay/reverse";
    public static final String CLOSEORDER_URL_SUFFIX   = "/pay/closeorder";
    public static final String REFUND_URL_SUFFIX       = "/secapi/pay/refund";
    public static final String REFUNDQUERY_URL_SUFFIX  = "/pay/refundquery";
    public static final String DOWNLOADBILL_URL_SUFFIX = "/pay/downloadbill";
    public static final String REPORT_URL_SUFFIX       = "/payitil/report";
    public static final String SHORTURL_URL_SUFFIX     = "/tools/shorturl";
    public static final String AUTHCODETOOPENID_URL_SUFFIX = "/tools/authcodetoopenid";

    // sandbox沙盒
    public static final String SANDBOX_MICROPAY_URL_SUFFIX     = "/sandboxnew/pay/micropay";
    public static final String SANDBOX_UNIFIEDORDER_URL_SUFFIX = "/sandboxnew/pay/unifiedorder";
    public static final String SANDBOX_ORDERQUERY_URL_SUFFIX   = "/sandboxnew/pay/orderquery";
    public static final String SANDBOX_REVERSE_URL_SUFFIX      = "/sandboxnew/secapi/pay/reverse";
    public static final String SANDBOX_CLOSEORDER_URL_SUFFIX   = "/sandboxnew/pay/closeorder";
    public static final String SANDBOX_REFUND_URL_SUFFIX       = "/sandboxnew/secapi/pay/refund";
    public static final String SANDBOX_REFUNDQUERY_URL_SUFFIX  = "/sandboxnew/pay/refundquery";
    public static final String SANDBOX_DOWNLOADBILL_URL_SUFFIX = "/sandboxnew/pay/downloadbill";
    public static final String SANDBOX_REPORT_URL_SUFFIX       = "/sandboxnew/payitil/report";
    public static final String SANDBOX_SHORTURL_URL_SUFFIX     = "/sandboxnew/tools/shorturl";
    public static final String SANDBOX_AUTHCODETOOPENID_URL_SUFFIX = "/sandboxnew/tools/authcodetoopenid";

}
