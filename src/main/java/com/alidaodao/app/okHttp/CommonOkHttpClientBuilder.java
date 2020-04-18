package com.alidaodao.app.okHttp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import com.alidaodao.app.okHttp.utils.HttpsUtils;

/**
 *
 * @desc Generic OKHttp package creator
 * @author bosong
 * @date 2020/4/18 16:13
 */
public class CommonOkHttpClientBuilder {
    /**
     *  Read timeout
     */
    private long readTimeoutMilliSeconds;

    /**
     *  writeTimeout
     */
    private long writeTimeout;

    /**
     * connectTimeout
     */
    private long connectTimeout;

    /**
     * isUnSafe : Whether to use an insecure method (do not perform any verification on the certificate), if this parameter is the default value, and no trustee certificate is added, the default CA method is used for verification
     */
    boolean isUnSafe;

    /**
     * isCheckHostname : Whether to verify domain name / IP, only effective when adding self-signed certificate as trust
     */
    boolean isCheckHostname;

    /**
     * certificateFilePaths : Verify the server certificate with the certificate containing the server public key (add self-signed certificate as trust certificate)
     */
    private List<URL> certificateFilePaths;


    /**
     *  pkcsFile : Use the specified PKCS12 certificate to encrypt and decrypt data (for Alipay, WeChat payment, etc.)
     */
    private String pkcsFile;

    /**
     * pkcsFilePwd :Password for PKCS12 certificate
     */
    private String pkcsFilePwd;

    public CommonOkHttpClientBuilder() {
        readTimeoutMilliSeconds = 100000;
        writeTimeout = 10000;
        connectTimeout = 15000;
        isUnSafe = false;
        isCheckHostname = true;
        certificateFilePaths = null;
        pkcsFile = null;
        pkcsFilePwd = null;
    }

    public CommonOkHttpClientBuilder(long readTimeoutMilliSeconds, long writeTimeout, long connectTimeout, boolean isUnSafe,
                                     boolean isCheckHostname, List<URL> certificateFilePaths, String pkcsFile, String pkcsFilePwd) {
        this.readTimeoutMilliSeconds = readTimeoutMilliSeconds;
        this.writeTimeout = writeTimeout;
        this.connectTimeout = connectTimeout;
        this.isUnSafe = isUnSafe;
        this.isCheckHostname = isCheckHostname;
        this.certificateFilePaths = certificateFilePaths;
        this.pkcsFile = pkcsFile;
        this.pkcsFilePwd = pkcsFilePwd;
    }

    public CommonOkHttpClientBuilder(long readTimeoutMilliSeconds, long writeTimeout, long connectTimeout, boolean isUnSafe,
                                     boolean isCheckHostname, Resource[] certificateFilePaths, String pkcsFile, String pkcsFilePwd) {
        this.readTimeoutMilliSeconds = readTimeoutMilliSeconds;
        this.writeTimeout = writeTimeout;
        this.connectTimeout = connectTimeout;
        this.isUnSafe = isUnSafe;
        this.isCheckHostname = isCheckHostname;
        this.certificateFilePaths(certificateFilePaths);
        this.pkcsFile = pkcsFile;
        this.pkcsFilePwd = pkcsFilePwd;
    }

    public CommonOkHttpClientBuilder readTimeoutMilliSeconds(long readTimeoutMilliSeconds) {
        this.readTimeoutMilliSeconds = readTimeoutMilliSeconds;
        return this;
    }

    public CommonOkHttpClientBuilder writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public CommonOkHttpClientBuilder connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public CommonOkHttpClientBuilder unSafe(boolean isUnSafe) {
        this.isUnSafe = isUnSafe;
        return this;
    }

    public CommonOkHttpClientBuilder checkHostname(boolean isCheckHostname) {
        this.isCheckHostname = isCheckHostname;
        return this;
    }

    public void certificateFilePaths(Resource[] certificateFilePathsArr) {
        if (certificateFilePathsArr != null) {
            this.certificateFilePaths = new ArrayList<>(certificateFilePathsArr.length);
            for (Resource certificateFilePath : certificateFilePathsArr) {
                try {
                    certificateFilePaths.add(certificateFilePath.getURL());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

    public CommonOkHttpClientBuilder certificateFilePaths(List<URL> certificateFilePaths) {
        this.certificateFilePaths = certificateFilePaths;
        return this;
    }

    public CommonOkHttpClientBuilder pkcs(String pkcsFile, String pkcsFilePwd) {
        this.pkcsFile = pkcsFile;
        this.pkcsFilePwd = pkcsFilePwd;
        return this;
    }

    public CommonOkHttpClient build() {
        HttpsUtils.SSLParams sslParams = null;
        if (isUnSafe) {
            sslParams = HttpsUtils.getSslSocketFactory(isUnSafe);
        } else {
            if (certificateFilePaths != null && certificateFilePaths.size() > 0) {
                List<InputStream> isList = new ArrayList<>();
                certificateFilePaths.forEach((certificateFilePath) -> {
                    try {
                        isList.add(new FileInputStream(certificateFilePath.getFile()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                sslParams = HttpsUtils.getSslSocketFactory(isCheckHostname, isList.toArray(new InputStream[0]));
            } else if (StringUtils.isNotBlank(pkcsFile) && StringUtils.isNotBlank(pkcsFilePwd)) {
                sslParams = HttpsUtils.getSslSocketFactory(pkcsFile, pkcsFilePwd);
            } else {
                sslParams = HttpsUtils.getSslSocketFactory(isUnSafe);
            }
        }
        return new CommonOkHttpClient(readTimeoutMilliSeconds, writeTimeout, connectTimeout, sslParams);
    }

}
