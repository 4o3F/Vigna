package moe.exusiai;

import com.google.common.collect.ImmutableMap;
import com.sun.jndi.toolkit.url.Uri;
import io.undertow.client.ClientCallback;
import io.undertow.client.ClientConnection;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReverseProxyClient implements ProxyClient {
    private static final ProxyTarget TARGET = new ProxyTarget() {
    };

    private final UndertowClient client;
    private final ImmutableMap<String, URI> foldermapping;
    private final ImmutableMap<String, URI> hostmapping;
    private final URI defaultTarget;

    public ReverseProxyClient(ImmutableMap<String, URI> foldermapping, ImmutableMap<String, URI> hostmapping, URI defaultTarget) {
        this.client = UndertowClient.getInstance();
        this.foldermapping = foldermapping;
        this.hostmapping = hostmapping;
        this.defaultTarget = defaultTarget;
    }

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {
        return TARGET;
    }

    @Override
    public void getConnection(ProxyTarget target, HttpServerExchange exchange, ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {
        URI targetUri = defaultTarget;
        String host = exchange.getHostAndPort().split(":")[0];
        if (hostmapping.containsKey(host)) {
            targetUri = hostmapping.get(host);
        } else {

            String[] uri = exchange.getRequestURI().split("/", 3);

            String firstUriSegment = uri[1];
            String remaininguri;
            if (uri.length > 2) {
                remaininguri = "/" + uri[2];
            } else {
                remaininguri = "/";
            }

            if (foldermapping.containsKey(firstUriSegment)) {
                // If the first uri segment is in the mapping, update the targetUri
                targetUri = foldermapping.get(firstUriSegment);
                // Strip the request uri from the part that is used to map upon.
                exchange.setRequestURI(remaininguri);
            }
        }

            client.connect(
                    new ConnectNotifier(callback, exchange),
                    targetUri,
                    exchange.getIoThread(),
                    exchange.getConnection().getByteBufferPool(),
                    OptionMap.EMPTY);

    }

    private final class ConnectNotifier implements ClientCallback<ClientConnection> {
        private final ProxyCallback<ProxyConnection> callback;
        private final HttpServerExchange exchange;

        private ConnectNotifier(ProxyCallback<ProxyConnection> callback, HttpServerExchange exchange) {
            this.callback = callback;
            this.exchange = exchange;
        }

        @Override
        public void completed(final ClientConnection connection) {
            final ServerConnection serverConnection = exchange.getConnection();
            serverConnection.addCloseListener(serverConnection1 -> IoUtils.safeClose(connection));
            callback.completed(exchange, new ProxyConnection(connection, "/"));
        }

        @Override
        public void failed(IOException e) {
            callback.failed(exchange);
        }
    }
}
