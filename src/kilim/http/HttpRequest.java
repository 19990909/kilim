/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;

import kilim.Pausable;
import kilim.nio.EndPoint;


/**
 * This object encapsulates a bytebuffer (via HttpMsg). HttpRequestParser
 * creates an instance of this object, but only converts a few of the important
 * fields into Strings; the rest are maintained as ranges (offset + length) in
 * the bytebuffer. Use {@link #getHeader(String)} to get the appropriate field.
 */
public class HttpRequest extends HttpMsg {
    // All the header related members of this class are initialized by the
    // HttpRequestParser class.

    /**
     * The original header. All string variables that pertain to the message's
     * header are either subsequences of this header, or interned (all known
     * keywords).
     */
    public String method;

    /**
     * The UTF8 decoded path from the HTTP header.
     */
    public String uriPath;

    public int nFields;
    /**
     * Keys present in the HTTP header
     */
    public String keys[];

    public String values[];

    public int uriFragmentRange;
    public int queryStringRange;
    public int[] valueRanges;


    public HttpRequest() {
        this.keys = new String[5];
        this.valueRanges = new int[5];
        this.values = new String[5];
    }


    /**
     * Get the value for a given key
     * 
     * @param key
     * @return null if the key is not present in the header.
     */
    public String getHeader(String key) {
        for (int i = 0; i < this.nFields; i++) {
            if (key.equalsIgnoreCase(this.keys[i])) {
                if (this.values[i] != null) {
                    return this.values[i];
                }
                else {
                    return this.extractRange(this.valueRanges[i]);
                }
            }
        }
        return ""; // no point returning null
    }


    /**
     * @return the query part of the URI.
     */
    public String getQuery() {
        return this.extractRange(this.queryStringRange);
    }


    public boolean keepAlive() {
        return this.isOldHttp() ? "Keep-Alive".equals(this.getHeader("Connection;")) : !"close".equals(this
            .getHeader("Connection"));
    }


    public KeyValues getQueryComponents() {
        String q = this.getQuery();
        int len = q.length();
        if (q == null || len == 0) {
            return new KeyValues(0);
        }

        int numPairs = 0;
        for (int i = 0; i < len; i++) {
            if (q.charAt(i) == '=') {
                numPairs++;
            }
        }
        KeyValues components = new KeyValues(numPairs);

        int beg = 0;
        String key = null;
        boolean url_encoded = false;
        for (int i = 0; i <= len; i++) {
            char c = i == len ? '&' // pretending there's an artificial marker
                    // at the end of the string, to capture
                    // the last component
                    : q.charAt(i);

            if (c == '+' || c == '%') {
                url_encoded = true;
            }
            if (c == '=' || c == '&') {
                String comp = q.substring(beg, i);
                if (url_encoded) {
                    try {
                        comp = URLDecoder.decode(comp, "UTF-8");
                    }
                    catch (UnsupportedEncodingException ignore) {
                    }
                }
                if (key == null) {
                    key = comp;
                }
                else {
                    components.put(key, comp);
                    key = null;
                }
                beg = i + 1;
                url_encoded = false; // for next time
            }
        }
        return components;
    }


    public String uriFragment() {
        return this.extractRange(this.uriFragmentRange);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("method: ").append(this.method).append('\n').append("version: ").append(this.version()).append('\n')
            .append("path = ").append(this.uriPath).append('\n').append("uri_fragment = ").append(this.uriFragment())
            .append('\n').append("query = ").append(this.getQueryComponents()).append('\n');
        for (int i = 0; i < this.nFields; i++) {
            sb.append(this.keys[i]).append(": ").append(this.extractRange(this.valueRanges[i])).append('\n');
        }

        return sb.toString();
    }


    /**
     * @return true if version is 1.0 or earlier
     */
    public boolean isOldHttp() {
        final byte b1 = (byte) '1';
        int offset = this.versionRange >> 16;
        return this.buffer.get(offset) < b1 || this.buffer.get(offset + 2) < b1;
    }


    /**
     * Clear the request object so that it can be reused for the next message.
     */
    public void reuse() {
        this.method = null;
        this.uriPath = null;
        this.versionRange = 0;
        this.uriFragmentRange = this.queryStringRange = 0;
        this.contentOffset = 0;
        this.contentLength = 0;

        if (this.buffer != null) {
            this.buffer.clear();
        }
        for (int i = 0; i < this.nFields; i++) {
            this.keys[i] = null;
        }
        if (this.bodyStream != null) {
            this.bodyStream.reset();
        }
        this.nFields = 0;
    }

    static final byte[] SPACE = " ".getBytes();
    public static final byte[] PROTOCOL = "HTTP/1.1".getBytes();
    public static final byte[] CRLF = "\r\n".getBytes();
    public static final byte[] FIELD_SEP = ": ".getBytes();


    public void setContentLength(long length) {
        this.addField("Content-Length", Long.toString(length));
    }


    @Override
    public void writeHeader(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.write(this.method.getBytes());
        dos.write(SPACE);
        dos.write(this.uriPath.getBytes());
        dos.write(SPACE);
        dos.write(PROTOCOL);
        dos.write(CRLF);
        if (this.bodyStream != null) {
            this.setContentLength(this.bodyStream.size());
        }

        for (int i = 0; i < this.nFields; i++) {
            String key = this.keys[i];
            String value = this.values[i];
            dos.write(key.getBytes());
            dos.write(FIELD_SEP);
            dos.write(value.getBytes());
            dos.write(CRLF);
        }
        dos.write(CRLF);
    }


    @Override
    public void readHeader(EndPoint endpoint) throws Pausable, IOException {
        this.buffer = ByteBuffer.allocate(64 * 1024);
        int headerLength = 0;
        int n;
        do {
            n = this.readLine(endpoint); // includes 2 bytes for CRLF
            headerLength += n;
        } while (n > 2); // until blank line (CRLF)
        // dumpBuffer(buffer);
        HttpRequestParser.initHeader(this, headerLength);
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


    public void addField(String key, int valRange) {
        if (this.keys.length == this.nFields) {
            this.keys = (String[]) Utils.growArray(this.keys, 5);
            this.valueRanges = Utils.growArray(this.valueRanges, 5);
        }
        this.keys[this.nFields] = key;
        this.valueRanges[this.nFields] = valRange;
        this.nFields++;
    }


    public void addField(String key, String value) {
        if (this.keys.length == this.nFields) {
            this.keys = (String[]) Utils.growArray(this.keys, 5);
            this.valueRanges = Utils.growArray(this.valueRanges, 5);
            this.values = (String[]) Utils.growArray(this.values, 5);
        }
        this.keys[this.nFields] = key;
        this.values[this.nFields] = value;
        this.nFields++;
    }

}