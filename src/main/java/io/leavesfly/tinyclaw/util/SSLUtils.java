package io.leavesfly.tinyclaw.util;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * SSL 工具类
 * 
 * 提供信任所有证书的 SSL 配置，用于解决企业内网环境下
 * PKIX 证书链验证失败的问题（如中间代理、自签名证书等）。
 */
public class SSLUtils {
    
    private static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    
    /**
     * 获取信任所有证书的 TrustManager
     */
    public static X509TrustManager getTrustAllManager() {
        return TRUST_ALL_MANAGER;
    }
    
    /**
     * 获取信任所有证书的 SSLSocketFactory
     */
    public static SSLSocketFactory getTrustAllSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{TRUST_ALL_MANAGER}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create trust-all SSLSocketFactory", e);
        }
    }
    
    /**
     * 获取不验证主机名的 HostnameVerifier
     */
    public static HostnameVerifier getTrustAllHostnameVerifier() {
        return (hostname, session) -> true;
    }
}
