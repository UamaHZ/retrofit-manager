package cn.com.uama.retrofitmanager;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by liwei on 2017/6/27 18:19
 * Email: liwei@uama.com.cn
 * Description: HTTPS 配置帮助类
 */

public class HttpsHelper {

    public static X509TrustManager getTrustManager(Context context) {
        try {
            return getTrustManager(trustedCertificatesInputStream(context));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509TrustManager getTrustManager(InputStream trustedCertificatesInputStream) {
        try {
            return trustManagerForCertificates(trustedCertificatesInputStream);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns an input stream containing one or more certificate PEM files.
     */
    private static InputStream trustedCertificatesInputStream(Context context) throws IOException {
        // 访问接口的证书
        InputStream certInputStream = context.getAssets().open("cert.pem");
        // 代理证书，配置以方便开发的时候进行抓包
        /*
        代理证书的说明：
        配置使用 https 之后，如果不将代理（Charles 或 Fiddler）证书加入信任列表，将无法通过代理进行抓包。
        将 charles.pem 或 fiddler.cer 或两者放入 app/src/debug/assets 目录下。
        1. 证书名字和格式说明：
        Charles 的证书为 charles.pem，Fiddler 的证书为 fiddler.cer，名字算是一种约定。
        本质上证书格式不会造成影响，Charles 使用 pem 格式，Fiddler 使用 cer 格式是因为这是他们各自导出证书的默认格式。
        2. 证书存放位置说明：
        证书必须放到 app/src/debug/assets 目录下，是因为只有在开发的时候才需要抓包，放到该目录下不会在正式打包的时
        候包含到包里面造成不必要的包体积增加。另外，每个人电脑上安装的代理软件（Charles 或 Fiddler）对应的证书
        可能不一样，为了隔离，我将 /src/debug/assets/charles.pem 和 /src/debug/assets/fiddler.cer
        放到了 app/.gitignore 中（也就是没有将代理证书加入 Git 仓库），每个有抓包需求的开发者自行将自己需要的证书
        放到指定目录下（没有的话手动创建）。
         */
        if (BuildConfig.DEBUG) {
            InputStream proxyInputStream = null;
            // charles 的证书
            InputStream charlesInputStream = null;
            try {
                charlesInputStream = context.getAssets().open("charles.pem");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // fiddler 的证书
            InputStream fiddlerInputStream = null;
            try {
                fiddlerInputStream = context.getAssets().open("fiddler.cer");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (charlesInputStream != null) {
                // 注意 SequenceInputStream 构造的第一个参数不能为 null
                proxyInputStream = new SequenceInputStream(charlesInputStream, fiddlerInputStream);
            } else if (fiddlerInputStream != null) {
                proxyInputStream = fiddlerInputStream;
            }
            return new SequenceInputStream(certInputStream, proxyInputStream);
        } else {
            return certInputStream;
        }
    }

    /**
     * Returns a trust manager that trusts certificates and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a
     * SSLHandshakeException.
     */
    private static X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        char[] password = password();
        KeyStore keyStore = getKeyStore(in, password);

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust manager:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private static char[] password() {
        return "password".toCharArray();
    }

    private static KeyStore getKeyStore(InputStream in, char[] password) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);

        // Put the certificates a key store.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }
        return keyStore;
    }


    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
