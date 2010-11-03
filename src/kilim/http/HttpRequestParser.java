
// line 1 "HttpRequestParser.rl"
/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.http;

/**
 * --- DO NOT EDIT -----
 * HttpRequestParser.java generated from RAGEL (http://www.complang.org/ragel/) from the
 * specification file HttpRequestParser.rl. All changes must be made in the .rl file.
 **/

import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;

public class HttpRequestParser {
  public static final Charset UTF8 = Charset.forName("UTF-8");

  
// line 135 "HttpRequestParser.rl"


  
// line 34 "HttpRequestParser.java"
private static byte[] init__http_parser_actions_0()
{
	return new byte [] {
	    0,    1,    0,    1,    1,    1,    2,    1,    3,    1,    4,    1,
	    5,    1,    6,    1,    7,    1,    8,    1,    9,    1,   10,    1,
	   11,    1,   12,    1,   13,    2,    0,    3,    2,    0,    6,    2,
	    1,    5
	};
}

private static final byte _http_parser_actions[] = init__http_parser_actions_0();


private static short[] init__http_parser_key_offsets_0()
{
	return new short [] {
	    0,    0,   10,   12,   14,   16,   18,   20,   21,   31,   41,   50,
	   52,   53,   54,   55,   56,   58,   61,   63,   66,   67,   69,   70,
	   72,   73,   75,   84,   93,   99,  105,  111,  117,  121,  125,  135,
	  141,  147,  156,  165,  171,  177,  179,  181,  183,  185,  187,  189,
	  191,  193,  195,  197,  199,  203,  205,  207,  209
	};
}

private static final short _http_parser_key_offsets[] = init__http_parser_key_offsets_0();


private static char[] init__http_parser_trans_keys_0()
{
	return new char [] {
	   68,   71,   72,   79,   80,  100,  103,  104,  111,  112,   69,  101,
	   76,  108,   69,  101,   84,  116,   69,  101,   32,   32,   43,   47,
	   58,   45,   57,   65,   90,   97,  122,   43,   58,   45,   46,   48,
	   57,   65,   90,   97,  122,   32,   34,   35,   37,   60,   62,  127,
	    0,   31,   32,   72,   84,   84,   80,   47,   48,   57,   46,   48,
	   57,   48,   57,   13,   48,   57,   10,   13,   58,   58,   13,   32,
	   13,   10,   58,   32,   37,   60,   62,  127,    0,   31,   34,   35,
	   32,   37,   60,   62,  127,    0,   31,   34,   35,   48,   57,   65,
	   70,   97,  102,   48,   57,   65,   70,   97,  102,   48,   57,   65,
	   70,   97,  102,   48,   57,   65,   70,   97,  102,   32,   35,   59,
	   63,   32,   35,   59,   63,   32,   34,   35,   37,   60,   62,   63,
	  127,    0,   31,   48,   57,   65,   70,   97,  102,   48,   57,   65,
	   70,   97,  102,   32,   34,   35,   37,   60,   62,  127,    0,   31,
	   32,   34,   35,   37,   60,   62,  127,    0,   31,   48,   57,   65,
	   70,   97,  102,   48,   57,   65,   70,   97,  102,   69,  101,   84,
	  116,   69,  101,   65,   97,   68,  100,   80,  112,   84,  116,   73,
	  105,   79,  111,   78,  110,   83,  115,   79,   85,  111,  117,   83,
	  115,   84,  116,   84,  116,   58,    0
	};
}

private static final char _http_parser_trans_keys[] = init__http_parser_trans_keys_0();


private static byte[] init__http_parser_single_lengths_0()
{
	return new byte [] {
	    0,   10,    2,    2,    2,    2,    2,    1,    4,    2,    7,    2,
	    1,    1,    1,    1,    0,    1,    0,    1,    1,    2,    1,    2,
	    1,    2,    5,    5,    0,    0,    0,    0,    4,    4,    8,    0,
	    0,    7,    7,    0,    0,    2,    2,    2,    2,    2,    2,    2,
	    2,    2,    2,    2,    4,    2,    2,    2,    1
	};
}

private static final byte _http_parser_single_lengths[] = init__http_parser_single_lengths_0();


private static byte[] init__http_parser_range_lengths_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    0,    0,    3,    4,    1,    0,
	    0,    0,    0,    0,    1,    1,    1,    1,    0,    0,    0,    0,
	    0,    0,    2,    2,    3,    3,    3,    3,    0,    0,    1,    3,
	    3,    1,    1,    3,    3,    0,    0,    0,    0,    0,    0,    0,
	    0,    0,    0,    0,    0,    0,    0,    0,    0
	};
}

