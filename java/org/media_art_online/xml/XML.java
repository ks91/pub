//---------------------------------------------------------------------------
// Freely available from Media Art Online (http://www.media-art-online.org/).
// Copyright (C) 2015 Media Art Online (cafe@media-art-online.org)
//
// This file is part of xml.
//
// xml is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// xml is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
// Package
//---------------------------------------------------------------------------
package org.media_art_online.xml;

//---------------------------------------------------------------------------
// Import
//---------------------------------------------------------------------------
import java.io.*;
import java.util.LinkedList;
import java.util.Stack;

public class XML {

    public static final String S_ERR_BAD_ATTR   = "XML_ERR_BAD_ATTR";
    public static final String S_ERR_BAD_EOF    = "XML_ERR_BAD_EOF";
    public static final String S_ERR_IO         = "XML_ERR_IO";
    public static final String S_ERR_NOT_CLOSED = "XML_ERR_NOT_CLOSED";
    public static final String S_ERR_NOT_OPENED = "XML_ERR_NOT_OPENED";
    public static final String S_ERR_NO_VALUE   = "XML_ERR_NO_VALUE";

    public static final String S_HEAD
     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String S_ASSIGN        = "=";
    public static final String S_CLOSER        = ">";
    public static final String S_CLOSE_PREFIX  = "/";
    public static final String S_OPENER        = "<";
    public static final String S_STREAM_STREAM = "stream:stream";
    public static final String S_XML           = "xml";
    public static final String S_XMLNS         = "xmlns";
    public static final String S_XQ            = "?";
    public static final String S_XE            = "!";

    public static final String ENT_BEGIN   = "&";
    public static final String ENT_BEGIN10 = "&#";
    public static final String ENT_BEGIN16 = "&#x";
    public static final String ENT_AMP     = "&amp;";
    public static final String ENT_APOS    = "&apos;";
    public static final String ENT_CLOSER  = "&gt;";
    public static final String ENT_END     = ";";
    public static final String ENT_OPENER  = "&lt;";
    public static final String ENT_QUOTE   = "&quot;";

    public static final int ERROR = -2;
    public static final int EOF   = -1;
    public static final int OK    = 0;
    public static final int DONE  = 1;

    public XML(File file) {
        this(file, "UTF-8");
    }

    public XML(File file, String sEncoding) {
        this();

        try {
            _reader = new PushbackReader(sEncoding == null
             ? new InputStreamReader(new FileInputStream(file))
             : new InputStreamReader(new FileInputStream(file), sEncoding),
             LEN_BUF_PUSHBACK);

        } catch (IOException unused) {
            System.err.println("xml: could not open a file.");
        }
    }

    public XML(Reader reader) {
        this();

        _reader = new PushbackReader(reader, LEN_BUF_PUSHBACK);
    }

    public void close() {

        try {
            _reader.close();

        } catch (IOException unused) {
            _result = ERROR;
        }
    }

    public LinkedList<XMLParseError> getErrors() {
        return (_loErrors);
    }

    public int getLineNumber() {
        return (_iLine);
    }

    public XMLElement getRootElement() {
        return (_eRoot);
    }

