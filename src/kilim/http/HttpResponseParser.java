
// line 1 "HttpResponseParser.rl"
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

  
// line 81 "HttpResponseParser.rl"


  
// line 34 "HttpResponseParser.java"
private static byte[] init__http_parser_actions_0()
{
	return new byte [] {
	    0,    1,    0,    1,    1,    1,    2,    1,    3,    1,    5,    1,
	    6,    2,    0,    2,    2,    0,    3,    2,    4,    0,    3,    4,
	    0,    3
	};
}

private static final byte _http_parser_actions[] = init__http_parser_actions_0();


private static byte[] init__http_parser_key_offsets_0()
{
	return new byte [] {
	    0,    0,    1,    2,    3,    4,    5,    7,   10,   12,   15,   17,
	   19,   21,   24,   26,   43,   44,   60,   63,   65,   66,   68
	};
}

private static final byte _http_parser_key_offsets[] = init__http_parser_key_offsets_0();


private static char[] init__http_parser_trans_keys_0()
{
	return new char [] {
	   72,   84,   84,   80,   47,   48,   57,   46,   48,   57,   48,   57,
	   32,   48,   57,   48,   57,   48,   57,   48,   57,   10,   13,   32,
	   10,   13,   10,   13,   33,  124,  126,   35,   39,   42,   43,   45,
	   46,   48,   57,   65,   90,   94,  122,   10,   33,   58,  124,  126,
	   35,   39,   42,   43,   45,   46,   48,   57,   65,   90,   94,  122,
	   10,   13,   32,   10,   13,   10,   10,   13,    0
	};
}

private static final char _http_parser_trans_keys[] = init__http_parser_trans_keys_0();


private static byte[] init__http_parser_single_lengths_0()
{
	return new byte [] {
	    0,    1,    1,    1,    1,    1,    0,    1,    0,    1,    0,    0,
	    0,    3,    2,    5,    1,    4,    3,    2,    1,    2,    0
	};
}

private static final byte _http_parser_single_lengths[] = init__http_parser_single_lengths_0();


private static byte[] init__http_parser_range_lengths_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    1,    1,    1,    1,    1,    1,
	    1,    0,    0,    6,    0,    6,    0,    0,    0,    0,    0
	};
}

private static final byte _http_parser_range_lengths[] = init__http_parser_range_lengths_0();


private static byte[] init__http_parser_index_offsets_0()
{
	return new byte [] {
	    0,    0,    2,    4,    6,    8,   10,   12,   15,   17,   20,   22,
	   24,   26,   30,   33,   45,   47,   58,   62,   65,   67,   70
	};
}

private static final byte _http_parser_index_offsets[] = init__http_parser_index_offsets_0();


private static byte[] init__http_parser_trans_targs_0()
{
	return new byte [] {
	    2,    0,    3,    0,    4,    0,    5,    0,    6,    0,    7,    0,
	    8,    7,    0,    9,    0,   10,    9,    0,   11,    0,   12,    0,
	   13,    0,   15,   20,   21,   14,   15,   20,   14,   22,   16,   17,
	   17,   17,   17,   17,   17,   17,   17,   17,    0,   22,    0,   17,
	   18,   17,   17,   17,   17,   17,   17,   17,   17,    0,   15,   20,
	   18,   19,   15,   20,   19,   15,    0,   15,   20,   14,    0,    0
	};
}

private static final byte _http_parser_trans_targs[] = init__http_parser_trans_targs_0();


private static byte[] init__http_parser_trans_actions_0()
{
	return new byte [] {
	    1,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    9,    0,    0,    1,    0,    0,    0,
	    0,    0,   22,   22,   19,   19,    7,    7,    0,    0,    0,    1,
	    1,    1,    1,    1,    1,    1,    1,    1,    0,    0,    0,    0,
	    3,    0,    0,    0,    0,    0,    0,    0,    0,    0,   13,   13,
	    1,    1,    5,    5,    0,    0,    0,   16,   16,    1,   11,    0
	};
}

private static final byte _http_parser_trans_actions[] = init__http_parser_trans_actions_0();


static final int http_parser_start = 1;
static final int http_parser_first_final = 22;
static final int http_parser_error = 0;

