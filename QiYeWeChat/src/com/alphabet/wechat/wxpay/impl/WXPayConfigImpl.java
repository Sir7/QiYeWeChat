package com.alphabet.wechat.wxpay.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.alphabet.wechat.wxpay.IWXPayDomain;
import com.alphabet.wechat.wxpay.WXPayConfig;

public class WXPayConfigImpl extends WXPayConfig{

    private byte[] certData;
    private static WXPayConfigImpl INSTANCE;

    /**
     * 获取本地商户证书内容。
     * 1、登录微信商户平台- 点击[产品中心 ] - [我的产品 ]，开通“现金红包”，“企业付款到零钱”这两个功能。
     * 2、接着点击[账户中心]-点击[API安全]，下载证书(之前没有申请过证书的需要先申请证书).
     * 3、下载下来的证书有三个文件：apiclient_cert，apiclient_key，rootca
     * @throws Exception
     */
    private WXPayConfigImpl() throws Exception{
        String certPath = "D://CERT/common/apiclient_cert.p12";
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public static WXPayConfigImpl getInstance() throws Exception{
        if (INSTANCE == null) {
            synchronized (WXPayConfigImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WXPayConfigImpl();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 公众账号appid	微信分配的公众账号ID（企业微信corpid即为此appId）
     */
    public String getAppID() {
        return "ww4fa9d32099c5d219";
    }

    /**
     * 商户号,微信支付分配的商户号
     */
    public String getMchID() {
        return "11473623";
    }

    /**
     * key为商户平台API密钥里面设置的key
     */
    public String getKey() {
        return "2ab9071b06b9f739b950ddb41db2690d";
    }

    /**
     * 获取商户证书内容
     */
    public InputStream getCertStream() {
        ByteArrayInputStream certBis;
        certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    /**
     * HTTP(S) 连接超时时间，单位毫秒
     */
    public int getHttpConnectTimeoutMs() {
        return 2000;
    }

    /**
     * HTTP(S) 读数据超时时间，单位毫秒
     */
    public int getHttpReadTimeoutMs() {
        return 10000;
    }

    /**
     * HTTP(S) 读数据超时时间，单位毫秒
     */
    public IWXPayDomain getWXPayDomain() {
        return WXPayDomainSimpleImpl.instance();
    }

    public String getPrimaryDomain() {
        return "api.mch.weixin.qq.com";
    }

    public String getAlternateDomain() {
        return "api2.mch.weixin.qq.com";
    }

    /**
     * 进行健康上报的线程的数量
     */
    @Override
    public int getReportWorkerNum() {
        return 1;
    }

    /**
     * 批量上报，一次最多上报多个数据
     */
    @Override
    public int getReportBatchSize() {
        return 2;
    }
}