    public int parse() {

        String sTemp = "";
        String s;
        boolean isClosing = false;
        int state = WAIT_OPEN;
        int c;

        _loErrors.clear();
        _result = OK;

        for (; ;) {

            if (_result != OK || isEndOfFile()) {
                break;
            }

            switch (state) {
                case WAIT_OPEN:
                    s = readWhileSpace();

                    if (!_stack.empty()) {
                        sTemp += s;
                    }

                    if (isStartOf(S_OPEN_CLOSE)) {
                        if (!sTemp.equals("")) {
                            _stack.peek().getCurrentElement().add(sTemp);
                            sTemp = "";
                        }

                        state = WAIT_NAME;
                        isClosing = true;

                    } else if (isStartOf(S_OPEN_COMMENT)) {
                        ignoreComment();

                    } else if (isStartOf(S_OPEN_CDATA)) {
                        state = WAIT_CLOSE_CDATA;

                    } else if (isStartOf(S_OPEN_XE)) {
                        ignoreDeclaration();

                    } else if (isStartOf(S_OPEN)) {

                        state = WAIT_NAME;

                        if (!sTemp.equals("")) {
                            _stack.peek().getCurrentElement().add(sTemp);
                            sTemp = "";
                        }

                        skip(C_XQ);

                    } else if (!_stack.empty()) {

                        s = readUntil(C_OPENER, true);
                        sTemp += decode(s);

                    } else if (_result == OK) {
                        _result = ERROR;
                    }
                    break;

                case WAIT_NAME:
                    s = readWhileName();

                    if (_stack.empty()) {
                        XMLParsedBlock block = new XMLParsedBlock(null,
                         _eRoot = new XMLElement(s), _iLine);
                        _stack.push(block);

                    } else if (isClosing) {
                        if (_stack.empty()) {
                            if (!s.equals(S_STREAM_STREAM)) {
                                _result = ERROR;
                                _loErrors.add(new XMLParseError(
                                 S_ERR_NOT_OPENED, new Object[] {s}, _iLine));
                            }

                        } else if (_stack.peek().getCurrentElement().getTag()
                         .equals(s)) {
                            _stack.pop();

                        } else {
                            _result = ERROR;

                            XMLParsedBlock block = _stack.peek();

                            _loErrors.add(new XMLParseError(S_ERR_NOT_CLOSED,
                             new Object[] {block.getCurrentElement().getTag()},
                             block.getLineNumber()));
                        }

                    } else {
                        XMLParsedBlock block = new XMLParsedBlock(
                         _stack.peek().getCurrentElement(), new XMLElement(s),
                         _iLine);
                        _stack.push(block);
                    }

                    state = WAIT_CLOSE;
                    break;

                case WAIT_CLOSE:
                    readWhileSpace();

                    if ((char)(c = readChar()) == C_CLOSER) {
                        state = WAIT_OPEN;
                        sTemp = "";

                        if (_eRoot.getTag().equals(S_STREAM_STREAM)) {
                            isClosing = true;
                            _stack.pop();
                        }

                        if (isClosing) {
                            isClosing = false;

                            if (_stack.empty()) {
                                _result = DONE;
                            }
                        }

                    } else if (c == C_CLOSE_PREFIX || c == C_XQ) {
                        isClosing = true;
                        _stack.pop();

                    } else if (Character.isLetter((char)c)) {
                        s = String.valueOf((char)c) + readWhileName();

                        sTemp = s;

                        if (skip(C_ASSIGN)) {
                            state = WAIT_VALUE;

                        } else {
                            _loErrors.add(new XMLParseError(S_ERR_NO_VALUE,
                             new Object[] {sTemp}, _iLine));
                        }

                    } else {
                        _result = ERROR;

                        _loErrors.add(new XMLParseError(S_ERR_BAD_ATTR,
                         new Object[] {"" + (char)c}, _iLine));
                    }
                    break;

                case WAIT_VALUE:
                    if ((c = readChar()) == EOF) {
                        _result = ERROR;

                        _loErrors.add(new XMLParseError(S_ERR_BAD_EOF,
                         new Object[] {}, _iLine));

                    } else {        
                        s = readUntil((char)c, false);

                        XMLAttribute attribute
                         = new XMLAttribute(sTemp, decode(s));
                        _stack.peek().getCurrentElement().addAttribute(
                         attribute);

                        state = WAIT_CLOSE;
                    }
                    break;

                case WAIT_CLOSE_CDATA:
                    s = readUntil(S_CLOSE_CDATA);

                    state = WAIT_OPEN;
                    sTemp += s;
                    break;

                default:
                    break;
            }
        }

        if (_loErrors.size() > 0) {
            _result = ERROR;
        }

        return (_result);
    }

    public static String encodeText(String sCDATA) {
        return (encode(sCDATA, false));
    }

    public static String encodeAttribute(String sCDATA) {

        return (encode(sCDATA, true));
    }

    public static String encode(String sCDATA, boolean isAttr) {
        String s = (isAttr ? S_QUOTE : "");
        String s1;
        char c;

        for (int i = 0; i < sCDATA.length(); i++) {
            switch ((c = sCDATA.charAt(i))) {
                case C_OPENER:
                    s1 = ENT_OPENER;
                    break;

                case C_CLOSER:
                    s1 = (isAttr ? String.valueOf(c) : ENT_CLOSER);
                    break;

                case C_AMP:
                    s1 = ENT_AMP;
                    break;

                case C_QUOTE:
                    s1 = (isAttr ? ENT_QUOTE : String.valueOf(c));
                    break;

                case C_TAB:
                case C_CR:
                case C_LF:
                    if (c == C_CR || isAttr) {
                        s1 = ENT_BEGIN16
                         + Integer.toString((int)c, 16).toUpperCase()
                         + ENT_END;

                    } else {
                        s1 = String.valueOf(c);
                    }
                    break;

                case C_VT:
                    s1 = "\n";
                    break;

                default:
                    s1 = String.valueOf(c);
                    break;
            }

            s += s1;
        }

        if (isAttr) {
            s += S_QUOTE;
        }

        return (s);
    }

