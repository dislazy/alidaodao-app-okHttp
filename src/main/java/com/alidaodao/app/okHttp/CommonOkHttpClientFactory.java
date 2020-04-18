package com.alidaodao.app.okHttp;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

/**
 *
 * @desc CommonOkHttpClientFactory
 * @author bosong
 * @date 2020/4/18 16:09
 */
public class CommonOkHttpClientFactory extends AbstractFactoryBean<CommonOkHttpClient> {

    /**
     * readTimeoutMilliSeconds
     */
    private long readTimeoutMilliSeconds = 100000;

    /**
     * writeTimeout
     */
    private long writeTimeout = 10000;

    /**
     * connectTimeout
     */
    private long connectTimeout = 15000;
    
    /**
     * isUnSafe
     */
    private boolean isUnSafe = false;

    /**
     * isCheckHostname Whether to verify domain name / IP, only effective when adding self-signed certificate as trust
     */
    private boolean isCheckHostname = true;

    /**
     * certificateFilePaths Add self-signed certificate as trust certificate
     */
    private Resource[] certificateFilePaths;

    /**
     * pkcsFile Use the specified PKCS12 certificate to encrypt and decrypt data (for Alipay, WeChat payment,
     */
    private String pkcsFile = null;

    /**
     * pkcsFilePwd Password for PKCS12 certificate
     */
    private String pkcsFilePwd = null;
    
    @Override
    protected CommonOkHttpClient createInstance() throws Exception {
	return new CommonOkHttpClientBuilder(readTimeoutMilliSeconds, writeTimeout, connectTimeout, isUnSafe, 
		    isCheckHostname, certificateFilePaths, pkcsFile, pkcsFilePwd).build();
    }

    @Override
    public Class<?> getObjectType() {
	return CommonOkHttpClient.class;
    }

    public void setReadTimeoutMilliSeconds(long readTimeoutMilliSeconds) {
        this.readTimeoutMilliSeconds = readTimeoutMilliSeconds;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setUnSafe(boolean unSafe) {
        isUnSafe = unSafe;
    }

    public void setCheckHostname(boolean checkHostname) {
        isCheckHostname = checkHostname;
    }

    public void setCertificateFilePaths(Resource[] certificateFilePaths) {
        this.certificateFilePaths = certificateFilePaths;
    }

    public void setPkcsFile(String pkcsFile) {
        this.pkcsFile = pkcsFile;
    }

    public void setPkcsFilePwd(String pkcsFilePwd) {
        this.pkcsFilePwd = pkcsFilePwd;
    }
}
