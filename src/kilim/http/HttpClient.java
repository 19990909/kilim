/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import kilim.Pausable;
import kilim.nio.EndPoint;
import kilim.nio.NioSelectorScheduler;


/**
 * A very rudimentary HTTP server bound to a specific given port.
 */
public class HttpClient {
    public NioSelectorScheduler nio;


    public HttpClient() throws IOException {
        this.nio = new NioSelectorScheduler();
    }


    public HttpClient(InetSocketAddress remoteAddr, Class<? extends HttpSession> httpSessionClass) throws IOException {
        this.nio = new NioSelectorScheduler();
        this.connect(remoteAddr, httpSessionClass);
    }


    public EndPoint connect(InetSocketAddress remoteAddr, Class<? extends HttpSession> httpSessionClass)
            throws IOException {
        return this.nio.connect(remoteAddr, httpSessionClass);
    }


    public void get(String url) throws Exception {
    }

    public static class MySessionTask extends HttpSession {
        @Override
        public void execute() throws Pausable, Exception {
            HttpRequest request = new HttpRequest();
            request.method = "GET";
            request.uriPath = "/index.html";
            request.addField("Host", "www.163.com");
            request.addField("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12");
            request.addField("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            request.addField("Accept-Language", "zh-cn,zh;q=0.5");
            request.addField("Accept-Encoding", "gzip,deflate");
            request.addField("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            request.addField("Keep-Alive", "115");
            request.addField("Connection", "keep-alive");

            request.writeTo(this.endpoint);
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.readFrom(this.endpoint);
            String s =uncompress(httpResponse.buffer.array(),httpResponse.contentOffset, httpResponse.contentOffset + httpResponse.contentLength);
            System.out.println(httpResponse.status());
            System.out.println(httpResponse.getHeader("Content-Type"));
            System.out.println(s);
        }
    }
    
    public static String uncompress(byte []buf,int offset,int length){
        // 处理压缩过的配置信息的逻辑
        InputStream is = null;
        GZIPInputStream gzin = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder sb=new StringBuilder();
        try {
            is =new ByteArrayInputStream(buf, offset, length);
            gzin = new GZIPInputStream(is);
            isr = new InputStreamReader(gzin, "gb2312"); // 设置读取流的编码格式，自定义编码
            br = new BufferedReader(isr);
            char[] buffer = new char[4096];
            int readlen = -1;
            while ((readlen = br.read(buffer, 0, 4096)) != -1) {
                sb.append(buffer, 0, readlen);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
            }
            catch (Exception e1) {
                // ignore
            }
            try {
                isr.close();
            }
            catch (Exception e1) {
                // ignore
            }
            try {
                gzin.close();
            }
            catch (Exception e1) {
                // ignore
            }
            try {
                is.close();
            }
            catch (Exception e1) {
                // ignore
            }
        }
        return sb.toString();
    }

    static final int TIMEOUT = 10000;


    /**
     * http get调用
     * 
     * @param urlString
     * @return
     */
    private static String invokeURL(String urlString) {
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream urlStream = conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(urlStream));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
            return sb.toString();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "error";
    }


    public static void main(String[] args) throws Exception {
        // System.out.println(invokeURL("http://www.163.com/index.html"));
        HttpClient client = new HttpClient(new InetSocketAddress("www.163.com", 80), MySessionTask.class);
        // client.get("/index.html");

    }
}
