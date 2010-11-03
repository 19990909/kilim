/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

/**
 * --- DO NOT EDIT -----
 * HttpRequestParser.java generated from RAGEL (http://www.complang.org/ragel/) from the
 * specification file HttpResponseParser.rl. All changes must be made in the .rl file.
 **/

import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;

public class HttpResponseParser {
  public static final Charset UTF8 = Charset.forName("UTF-8");

  %%{
    # A variation of the Ragel grammar from Zed Shaw's mongrel parser. Thanks, Zed.

    machine http_parser;

    action mark {mark = fpc; }

    action extract_field_name { 
      field_name = HttpCommonParser.kw_lookup(data, mark, fpc);
      if (field_name == null) {// not a known keyword
        field_name = resp.extractRange(mark, fpc);
      }
    }

    action extract_value {
      int value = HttpCommonParser.encodeRange(mark, fpc);
      resp.addField(field_name, value);
    }

    action reason_phrase {
      resp.reasonRange = HttpCommonParser.encodeRange(mark, fpc);
    }

    action status_code {
      resp.statusRange = HttpCommonParser.encodeRange(mark, fpc);
    }
    
    action http_version {
      resp.versionRange = HttpCommonParser.encodeRange(mark, fpc);
    }

# line endings
  CRLF = ("\r\n" | "\n");

# character types
  CTL = (cntrl | 127);
  tspecials = ("(" | ")" | "<" | ">" | "@" | "," | ";" | ":" | "\\" | "\"" | "/" | "[" | "]" | "?" | "=" | "{" | "}" | " " | "\t");

# elements
  token = (ascii -- (CTL | tspecials));

  Reason_Phrase = (any -- CRLF)* >mark %reason_phrase;
  Status_Code = digit{3} >mark %status_code;
  http_number = (digit+ "." digit+) ;
  HTTP_Version = ("HTTP/" http_number) >mark %http_version ;
  Status_Line = HTTP_Version " " Status_Code " "? Reason_Phrase :> CRLF;

  field_name = token+ >mark %extract_field_name;
  field_value = any* >mark %extract_value;
  message_header = field_name ":" " "* field_value :> CRLF;

  Response = Status_Line (message_header)* CRLF;

  main := Response %err{err("Malformed Header. Error at " + p + "\n" + new String(data, 0, pe, UTF8));};

  }%%

  %% write data;

  public static void err(String msg) throws IOException{
    throw new IOException(msg);
  }

  public static void initHeader(HttpResponse resp, int headerLength) throws IOException {
    ByteBuffer bb = resp.buffer;
    /* required variables */
    byte[] data = bb.array();
    int p = 0;
    int pe = headerLength;
//  int eof = pe;
    int cs = 0;

    // variables used by actions in http_req_parser machine above.
    int mark = 0;
    String field_name = "";

    %% write init;
    %% write exec;
    
    if (cs == http_parser_error) {
      throw new IOException("Malformed HTTP Header. p = " + p +", cs = " + cs);
    }
  }

  public static void main(String args[]) throws Exception {
    /// Testing
    String s = 
      "HTTP/1.1 200 OK\r\n"+ 
      "Server: nginx\r\n"+   
      "Date: Wed, 03 Nov 2010 07:38:42 GMT\r\n"+
      "Content-Type: text/html; charset=GBK\r\n"+
      "Transfer-Encoding: chunked\r\n"+
      "Vary: Accept-Encoding\r\n"+
      "Expires: Wed, 03 Nov 2010 07:40:42 GMT\r\n"+
      "Cache-Control: max-age=120\r\n"+
      "Content-Encoding: gzip\r\n"+
      "X-Via: 1.1 ls103:8103 (Cdn Cache Server V2.0), 1.1 zjls205:8103 (Cdn Cache Server V2.0)\r\n"+
      "Connection: keep-alive\r\n"+
      "Age: 1\r\n\r\n";
    System.out.println("Input Response: (" + s.length() + " bytes)");System.out.println(s);
    byte[] data = s.getBytes();
    int len = data.length;
    
    System.out.println("=============================================================");
    HttpResponse resp = new HttpResponse();
    resp.buffer = ByteBuffer.allocate(2048);
    resp.buffer.put(data);
    initHeader(resp, len);
    System.out.println(resp);
  }
}

