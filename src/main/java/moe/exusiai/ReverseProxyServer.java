package moe.exusiai;

import com.google.common.collect.ImmutableMap;
import io.undertow.Undertow;
import io.undertow.server.handlers.proxy.ProxyHandler;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class ReverseProxyServer {

    public static void StartProxyServer() {
        SSLContext sslContext = SslContextProvider.get();
        try {

            ImmutableMap.Builder<String, URI> folderuriImmutableMapBuilder = new ImmutableMap.Builder<String, URI>();
            Iterator<Map.Entry<String, Object>> folderentryIterator = Vigna.proxyFolder.entrySet().iterator();
            while (folderentryIterator.hasNext()) {
                Map.Entry<String, Object> entry = folderentryIterator.next();
                folderuriImmutableMapBuilder
                        .put(entry.getKey(), new URI(String.valueOf(entry.getValue())));
            }

            ImmutableMap.Builder<String, URI> hosturiImmutableMapBuilder = new ImmutableMap.Builder<>();
            Iterator<Map.Entry<String, Object>> hostentryIterator = Vigna.proxyHost.entrySet().iterator();
            System.out.println(Vigna.proxyHost);
            while (hostentryIterator.hasNext()) {
                Map.Entry<String, Object> entry = hostentryIterator.next();
                hosturiImmutableMapBuilder
                        .put(entry.getKey().replaceAll("-", "."), new URI(String.valueOf(entry.getValue())));
            }

            ImmutableMap<String,URI> folderuriImmutableMap = folderuriImmutableMapBuilder.build();
            ImmutableMap<String, URI> hosturiImmutableMap = hosturiImmutableMapBuilder.build();


            ReverseProxyClient reverseProxyClient = new ReverseProxyClient(folderuriImmutableMap, hosturiImmutableMap, new URI("http://info.cern.ch/"));
            final Undertow reverseproxy = Undertow.builder()
                    .addHttpsListener(Vigna.port, "0.0.0.0", sslContext)
                    .setHandler(ProxyHandler.builder().setProxyClient(reverseProxyClient).build())
                    .build();
            reverseproxy.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