private static final byte _http_parser_range_lengths[] = init__http_parser_range_lengths_0();


private static short[] init__http_parser_index_offsets_0()
{
	return new short [] {
	    0,    0,   11,   14,   17,   20,   23,   26,   28,   36,   43,   52,
	   55,   57,   59,   61,   63,   65,   68,   70,   73,   75,   78,   80,
	   83,   85,   88,   96,  104,  108,  112,  116,  120,  125,  130,  140,
	  144,  148,  157,  166,  170,  174,  177,  180,  183,  186,  189,  192,
	  195,  198,  201,  204,  207,  212,  215,  218,  221
	};
}

private static final short _http_parser_index_offsets[] = init__http_parser_index_offsets_0();


private static byte[] init__http_parser_indicies_0()
{
	return new byte [] {
	    0,    2,    3,    4,    5,    0,    2,    3,    4,    5,    1,    6,
	    6,    1,    7,    7,    1,    8,    8,    1,    9,    9,    1,   10,
	   10,    1,   11,    1,   11,   12,   13,   14,   12,   12,   12,    1,
	   15,   16,   15,   15,   15,   15,    1,   17,    1,   18,   19,    1,
	    1,    1,    1,   16,   17,   20,    1,   21,    1,   22,    1,   23,
	    1,   24,    1,   25,    1,   26,   27,    1,   28,    1,   29,   28,
	    1,   30,    1,   32,    1,   31,   34,   33,   36,   37,   35,   39,
	   38,   40,   34,   33,   41,   43,    1,    1,    1,    1,    1,   42,
	   44,   46,    1,    1,    1,    1,    1,   45,   47,   47,   47,    1,
	   45,   45,   45,    1,   48,   48,   48,    1,   16,   16,   16,    1,
	   17,   18,   50,   51,   49,   52,   53,   54,   55,   49,   17,    1,
	   18,   56,    1,    1,   51,    1,    1,   50,   57,   57,   57,    1,
	   50,   50,   50,    1,   58,    1,   60,   61,    1,    1,    1,    1,
	   59,   62,    1,   64,   65,    1,    1,    1,    1,   63,   66,   66,
	   66,    1,   63,   63,   63,    1,   67,   67,    1,   68,   68,    1,
	   69,   69,    1,   70,   70,    1,   71,   71,    1,   72,   72,    1,
	   73,   73,    1,   74,   74,    1,   75,   75,    1,   76,   76,    1,
	   77,   77,    1,   78,   79,   78,   79,    1,   80,   80,    1,   81,
	   81,    1,   82,   82,    1,   34,   33,    0
	};
}

private static final byte _http_parser_indicies[] = init__http_parser_indicies_0();


private static byte[] init__http_parser_trans_targs_0()
{
	return new byte [] {
	    2,    0,   41,   43,   46,   52,    3,    4,    5,    6,    7,    8,
	    9,   32,   10,    9,   10,   11,   26,   30,   12,   13,   14,   15,
	   16,   17,   18,   17,   19,   20,   21,   22,   25,   22,   23,   24,
	   20,   23,   24,   20,   56,   11,   27,   28,   11,   27,   28,   29,
	   31,   33,   34,   37,   11,   26,   34,   37,   35,   36,   11,   38,
	   26,   39,   11,   38,   26,   39,   40,   42,    7,   44,   45,    7,
	   47,   48,   49,   50,   51,    7,   53,   55,   54,    7,    7
	};
}

private static final byte _http_parser_trans_targs[] = init__http_parser_trans_targs_0();


private static byte[] init__http_parser_trans_actions_0()
{
	return new byte [] {
	    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,   21,    0,
	    1,    1,    1,    0,    0,    0,    0,    0,    0,    0,    0,    0,
	    0,    1,    0,    0,    0,   15,    0,    1,    1,    0,    5,    1,
	   29,    1,    0,    7,    0,   32,    1,    1,   13,    0,    0,    0,
	    0,    0,    0,    0,    9,    9,    9,    9,    0,    0,   35,    3,
	   35,    3,   11,    0,   11,    0,    0,    0,   17,    0,    0,   23,
	    0,    0,    0,    0,    0,   27,    0,    0,    0,   19,   25
	};
}

private static final byte _http_parser_trans_actions[] = init__http_parser_trans_actions_0();


static final int http_parser_start = 1;
static final int http_parser_first_final = 56;
static final int http_parser_error = 0;

static final int http_parser_en_main = 1;


