package moe.exusiai;

import com.google.common.collect.ImmutableMap;
import io.undertow.Undertow;
import io.undertow.server.handlers.proxy.ProxyHandler;

import javax.net.ssl.SSLContext;
import java.net.URI;

public class ReverseProxyServer {

    public static void StartProxyServer() {
        SSLContext sslContext = SslContextProvider.get();
        try {
            ImmutableMap<String, URI> uriImmutableMap = new ImmutableMap.Builder<String, URI>()
                    .put("gameserver", new URI("http://127.0.0.1:10000"))
                    .put("plan", new URI("http://127.0.0.1:20000"))
                    .build();
            ReverseProxyClient reverseProxyClient = new ReverseProxyClient(uriImmutableMap, new URI("http://info.cern.ch/"));
            final Undertow reverseproxy = Undertow.builder()
                    .addHttpsListener(21068, Vigna.domain, sslContext)
                    .setHandler(ProxyHandler.builder().setProxyClient(reverseProxyClient).build())
                    .build();
            reverseproxy.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
