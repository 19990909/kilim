/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import kilim.Constants;
import kilim.Pausable;
import kilim.nio.EndPoint;
import kilim.nio.ExposedBaos;


/**
 * The response object encapsulates the header and often, but not always, the
 * content. The caller must set all the fields, except for the protocol, server
 * and date. The body of the response (the content) is written to a stream
 * obtained from {@link #getOutputStream()}.
 */
public class HttpResponse extends HttpMsg {
    // Status codes
    public static final byte[] ST_CONTINUE = "100 Continue\r\n".getBytes();
    public static final byte[] ST_SWITCHING_PROTOCOLS = "101 Switching Protocols\r\n".getBytes();

    // Successful status codes

    public static final byte[] ST_OK = "200 OK\r\n".getBytes();
    public static final byte[] ST_CREATED = "201 Created\r\n".getBytes();
    public static final byte[] ST_ACCEPTED = "202 Accepted\r\n".getBytes();
    public static final byte[] ST_NON_AUTHORITATIVE = "203 Non-Authoritative Information\r\n".getBytes();
    public static final byte[] ST_NO_CONTENT = "204 No Content\r\n".getBytes();
    public static final byte[] ST_RESET_CONTENT = "205 Reset Content\r\n".getBytes();
    public static final byte[] ST_PARTIAL_CONTENT = "206 Partial Content\r\n".getBytes();

    // Redirection status codes

    public static final byte[] ST_MULTIPLE_CHOICES = "300 Multiple Choices\r\n".getBytes();
    public static final byte[] ST_MOVED_PERMANENTLY = "301 Moved Permanently\r\n".getBytes();
    public static final byte[] ST_FOUND = "302 Found\r\n".getBytes();
    public static final byte[] ST_SEE_OTHER = "303 See Other\r\n".getBytes();
    public static final byte[] ST_NOT_MODIFIED = "304 Not Modified\r\n".getBytes();
    public static final byte[] ST_USE_PROXY = "305 Use Proxy\r\n".getBytes();
    public static final byte[] ST_TEMPORARY_REDIRECT = "307 Temporary Redirect\r\n".getBytes();

    // Client error codes

    public static final byte[] ST_BAD_REQUEST = "400 Bad Request\r\n".getBytes();
    public static final byte[] ST_UNAUTHORIZED = "401 Unauthorized\r\n".getBytes();
    public static final byte[] ST_PAYMENT_REQUIRED = "402 Payment Required\r\n".getBytes();
    public static final byte[] ST_FORBIDDEN = "403 Forbidden\r\n".getBytes();
    public static final byte[] ST_NOT_FOUND = "404 Not Found\r\n".getBytes();
    public static final byte[] ST_METHOD_NOT_ALLOWED = "405 Method Not Allowed\r\n".getBytes();
    public static final byte[] ST_NOT_ACCEPTABLE = "406 Not Acceptable\r\n".getBytes();
    public static final byte[] ST_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required\r\n".getBytes();
    public static final byte[] ST_REQUEST_TIMEOUT = "408 Request Time-out\r\n".getBytes();
    public static final byte[] ST_CONFLICT = "409 Conflict\r\n".getBytes();
    public static final byte[] ST_GONE = "410 Gone\r\n".getBytes();
    public static final byte[] ST_LENGTH_REQUIRED = "411 Length Required\r\n".getBytes();
    public static final byte[] ST_PRECONDITION_FAILED = "412 Precondition Failed\r\n".getBytes();
    public static final byte[] ST_REQUEST_ENTITY_TOO_LARGE = "413 Request Entity Too Large\r\n".getBytes();
    public static final byte[] ST_REQUEST_URI_TOO_LONG = "414 Request-URI Too Large\r\n".getBytes();
    public static final byte[] ST_UNSUPPORTED_MEDIA_TYPE = "415 Unsupported Media Type\r\n".getBytes();
    public static final byte[] ST_REQUEST_RANGE_NOT_SATISFIABLE = "416 Requested range not satisfiable\r\n".getBytes();
    public static final byte[] ST_EXPECTATION_FAILED = "417 Expectation Failed\r\n".getBytes();

    // Server error codes

    public static final byte[] ST_INTERNAL_SERVER_ERROR = "500 Internal Server Error\r\n".getBytes();
    public static final byte[] ST_NOT_IMPLEMENTED = "501 Not Implemented\r\n".getBytes();
    public static final byte[] ST_BAD_GATEWAY = "502 Bad Gateway\r\n".getBytes();
    public static final byte[] ST_SERVICE_UNAVAILABLE = "503 Service Unavailable\r\n".getBytes();
    public static final byte[] ST_GATEWAY_TIMEOUT = "504 Gateway Time-out\r\n".getBytes();
    public static final byte[] ST_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version not supported\r\n".getBytes();

