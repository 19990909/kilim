/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import kilim.Pausable;
import kilim.nio.EndPoint;
import kilim.nio.ExposedBaos;


public class HttpMsg {
    public static byte CR = (byte) '\r';
    public static byte LF = (byte) '\n';
    static final byte b0 = (byte) '0';
    public int contentOffset;
    public int contentLength;
    /**
     * The read cursor, used in the read* methods.
     */
    public int iread;
    static final byte b9 = (byte) '9';
    static final byte ba = (byte) 'a';
    static final byte bf = (byte) 'f';
    static final byte bA = (byte) 'A';


    public void readBody(EndPoint endpoint) throws Pausable, IOException {
        this.iread = this.contentOffset;
        if (this.contentLength > 0) {
            this.fill(endpoint, this.contentOffset, this.contentLength);
            this.iread = this.contentOffset + this.contentLength;
        }
        else if (this.contentLength == -1) {
            // CHUNKED
            this.readAllChunks(endpoint);
        }
        this.readTrailers(endpoint);
    }


    public void readTrailers(EndPoint endpoint) {
    }


    public void readAllChunks(EndPoint endpoint) throws IOException, Pausable {
        IntList chunkRanges = new IntList(); // alternate numbers in this list
        // refer to the start and end
        // offsets of chunks.
        try {
            do {
                int n = this.readLine(endpoint); // read chunk size text into
                // buffer
                int beg = this.iread;
                int size = parseChunkSize(this.buffer, this.iread - n, this.iread); // Parse
                // size
                // in
                // hex,
                // ignore
                // extension

                if (size == 0) {
                    break;
                }
                // If the chunk has not already been read in, do so
                this.fill(endpoint, this.iread, size + 2 /* chunksize + CRLF */);
                // record chunk start and end
                chunkRanges.add(beg);
                chunkRanges.add(beg + size); // without the CRLF
                this.iread += size + 2; // for the next round.
            } while (true);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        // / consolidate all chunkRanges
        if (chunkRanges.numElements == 0) {
            this.contentLength = 0;
            return;
        }
        this.contentOffset = chunkRanges.get(0); // first chunk's beginning
        int endOfLastChunk = chunkRanges.get(1); // first chunk's end

        byte[] bufa = this.buffer.array();
        for (int i = 2; i < chunkRanges.numElements; i += 2) {
            int beg = chunkRanges.get(i);
            int chunkSize = chunkRanges.get(i + 1) - beg;
            System.arraycopy(bufa, beg, bufa, endOfLastChunk, chunkSize);
            endOfLastChunk += chunkSize;
        }
        // TODO move all trailer stuff up
        this.contentLength = endOfLastChunk - this.contentOffset;
        // At this point, the contentOffset and contentLen give the entire
        // content
    }

    static final byte bF = (byte) 'F';
    static final byte SEMI = (byte) ';';


    public static int parseChunkSize(ByteBuffer buffer, int start, int end) throws IOException {
        byte[] bufa = buffer.array();
        int size = 0;
        for (int i = start; i < end; i++) {
            byte b = bufa[i];
            if (b >= b0 && b <= b9) {
                size = size * 16 + b - b0;
            }
            else if (b >= ba && b <= bf) {
                size = size * 16 + b - ba + 10;
            }
            else if (b >= bA && b <= bF) {
                size = size * 16 + b - bA + 10;
            }
            else if (b == CR || b == SEMI) {
                // SEMI-colon starts a chunk extension. We ignore extensions
                // currently.
                break;
            }
            else {
                throw new IOException("Error parsing chunk size; unexpected char " + b + " at offset " + i);
            }
        }
        return size;
    }

    ByteBuffer buffer;
    public ExposedBaos bodyStream;
    public int versionRange;


    public OutputStream getOutputStream() {
        if (this.bodyStream == null) {
            this.bodyStream = new ExposedBaos(2048);
        }
        return this.bodyStream;
    }


    public String extractRange(int range) {
        int beg = range >> 16;
        int end = range & 0xFFFF;
        return this.extractRange(beg, end);
    }


    public String extractRange(int beg, int end) {
        try {
            return new String(this.buffer.array(), beg, (end - beg), "UTF-8");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String version() {
        return this.extractRange(this.versionRange);
    }


    public void fill(EndPoint endpoint, int offset, int size) throws IOException, Pausable {
        int total = offset + size;
        int currentPos = this.buffer.position();
        if (total > this.buffer.position()) {
            this.buffer = endpoint.fill(this.buffer, (total - currentPos));
        }
    }


    public int readLine(EndPoint endpoint) throws IOException, Pausable {
        int ireadSave = this.iread;
        int i = ireadSave;
        while (true) {
            int end = this.buffer.position();
            byte[] bufa = this.buffer.array();
            for (; i < end; i++) {
                if (bufa[i] == CR) {
                    ++i;
                    if (i >= end) {
                        this.buffer = endpoint.fill(this.buffer, 1);
                        bufa = this.buffer.array(); // fill could have changed
                        // the buffer.
                        end = this.buffer.position();
                    }
                    if (bufa[i] != LF) {
                        throw new IOException("Expected LF at " + i + " but was " + bufa[i]);
                        // ++i;
                        // continue;
                    }
                    ++i;
                    int lineLength = i - ireadSave;
                    this.iread = i;
                    return lineLength;
                }
            }
            this.buffer = endpoint.fill(this.buffer, 1); // no CRLF found. fill
            // a bit more and start
            // over.
        }
    }


    public void dumpBuffer(ByteBuffer buffer) {
        byte[] ba = buffer.array();
        int len = buffer.position();
        for (int i = 0; i < len; i++) {
            System.out.print((char) ba[i]);
        }
    }

}