    public static String decode(final String s) {
        String sCDATA = "";
        String sTemp;
        int idx = 0;
        int i;
        int j;

        while (idx < s.length()) {
            if ((i = s.indexOf(ENT_BEGIN, idx)) >= 0) {
                if ((j = s.indexOf(ENT_END, i + 1)) >= 0) {
                    if (s.charAt(i + 1) == C_BEGIN_CODE) {
                        sTemp = (s.charAt(i + 2) == C_BEGIN16 ? "0" : "")
                         + s.substring(i + 2, j);

                        sTemp = String.valueOf((char)Integer.decode(sTemp)
                         .intValue());

                    } else {
                        if ((sTemp = s.substring(i, j + 1))
                         .equals(ENT_AMP)) {
                            sTemp = String.valueOf(C_AMP);

                        } else if (sTemp.equals(ENT_APOS)) {
                            sTemp = String.valueOf(C_APOS);

                        } else if (sTemp.equals(ENT_CLOSER)) {
                            sTemp = String.valueOf(C_CLOSER);

                        } else if (sTemp.equals(ENT_OPENER)) {
                            sTemp = String.valueOf(C_OPENER);

                        } else if (sTemp.equals(ENT_QUOTE)) {
                            sTemp = String.valueOf(C_QUOTE);

                        } else {
                            sTemp = String.valueOf(C_UNDEF);
                        }
                    }

                    sCDATA += s.substring(idx, i) + sTemp;

                    idx = j + 1;

                } else {
                    idx = i + 1;
                }

            } else {
                sCDATA += s.substring(idx);
                break;
            }
        }

        return (sCDATA);
    }

    public static boolean isCanonical = true;

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private XML() {
        _eRoot = null;
        _loErrors = new LinkedList<XMLParseError>();
        _stack = new Stack<XMLParsedBlock>();
        _iLine = 1;
    }

    private void ignoreComment() {

        int c;

        try {

            while ((c = _reader.read()) != EOF) {

                if (c == C_LF) {
                    _iLine++;
                }

                if (c == C_BEGIN_CCOM) {
                    if (isStartOf(S_REST_CCOM)) {
                        return;
                    }
                }
            }

        } catch (IOException unused) {
        }

        _result = ERROR;

        _loErrors.add(new XMLParseError(S_ERR_BAD_EOF, new Object[] {},
         _iLine));
    }

    private void ignoreDeclaration() {

        int cntNest = 1;
        int c;

        try {

            while ((c = _reader.read()) != EOF) {

                switch ((char)c) {
                    case C_OPENER:
                        cntNest++;
                        break;

                    case C_CLOSER:
                        if (--cntNest <= 0) {
                            return;
                        }
                        break;

                    case C_LF:
                        _iLine++;
                        break;

                    default:
                        break;
                }
            }

        } catch (IOException unused) {
        }

        _result = ERROR;

        _loErrors.add(new XMLParseError(S_ERR_BAD_EOF, new Object[] {},
         _iLine));
    }

    private boolean isEndOfFile() {

        boolean isEof = false;
        int c;

        try {
            if ((c = _reader.read()) == EOF) {
                isEof = true;
                _result = EOF;

            } else {
                _reader.unread(c);
            }

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (isEof);
    }

    private boolean isStartOf(String s) {

        int len = s.length();
        int i = 0;
        int c;

        for (i = 0; i < len; i++) {

            try {

                if ((char)(c = _reader.read()) != s.charAt(i)) {

                    _reader.unread(c);

                    for (int j = i - 1; j >= 0; j--) {
                        _reader.unread((int)s.charAt(j));
                    }
                    break;
                }

            } catch (IOException unused) {
                _result = ERROR;

                _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
                 _iLine));
            }
        }

