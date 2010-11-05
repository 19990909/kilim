/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import kilim.Pausable;
import kilim.nio.EndPoint;
import kilim.nio.NioSelectorScheduler;


/**
 * A very rudimentary HTTP client
 */
public class HttpClient {
    private static final String KEEP_ALIVE = "keep-alive";
    public static final String POST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static String USER_AGENT = "Kilim HttpClient";
    public static String CHARSET = "GB2312";
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final int DEFAULT_POOL_SIZE = 5;

    public NioSelectorScheduler nio;

    private int poolSize = DEFAULT_POOL_SIZE;

    private final ConcurrentHashMap<String/* host:port */, BlockingQueue<EndPoint>> endpointCache =
            new ConcurrentHashMap<String, BlockingQueue<EndPoint>>();


    public HttpClient() throws IOException {
        this(new NioSelectorScheduler(), DEFAULT_POOL_SIZE);
    }


    public HttpClient(NioSelectorScheduler scheduler) throws IOException {
        this(scheduler, DEFAULT_POOL_SIZE);
    }


    public HttpClient(NioSelectorScheduler scheduler, int poolSize) throws IOException {
        if (scheduler == null) {
            throw new NullPointerException("null scheduler");
        }
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Illegal pool size,it must great than zero");
        }
        this.nio = scheduler;
        this.poolSize = poolSize;
    }


    private String getCacheKey(URL url) {
        return url.getHost() + ":" + this.getPort(url);
    }


    private int getPort(URL url) {
        return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    }


    public HttpResponse get(String url) throws Pausable, Exception {
        return this.get(new URL(url));
    }


    private HttpResponse get(final URL url) throws Pausable, Exception {
        EndPoint endpoint = this.getEndPoint(url);
        HttpRequest request = new HttpRequest();
        request.method = GET_METHOD;

        request.uriPath = url.getPath();
        request.addField("Host", url.getHost());
        request.addField("User-Agent", USER_AGENT);
        request.addField("Connection", KEEP_ALIVE);

        request.writeTo(endpoint);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.readFrom(endpoint);
        final String respConn = httpResponse.getHeader("Connection");
        if (respConn != null && respConn.equalsIgnoreCase("close")) {
            endpoint.close();
        }
        else {
            this.releaseEndPoint(url, endpoint);
        }
        return httpResponse;
    }


    private String buildQueryString(Map<String, CharSequence> params, String charset) throws IOException {
        if (params == null) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Entry<String, CharSequence>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, CharSequence> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue().toString();
            if (name != null && name.length() > 0 && value != null) {
                if (hasParam) {
                    query.append("&");
                }

                query.append(name).append("=").append(URLEncoder.encode(value, charset));
                hasParam = true;
            }
        }

        return query.toString();
    }


    public HttpResponse post(URL url, Map<String, CharSequence> params) throws Pausable, Exception {
        final String body = this.buildQueryString(params, CHARSET);
        return this.post(url, body);
    }


    public HttpResponse post(String url, Map<String, CharSequence> params) throws Pausable, Exception {
        final String body = this.buildQueryString(params, CHARSET);
        return this.post(new URL(url), body);
    }


    private HttpResponse post(URL url, final String body) throws Pausable, Exception, IOException,
            UnsupportedEncodingException {
        EndPoint endpoint = this.getEndPoint(url);
        HttpRequest request = new HttpRequest();
        request.method = POST_METHOD;

        request.uriPath = url.getPath();
        request.addField("Host", url.getHost());
        request.addField("User-Agent", USER_AGENT);
        request.addField("Content-Type", POST_CONTENT_TYPE);
        request.addField("Connection", KEEP_ALIVE);

        request.getOutputStream().write(body.getBytes(CHARSET));

        request.writeTo(endpoint);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.readFrom(endpoint);
        final String respConn = httpResponse.getHeader("Connection");
        if (respConn != null && respConn.equalsIgnoreCase("close")) {
            endpoint.close();
        }
        else {
            this.releaseEndPoint(url, endpoint);
        }
        return httpResponse;
    }


    private EndPoint getEndPoint(URL url) throws Pausable, Exception {
        final String key = this.getCacheKey(url);
        BlockingQueue<EndPoint> pool = this.endpointCache.get(key);
        if (pool == null) {
            pool = new ArrayBlockingQueue<EndPoint>(this.poolSize);
            BlockingQueue<EndPoint> oldPool = this.endpointCache.putIfAbsent(key, pool);
            if (oldPool != null) {
                pool = oldPool;
            }
        }
        EndPoint head = pool.poll();

        if (head != null && ((SocketChannel) head.sockch).isConnected()) {
            return head;
        }
        if (head != null) {
            head.close();
        }

        head = this.nio.connect(new InetSocketAddress(url.getHost(), this.getPort(url)));
        return head;
    }


    private void releaseEndPoint(URL url, EndPoint ep) throws Exception {
        final String key = this.getCacheKey(url);
        BlockingQueue<EndPoint> pool = this.endpointCache.get(key);
        if (pool == null) {
            pool = new ArrayBlockingQueue<EndPoint>(this.poolSize);
            BlockingQueue<EndPoint> oldPool = this.endpointCache.putIfAbsent(key, pool);
            if (oldPool != null) {
                pool = oldPool;
            }
        }
        if (!pool.offer(ep)) {
            ep.close();
        }
    }

}
