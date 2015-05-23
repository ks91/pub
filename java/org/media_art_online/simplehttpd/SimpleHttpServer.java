//---------------------------------------------------------------------------
// Freely available from Media Art Online (http://www.media-art-online.org/).
// Copyright (C) 2015 Media Art Online (cafe@media-art-online.org)
//
// This file is part of SimpleHttpd.
//
// SimpleHttpd is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// SimpleHttpd is distributed in the hope that it will be useful,
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
package org.media_art_online.simplehttpd;

//---------------------------------------------------------------------------
// Import
//---------------------------------------------------------------------------
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.*;
import java.util.Random;
import org.media_art_online.xml.*;

public class SimpleHttpServer implements SimpleHttpdVersion {

    public static final String S_SERVER_NAME = S_NAME + " " + S_CAPS_VER;

    public static final String S_ADDRESS
     = "<hr/><address>" + S_SERVER_NAME + "</address>";

    public static final String S_BEGIN_ANCHOR = "<a href=\"";
    public static final String S_BEGIN_ANCHOR_CLOSE = "\">";

    public static final String S_BEGIN_HTML5
     = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><title>";

    public static final String S_BEGIN_ICON = "<img src=\"";

    public static final String S_BEGIN_TABLE        = "<table>";
    public static final String S_BEGIN_TABLE_DATA   = "<td>";
    public static final String S_BEGIN_TABLE_DATE
                 = "<td style=\"padding-left: 20px; padding-right: 20px;\">";
    public static final String S_BEGIN_TABLE_NUMBER
                                     = "<td style=\"text-align: right;\">";
    public static final String S_BEGIN_TABLE_HEADER = "<th>";
    public static final String S_BEGIN_TABLE_HEADER_COLSPAN_2
                                     = "<th colspan=\"2\">";
    public static final String S_BEGIN_TABLE_ROW    = "<tr>";

    public static final String S_BODY = "</title></head><body>";

    public static final String S_CONTENT_TYPE
     = "Content-Type: text/html; charset=iso-8859-1\r\n";

    public static final String S_END_ANCHOR = "</a>";

    public static final String S_END_HTML5 = "</body></html>";

    public static final String S_END_ICON = "\" alt=\"[icon]\" width=\"24\"/>";

    public static final String S_END_TABLE        = "</table>";
    public static final String S_END_TABLE_DATA   = "</td>";
    public static final String S_END_TABLE_HEADER = "</th>";
    public static final String S_END_TABLE_ROW    = "</tr>";

    public static final String S_SERVER = "Server: " + S_SERVER_NAME + "\r\n";

    public static final String S_302_FOUND        = "302 Found";
    public static final String S_304_NOT_MODIFIED = "304 Not Modified";

    public static final String S_400_BODY
     = "<h1>Bad Request</h1>"
     + "<p>The request was malformed.</p>"
     + S_ADDRESS;
    public static final String S_400_BAD_REQUEST = "400 Bad Request";

    public static final String S_403_BODY
     = "<h1>Forbidden</h1>"
     + "<p>The requested resource was not accessible on the remote node.</p>"
     + S_ADDRESS;
    public static final String S_403_FORBIDDEN = "403 Forbidden";

    public static final String S_404_BODY
     = "<h1>Not Found</h1>"
     + "<p>The requested resource was not found on the remote node.</p>"
     + S_ADDRESS;
    public static final String S_404_NOT_FOUND = "404 Not Found";

    public static final String S_500_BODY_HEAD
     = "<h1>Internal Server Error</h1>"
     + "<p>There was a software error on the remote node.</p><pre>\n";
    public static final String S_500_BODY_TAIL
     = "</pre>"
     + S_ADDRESS;
    public static final String S_500_INTERNAL = "500 Internal Server Error";

    public static final String S_503_BODY
     = "<h1>Service Unavailable</h1>"
     + "<p>The server is currently unable to handle the request due to a "
     + "temporary overloading or maintenance of the server.</p>"
     + S_ADDRESS;
    public static final String S_503_SERVICE_UNAVAILABLE
     = "503 Service Unavailable";