        return (i >= len);
    }

    private int readChar() {
        int c;

        try {

            c = _reader.read();

        } catch (IOException unused) {

            c = EOF;
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (c);
    }

    private String readUntil(char cStop, boolean isUnreading) {

        StringBuffer buffer = new StringBuffer();
        int c;

        try {
            while ((c = _reader.read()) != EOF && (char)c != cStop) {

                if (c == C_LF) {
                    _iLine++;
                }

                buffer.append((char)c);
            }

            if (isUnreading || c == EOF) {
                _reader.unread(c);

                if (c == EOF) {
                    _result = ERROR;

                    _loErrors.add(new XMLParseError(S_ERR_BAD_EOF,
                     new Object[] {}, _iLine));
                }
            }

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (buffer.toString());
    }

    private String readUntil(String s) {

        StringBuffer buffer = new StringBuffer();
        char cStop = s.charAt(0);
        int c;

        s = s.substring(1);

        try {
            while ((c = _reader.read()) != EOF) {

                if (c == C_LF) {
                    _iLine++;
                }

                if ((char)c == cStop && isStartOf(s)) {
                    break;
                }

                buffer.append((char)c);
            }

            if (c == EOF) {
                _result = ERROR;

                _loErrors.add(new XMLParseError(S_ERR_BAD_EOF, new Object[] {},
                 _iLine));
            }

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (buffer.toString());
    }

    private String readWhileName() {

        StringBuffer buffer = new StringBuffer();
        int c;

        try {
            while ((c = _reader.read()) != EOF
             && !Character.isWhitespace((char)c) && (char)c != C_CLOSER
             && (char)c != C_CLOSE_PREFIX && (char)c != C_XQ
             && (char)c != C_ASSIGN) {
                buffer.append((char)c);
            }

            _reader.unread(c);

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (buffer.toString());
    }

    private String readWhileSpace() {

        StringBuffer buffer = new StringBuffer();
        int c;

        try {
            while ((c = _reader.read()) != EOF
             && Character.isWhitespace((char)c)) {

                if (c == C_LF) {
                    _iLine++;
                }

                buffer.append((char)c);
            }
    
            _reader.unread(c);

            if (c == EOF) {
                _result = EOF;
            }

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (buffer.toString());
    }

    private boolean skip(char cSkip) {

        boolean hasSkipped = true;
        int c;

        try {
            if ((c = _reader.read()) == EOF || (char)c != cSkip) {
                _reader.unread(c);
                hasSkipped = false;
            }

        } catch (IOException unused) {
            _result = ERROR;

            _loErrors.add(new XMLParseError(S_ERR_IO, new Object[] {},
             _iLine));
        }

        return (hasSkipped);
    }

    private LinkedList<XMLParseError> _loErrors;

    private Stack<XMLParsedBlock> _stack;

    private PushbackReader _reader;

    private XMLElement _eRoot = null;

    private int _iLine;
    private int _result;

    private static final int WAIT_OPEN          = 0;
    private static final int WAIT_NAME          = 1;
    private static final int WAIT_CLOSE         = 2;
    private static final int WAIT_VALUE         = 3;
    private static final int WAIT_CLOSE_CDATA   = 4;

    private static final int IGNORE_NONE        = 0;
    private static final int IGNORE_DECLARATION = 1;
    private static final int IGNORE_COMMENT     = 2;
 
    private static final String S_CLOSE_CDATA   = "]]>";
    private static final String S_REST_CCOM     = "->";
    private static final String S_OPEN          = "<";
    private static final String S_OPEN_CDATA    = "<![CDATA[";
    private static final String S_OPEN_CLOSE    = "</";
    private static final String S_OPEN_COMMENT  = "<!--";
    private static final String S_OPEN_XE       = "<!";
    private static final String S_QUOTE         = "\"";

    private static final int LEN_BUF_PUSHBACK = 9;

    private static final char C_AMP          = '&';
    private static final char C_APOS         = '\'';
    private static final char C_ASSIGN       = '=';
    private static final char C_BEGIN_CODE   = '#';
    private static final char C_BEGIN_CCOM   = '-';
    private static final char C_BEGIN16      = 'x';
    private static final char C_CLOSER       = '>';
    private static final char C_CLOSE_PREFIX = '/';
    private static final char C_CR           = '\r';
    private static final char C_LF           = '\n';
    private static final char C_OPENER       = '<';
    private static final char C_QUOTE        = '"';
    private static final char C_TAB          = '\t';
    private static final char C_UNDEF        = '.';
    private static final char C_VT           = 0x0b;
    private static final char C_XQ           = '?';
}

class XMLParsedBlock {
    public XMLParsedBlock(XMLElement eParent, XMLElement eCurrent, int iLine) {
        _eCurrent = eCurrent;

        if ((_eParent = eParent) != null) {
            _eParent.add(_eCurrent);
        }

        _iLine = iLine;
    }

    public XMLElement getCurrentElement() {
        return (_eCurrent);
    }

    public int getLineNumber() {
        return (_iLine);
    }

    private XMLElement _eParent;
    private XMLElement _eCurrent;

    private int _iLine;
}

// end of XML.java
