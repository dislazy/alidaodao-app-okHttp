package com.alidaodao.app.okHttp.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @desc Https相关的工具类
 * @author bosong
 * @date 2020/4/18 15:50
 */
public class HttpsUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpsUtils.class);

    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
        public HostnameVerifier hostnameVerifier;
    }

    /**
     * @desc Create a default authentication method or an insecure authentication method
     * @author bosong
     * @date 2020/4/18 15:51
     * @param  isUnSafe true/false
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(boolean isUnSafe) {
        return getSslSocketFactoryBase(null, null, null, isUnSafe, false);
    }

    /**
     * @desc Use specified certificate to encrypt and decrypt data
     * @author bosong
     * @date 2020/4/18 15:51
     * @param  certPath Certificate location
     * @param certPwd Certificate password
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(String certPath, String certPwd) {
        SSLParams sslParams = new SSLParams();
        try (FileInputStream in = new FileInputStream(certPath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] pwdArray = certPwd.toCharArray();
            keyStore.load(in, pwdArray);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, pwdArray);
            KeyManager[] kms = kmf.getKeyManagers();
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(kms, null, new SecureRandom());
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = UnSafeTrustManager;
        } catch (Exception e) {
            logger.error("[getSslSocketFactory] error : [{}]", e.getMessage(), e);
        }
        return sslParams;
    }

    /**
     * @desc https one-way authentication
     * @author bosong
     * @date 2020/4/18 15:55
     * @param  trustManager trustManager
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, null, null, false, true);
    }

    /**
     * @desc https one-way authentication
     * @author bosong
     * @date 2020/4/18 15:55
     * @param  isCheckHostname isCheckHostname
     * @param certificates certificates
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(boolean isCheckHostname, InputStream... certificates) {
        return getSslSocketFactoryBase(null, null, null, false, isCheckHostname, certificates);
    }

    /**
     * @desc https two-way authentication
     * @author bosong
     * @date 2020/4/18 15:56
     * @param  bksFile file
     * @param password pass
     * @param certificates certificates
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, InputStream... certificates) {
        return getSslSocketFactoryBase(null, bksFile, password, false, true, certificates);
    }

    /**
     * @desc https two-way authentication
     * @author bosong
     * @date 2020/4/18 15:57
     * @param  bksFile bksFile
     * @param password password
     * @param trustManager trustManager
     * @return SSLParams
     */
    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, bksFile, password, false, true);
    }

    private static SSLParams getSslSocketFactoryBase(X509TrustManager trustManager, InputStream bksFile, String password, boolean isUnSafe, boolean isCheckHostname, InputStream... certificates) {
        SSLParams sslParams = new SSLParams();
        try {
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            X509TrustManager manager;
            if (trustManager != null) {
                //优先使用用户自定义的TrustManager
                manager = trustManager;
            } else if (trustManagers != null) {
                //然后使用默认的TrustManager
                manager = chooseTrustManager(trustManagers);
            } else {
                if (isUnSafe) {
                    // 使用不安全的TrustManager
                    manager = UnSafeTrustManager;
                } else {
                    // 使用 默认的 TrustManager
                    return null;
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{manager}, null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = manager;
            if (isUnSafe || !isCheckHostname) {
                sslParams.hostnameVerifier = UnSafeHostnameVerifier;
            }
            return sslParams;
        } catch (Exception e) {
            logger.error("[getSslSocketFactoryBase]error : [{}]", e.getMessage(), e);
        }
        return sslParams;
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try (InputStream in = bksFile;) {
            if (in == null || password == null) return null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(in, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKeyStore, password.toCharArray());
            return kmf.getKeyManagers();
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * @desc Add the specified certificate as a trusted certificate
     * @author bosong
     * @date 2020/4/18 16:00
     * @param  certificates certificates
     * @return TrustManager[]
     */
    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // Create a default type of Key Store to store certificates we trust
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certStream : certificates) {
                String certificateAlias = Integer.toString(index++);
                // The certificate factory generates certificates according to the stream of certificate files cert
                Certificate cert = certificateFactory.generateCertificate(certStream);
                // Put cert as a trusted certificate in keyStore
                keyStore.setCertificateEntry(certificateAlias, cert);
                try {
                    if (certStream != null) certStream.close();
                } catch (IOException e) {
                    logger.error("[prepareTrustManager]error : [{}]", e.getMessage(), e);
                }
            }
            //We create a default type Trust Manager Factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (Exception e) {
            logger.error("[prepareTrustManager]error :[{}]", e.getMessage(), e);
        }
        return null;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    public static X509TrustManager UnSafeTrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };

    public static HostnameVerifier UnSafeHostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

}