    // Http response components
    public static final byte[] PROTOCOL = "HTTP/1.1 ".getBytes();
    public static final byte[] F_SERVER = ("Server: kilim " + Constants.KILIM_VERSION + "\r\n").getBytes();
    public static final byte[] F_DATE = "Date: ".getBytes();
    public static final byte[] CRLF = "\r\n".getBytes();
    public static final byte[] FIELD_SEP = ": ".getBytes();

    public static ConcurrentHashMap<String, byte[]> byteCache = new ConcurrentHashMap<String, byte[]>();

    /**
     * The status line for the response. Can use any of the predefined strings
     * in HttpResponse.ST_*.
     */
    public byte[] status;
    public ArrayList<String> keys = new ArrayList<String>();
    public ArrayList<String> values = new ArrayList<String>();
    public static final SimpleDateFormat gmtdf;

    public int statusRange;

    public int reasonRange;
    static {
        gmtdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
        gmtdf.setTimeZone(TimeZone.getTimeZone("GMT:00"));
    }


    public HttpResponse() {
        this(ST_OK);
    }


    public HttpResponse(byte[] statusb) {
        this.status = statusb;
    }


    public void reuse() {
        this.status = ST_OK;
        this.keys.clear();
        this.values.clear();
        if (this.bodyStream != null) {
            this.bodyStream.reset();
        }
        if (this.buffer != null) {
            this.buffer.clear();
        }
    }


    public void setStatus(String statusMsg) {
        if (!statusMsg.endsWith("\r\n")) {
            statusMsg = statusMsg + "\r\n";
        }
        this.status = statusMsg.getBytes();
    }


    public HttpResponse(String statusMsg) {
        this();
        this.setStatus(statusMsg);
    }


    public HttpMsg addField(String key, String value) {
        this.keys.add(key);
        this.values.add(value);
        return this;
    }


    public void writeHeader(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.write(PROTOCOL);
        dos.write(this.status);

        dos.write(F_DATE);
        byte[] date = gmtdf.format(new Date()).getBytes();
        dos.write(date);
        dos.write(CRLF);

        dos.write(F_SERVER);

        if (this.bodyStream != null) {
            this.setContentLength(this.bodyStream.size());
        }

        // Fields.
        int nfields = this.keys.size();
        for (int i = 0; i < nfields; i++) {
            String key = this.keys.get(i);
            byte[] keyb = byteCache.get(key);
            if (keyb == null) {
                keyb = key.getBytes();
                byteCache.put(key, keyb);
            }
            dos.write(keyb);
            dos.write(FIELD_SEP);
            dos.write(this.values.get(i).getBytes());
            dos.write(CRLF);
        }
        dos.write(CRLF);
    }


    public HttpMsg addField(String key, int valRange) {
        this.keys.add(key);
        this.values.add(this.extractRange(valRange));
        return this;
    }


    public void writeTo(EndPoint endpoint) throws IOException, Pausable {
        ExposedBaos headerStream = new ExposedBaos();
        this.writeHeader(headerStream);
        ByteBuffer bb = headerStream.toByteBuffer();
        endpoint.write(bb);
        if (this.bodyStream != null && this.bodyStream.size() > 0) {
            bb = this.bodyStream.toByteBuffer();
            endpoint.write(bb);
        }
    }


    /*
     * Internal methods
     */
    public void readFrom(EndPoint endpoint) throws Pausable, IOException {
        this.iread = 0;
        this.readHeader(endpoint);
        this.readBody(endpoint);
    }


    public void readHeader(EndPoint endpoint) throws Pausable, IOException {
        this.buffer = ByteBuffer.allocate(1024);
        int headerLength = 0;
        int n;
        do {
            n = this.readLine(endpoint); // includes 2 bytes for CRLF
            headerLength += n;
        } while (n > 2); // until blank line (CRLF)
        // dumpBuffer(buffer);
        HttpResponseParser.initHeader(this, headerLength);
        this.contentOffset = headerLength; // doesn't mean there's necessarily
        // any content.
        String cl = this.getHeader("Content-Length");
        if (cl.length() > 0) {
            try {
                this.contentLength = Integer.parseInt(cl);
            }
            catch (NumberFormatException nfe) {
                throw new IOException("Malformed Content-Length hdr");
            }
        }
        else if (this.getHeader("Transfer-Encoding").indexOf("chunked") >= 0
                || this.getHeader("TE").indexOf("chunked") >= 0) {
            this.contentLength = -1;
        }
        else {
            this.contentLength = 0;
        }
    }


    /**
     * Get the value for a given key
     * 
     * @param key
     * @return null if the key is not present in the header.
     */
    public String getHeader(String key) {
        for (int i = 0; i < this.keys.size(); i++) {
            if (key.equalsIgnoreCase(this.keys.get(i))) {
                return this.values.get(i);
            }
        }
        return ""; // no point returning null
    }


    public void setContentLength(long length) {
        this.addField("Content-Length", Long.toString(length));
    }


    public void setContentType(String contentType) {
        this.addField("Content-Type", contentType);
    }


    public String status() {
        return this.extractRange(this.statusRange);
    }


    public String reason() {
        return this.extractRange(this.reasonRange);
    }

}