static final int http_parser_en_main = 1;


// line 84 "HttpResponseParser.rl"

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

    
// line 163 "HttpResponseParser.java"
	{
	cs = http_parser_start;
	}

// line 103 "HttpResponseParser.rl"
    
// line 170 "HttpResponseParser.java"
	{
	int _klen;
	int _trans = 0;
	int _acts;
	int _nacts;
	int _keys;
	int _goto_targ = 0;

	_goto: while (true) {
	switch ( _goto_targ ) {
	case 0:
	if ( p == pe ) {
		_goto_targ = 4;
		continue _goto;
	}
	if ( cs == 0 ) {
		_goto_targ = 5;
		continue _goto;
	}
case 1:
	_match: do {
	_keys = _http_parser_key_offsets[cs];
	_trans = _http_parser_index_offsets[cs];
	_klen = _http_parser_single_lengths[cs];
	if ( _klen > 0 ) {
		int _lower = _keys;
		int _mid;
		int _upper = _keys + _klen - 1;
		while (true) {
			if ( _upper < _lower )
				break;

			_mid = _lower + ((_upper-_lower) >> 1);
			if ( data[p] < _http_parser_trans_keys[_mid] )
				_upper = _mid - 1;
			else if ( data[p] > _http_parser_trans_keys[_mid] )
				_lower = _mid + 1;
			else {
				_trans += (_mid - _keys);
				break _match;
			}
		}
		_keys += _klen;
		_trans += _klen;
	}

	_klen = _http_parser_range_lengths[cs];
	if ( _klen > 0 ) {
		int _lower = _keys;
		int _mid;
		int _upper = _keys + (_klen<<1) - 2;
		while (true) {
			if ( _upper < _lower )
				break;

			_mid = _lower + (((_upper-_lower) >> 1) & ~1);
			if ( data[p] < _http_parser_trans_keys[_mid] )
				_upper = _mid - 2;
			else if ( data[p] > _http_parser_trans_keys[_mid+1] )
				_lower = _mid + 2;
			else {
				_trans += ((_mid - _keys)>>1);
				break _match;
			}
		}
		_trans += _klen;
	}
	} while (false);

	cs = _http_parser_trans_targs[_trans];

	if ( _http_parser_trans_actions[_trans] != 0 ) {
		_acts = _http_parser_trans_actions[_trans];
		_nacts = (int) _http_parser_actions[_acts++];
		while ( _nacts-- > 0 )
	{
			switch ( _http_parser_actions[_acts++] )
			{
	case 0:
// line 31 "HttpResponseParser.rl"
	{mark = p; }
	break;
	case 1:
// line 33 "HttpResponseParser.rl"
	{ 
      field_name = HttpCommonParser.kw_lookup(data, mark, p);
      if (field_name == null) {// not a known keyword
        field_name = resp.extractRange(mark, p);
      }
    }
	break;
	case 2:
// line 40 "HttpResponseParser.rl"
	{
      int value = HttpCommonParser.encodeRange(mark, p);
      resp.addField(field_name, value);
    }
	break;
	case 3:
// line 45 "HttpResponseParser.rl"
	{
      resp.reasonRange = HttpCommonParser.encodeRange(mark, p);
    }
	break;
	case 4:
// line 49 "HttpResponseParser.rl"
	{
      resp.statusRange = HttpCommonParser.encodeRange(mark, p);
    }
	break;
	case 5:
// line 53 "HttpResponseParser.rl"
	{
      resp.versionRange = HttpCommonParser.encodeRange(mark, p);
    }
	break;
	case 6:
// line 79 "HttpResponseParser.rl"
	{err("Malformed Header. Error at " + p + "\n" + new String(data, 0, pe, UTF8));}
	break;
// line 291 "HttpResponseParser.java"
			}
		}
	}

case 2:
	if ( cs == 0 ) {
		_goto_targ = 5;
		continue _goto;
	}
	if ( ++p != pe ) {
		_goto_targ = 1;
		continue _goto;
	}
case 4:
case 5:
	}
	break; }
	}

// line 104 "HttpResponseParser.rl"
    
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

