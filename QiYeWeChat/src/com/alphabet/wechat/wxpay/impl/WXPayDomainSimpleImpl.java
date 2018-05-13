package com.alphabet.wechat.wxpay.impl;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;

import com.alphabet.wechat.wxpay.IWXPayDomain;
import com.alphabet.wechat.wxpay.WXPayConfig;
import com.alphabet.wechat.wxpay.WXPayConstants;

/**
 * Created by blaketang on 2017/6/16.
 */
public class WXPayDomainSimpleImpl implements IWXPayDomain {
    private WXPayDomainSimpleImpl(){}
    private static class WxpayDomainHolder{
        private static IWXPayDomain holder = new WXPayDomainSimpleImpl();
    }
    public static IWXPayDomain instance(){
        return WxpayDomainHolder.holder;
    }

    /**
     * 上报域名网络状况
     * @param domain 域名。 比如：api.mch.weixin.qq.com
     * @param elapsedTimeMillis 耗时
     * @param ex 网络请求中出现的异常。
     *           null表示没有异常
     *           ConnectTimeoutException，表示建立网络连接异常
     *           UnknownHostException， 表示dns解析异常
     */
    public synchronized void report(final String domain, long elapsedTimeMillis, final Exception ex) {
        DomainStatics info = domainData.get(domain);
        if(info == null){
            info = new DomainStatics(domain);
            domainData.put(domain, info);
        }

        if(ex == null){ //没异常，success
            if(info.succCount >= 2){    //continue succ, clear error count
                info.connectTimeoutCount = info.dnsErrorCount = info.otherErrorCount = 0;
            }else{
                ++info.succCount;
            }
        }else if(ex instanceof ConnectTimeoutException){
            info.succCount = info.dnsErrorCount = 0;
            ++info.connectTimeoutCount;
        }else if(ex instanceof UnknownHostException){
            info.succCount = 0;
            ++info.dnsErrorCount;
        }else{
            info.succCount = 0;
            ++info.otherErrorCount;
        }
    }

    /**
     * 获取域名
     * @param config 配置
     * @return 域名
     */
    public synchronized DomainInfo getDomain(final WXPayConfig config) {
        DomainStatics primaryDomain = domainData.get(WXPayConstants.DOMAIN_API);
        if(primaryDomain == null || primaryDomain.isGood()) {
            return new DomainInfo(WXPayConstants.DOMAIN_API, true);		//设定该域名为主域名
        }

        long now = System.currentTimeMillis();
        if(switchToAlternateDomainTime == 0){   //first switch
            switchToAlternateDomainTime = now;
            return new DomainInfo(WXPayConstants.DOMAIN_API2, false);
        }else if(now - switchToAlternateDomainTime < MIN_SWITCH_PRIMARY_MSEC){
            DomainStatics alternateDomain = domainData.get(WXPayConstants.DOMAIN_API2);
            if(alternateDomain == null || alternateDomain.isGood() ||
                alternateDomain.badCount() < primaryDomain.badCount()){
                return new DomainInfo(WXPayConstants.DOMAIN_API2, false);
            }else{
                return new DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        }else{  //force switch back
            switchToAlternateDomainTime = 0;
            primaryDomain.resetCount();
            DomainStatics alternateDomain = domainData.get(WXPayConstants.DOMAIN_API2);
            if(alternateDomain != null)
                alternateDomain.resetCount();
            return new DomainInfo(WXPayConstants.DOMAIN_API, true);
        }
    }

    static class DomainStatics {
        final String domain;	//域名
        int succCount = 0;		//成功连接数
        int connectTimeoutCount = 0;	//链接超时个数
        int dnsErrorCount =0;	//dns错误个数
        int otherErrorCount = 0;	//其他错误个数

        DomainStatics(String domain) {
            this.domain = domain;
        }
        void resetCount(){
            succCount = connectTimeoutCount = dnsErrorCount = otherErrorCount = 0;
        }
        boolean isGood(){ return connectTimeoutCount <= 2 && dnsErrorCount <= 2; }
        int badCount(){
            return connectTimeoutCount + dnsErrorCount * 5 + otherErrorCount / 4;
        }
    }
    private final int MIN_SWITCH_PRIMARY_MSEC = 3 * 60 * 1000;  //3 minutes
    private long switchToAlternateDomainTime = 0;
    private Map<String, DomainStatics> domainData = new HashMap<String, DomainStatics>();
}