// line 138 "HttpRequestParser.rl"


  public static void initHeader(HttpRequest req, int headerLength) throws IOException {
    ByteBuffer bb = req.buffer;
    /* required variables */
    byte[] data = bb.array();
    int p = 0;
    int pe = headerLength;
//  int eof = pe;
    int cs = 0;

    // variables used by actions in http_req_parser machine above.
    int query_start = 0;
    int mark = 0;
    String field_name = "";

    
// line 216 "HttpRequestParser.java"
	{
	cs = http_parser_start;
	}

// line 155 "HttpRequestParser.rl"
    
// line 223 "HttpRequestParser.java"
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

	_trans = _http_parser_indicies[_trans];
	cs = _http_parser_trans_targs[_trans];

	if ( _http_parser_trans_actions[_trans] != 0 ) {
		_acts = _http_parser_trans_actions[_trans];
		_nacts = (int) _http_parser_actions[_acts++];
		while ( _nacts-- > 0 )
	{
			switch ( _http_parser_actions[_acts++] )
			{
	case 0:
// line 31 "HttpRequestParser.rl"
	{mark = p; }
	break;
	case 1:
// line 33 "HttpRequestParser.rl"
	{query_start = p; }
	break;
	case 2:
// line 35 "HttpRequestParser.rl"
	{ 
      field_name = HttpCommonParser.kw_lookup(data, mark, p);
      if (field_name == null) {// not a known keyword
        field_name = req.extractRange(mark, p);
      }
    }
	break;
	case 3:
// line 42 "HttpRequestParser.rl"
	{
      int value = HttpCommonParser.encodeRange(mark, p);
      req.addField(field_name, value);
    }
	break;
	case 4:
// line 47 "HttpRequestParser.rl"
	{
      req.uriPath = req.extractRange(mark, p);
      String s = req.uriPath;
      int len = s.length();
      boolean need_decode;
      // Scan the string to see if the string requires any conversion.
      for (int i = 0; i < len; i++) {
         char c = s.charAt(i);
         if (c == '%' || c > 0x7F) {
           try {
              // TODO: Correct this. URLDecoder is broken for path (upto
              // JDK1.6): it converts'+' to ' ', which should
              // be done only for the query part of the url.
              req.uriPath = URLDecoder.decode(req.uriPath, "UTF-8");
              break;
           } catch (UnsupportedEncodingException ignore){}
         }
      }
    }
	break;
	case 5:
// line 72 "HttpRequestParser.rl"
	{
      req.queryStringRange = HttpCommonParser.encodeRange(query_start, p);
    }
	break;
	case 6:
// line 76 "HttpRequestParser.rl"
	{ 
      req.uriFragmentRange = HttpCommonParser.encodeRange(mark, p);
    }
	break;
	case 7:
// line 80 "HttpRequestParser.rl"
	{
      req.versionRange = HttpCommonParser.encodeRange(mark, p);
    }
	break;
	case 8:
// line 119 "HttpRequestParser.rl"
	{req.method = "GET";}
	break;
	case 9:
// line 120 "HttpRequestParser.rl"
	{req.method = "POST";}
	break;
	case 10:
// line 121 "HttpRequestParser.rl"
	{req.method = "DELETE";}
	break;
	case 11:
// line 122 "HttpRequestParser.rl"
	{req.method = "HEAD";}
	break;
	case 12:
// line 123 "HttpRequestParser.rl"
	{req.method = "PUT";}
	break;
	case 13:
// line 124 "HttpRequestParser.rl"
	{req.method = "OPTIONS";}
	break;
// line 391 "HttpRequestParser.java"
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

// line 156 "HttpRequestParser.rl"
    
    if (cs == http_parser_error) {
      throw new IOException("Malformed HTTP Header. p = " + p +", cs = " + cs);
    }
  }

  
  public static void main(String args[]) throws Exception {
    /// Testing
    String s = 
      "GET /favicon.ico#test HTTP/1.1\r\n" +
      "Host: localhost:7262\r\n" +
      "User-Agent: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.10) Gecko/2009042315 Firefox/3.0.10 Ubiquity/0.1.5\r\n" +
      "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
      "Accept-Language: en-us,en;q=0.5\r\n" +
      "Accept-Encoding: gzip,deflate\r\n" +
      "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n" +
      "Keep-Alive: 300\r\n" +
      "Connection: keep-alive\r\n\r\n";
    System.out.println("Input Request: (" + s.length() + " bytes)");System.out.println(s);
    byte[] data = s.getBytes();
    int len = data.length;
    
    System.out.println("=============================================================");
    HttpRequest req = new HttpRequest();
    req.buffer = ByteBuffer.allocate(2048);
    req.buffer.put(data);
    initHeader(req, len);
    System.out.println(req);
  }
}