    public static String getBadRequestResponse() {
        return ("HTTP/1.1 400 Bad Request\r\n"
         + S_SERVER
         + S_CONTENT_TYPE
         + "\r\n"
         + S_BEGIN_HTML5 + S_400_BAD_REQUEST
         + S_BODY + S_400_BODY + S_END_HTML5);
    }

    public static void clean() {

        if (_socket != null) {

            try {
                _socket.close();

            } catch (IOException unused) {
            }
        }
    }

    public static SimpleDateFormat getDateFormat() {
        return (_format);
    }

    public static String getForbiddenResponse() {
        return ("HTTP/1.1 403 Forbidden\r\n"
         + S_SERVER
         + S_CONTENT_TYPE
         + "\r\n"
         + S_BEGIN_HTML5 + S_403_FORBIDDEN
         + S_BODY + S_403_BODY + S_END_HTML5);
    }

    public static String getInternalServerErrorResponse(Exception ex) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(output);
        ex.printStackTrace(print);
        ex.printStackTrace();

        return ("HTTP/1.1 500 Internal Server Error\r\n"
         + S_SERVER
         + S_CONTENT_TYPE
         + "\r\n"
         + S_BEGIN_HTML5 + S_500_INTERNAL
         + S_BODY + S_500_BODY_HEAD + output.toString() + S_500_BODY_TAIL
         + S_END_HTML5);
    }

    public static String getNotFoundResponse() {
        return ("HTTP/1.1 404 Not Found\r\n"
         + S_SERVER
         + S_CONTENT_TYPE
         + "\r\n"
         + S_BEGIN_HTML5 + S_404_NOT_FOUND
         + S_BODY + S_404_BODY + S_END_HTML5);
    }

    public static String getNotModifiedResponse() {

        SimpleHttpd.logger.fine("http: not modified.");

        return ("HTTP/1.1 304 Not Modified\r\n"
         + S_SERVER
         + "Date: " + _format.format(new Date(System.currentTimeMillis()))
         + "\r\n\r\n");
    }

    public static String getOKResponse(long size, String sType) {
        return ("HTTP/1.1 200 OK\r\n"
         + S_SERVER
         + "Accept-Ranges: bytes\r\n"
         + "Content-Length: " + size + "\r\n"
         + "Content-Type: " + sType + "\r\n\r\n");
    }

    public static String getOverloadedResponse() {
        return ("HTTP/1.1 503 Service Unavailable\r\n"
         + S_SERVER
         + S_CONTENT_TYPE
         + "\r\n"
         + S_BEGIN_HTML5 + S_503_SERVICE_UNAVAILABLE
         + S_BODY + S_503_BODY + S_END_HTML5);
    }

    public static String getRedirectResponse(String sURL) {
        return ("HTTP/1.1 302 Found\r\n"
         + S_SERVER
         + "Location: " + sURL + "\r\n\r\n");
    }

    public static SimpleHttpServingThread getThread() {
        return ((SimpleHttpServingThread)Thread.currentThread());
    }

    public void run() {

        try {
            _socket = new ServerSocket(SimpleHttpd.iPort);

        } catch (Exception exception) {
            throw (new RuntimeException(exception));
        }

        try {
            for (; ;) {
                (new SimpleHttpServingThread(_socket.accept())).start();
            }

        /*
         * whatever happened should terminate the server.
         */
        } catch (IOException exception) {
            throw (new RuntimeException(exception));

        } finally {

            try {
                _socket.close();

            } catch (IOException unused) {
                throw (new RuntimeException(unused));
            }
        }
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private static ServerSocket _socket = null;

    private static final String S_FORMAT_DATE = "EEE, dd MMM yyyy hh:mm:ss z";

    private static SimpleDateFormat _format
     = new SimpleDateFormat(S_FORMAT_DATE, new Locale("en", "US"));
}

// end of SimpleHttpServer.java
