package moe.exusiai;

import org.apache.commons.io.FileUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.KeyStore;

public class SslContextProvider {
    private static SSLContext sslContext = null;
    public static SSLContext get() {
        if(sslContext==null) {
            synchronized (SslContextProvider.class) {
                if(sslContext==null) {
                    try {
                        sslContext = SSLContext.getInstance("TLS");
                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        ks.load(new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(Vigna.folder.getPath() + "/" + Vigna.certname))), Vigna.certpassword.toCharArray());
                        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                        kmf.init(ks, Vigna.certpassword.toCharArray());
                        sslContext.init(kmf.getKeyManagers(), null, null);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
        return sslContext;
    }
}
