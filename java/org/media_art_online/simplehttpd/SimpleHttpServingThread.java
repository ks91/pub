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
import java.text.*;
import java.util.Date;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import org.media_art_online.mime.*;
import org.media_art_online.xml.*;

public class SimpleHttpServingThread extends Thread {

    public static final String S_ACCEPT_LANGUAGE   = "Accept-Language: ";
    public static final String S_CONTENT_LENGTH    = "Content-Length: ";
    public static final String S_IF_MODIFIED_SINCE = "If-Modified-Since: ";
    public static final String S_USER_AGENT        = "User-Agent: ";

    public static final String S_HTTP_GET = "GET ";
    public static final String S_HTTP_PUT = "PUT ";

    public static final String S_DIR_CURRENT   = ".";
    public static final String S_DIR_RESOURCE  = "_";
    public static final String S_INDEX_DEFAULT = "index.html";

    public static final String S_PATH_ICON_AUDIO   = "/_res/wav.png";
    public static final String S_PATH_ICON_CSS     = "/_res/css.png";
    public static final String S_PATH_ICON_DEFAULT = "/_res/default.png";
    public static final String S_PATH_ICON_FOLDER  = "/_res/folder.png";
    public static final String S_PATH_ICON_HTML    = "/_res/html.png";
    public static final String S_PATH_ICON_IMAGE   = "/_res/jpg.png";
    public static final String S_PATH_ICON_JS      = "/_res/js.png";
    public static final String S_PATH_ICON_PDF     = "/_res/acrobat.png";
    public static final String S_PATH_ICON_TEXT    = "/_res/txt.png";
    public static final String S_PATH_ICON_VIDEO   = "/_res/video.png";
    public static final String S_PATH_ICON_ZIP     = "/_res/archive.png";

    public static final String S_NA = "--";

    public static final String S_ISO_3166_JAPAN = "JP";
    public static final String S_ISO_3166_USA   = "US";

    public static final String S_ISO_639_ENGLISH  = "en";
    public static final String S_ISO_639_JAPANESE = "ja";

    public static final String S_VAR_PLATFORM_AU          = "AU";
    public static final String S_VAR_PLATFORM_DOCOMO      = "DoCoMo";
    public static final String S_VAR_PLATFORM_IPOD        = "IPOD";
    public static final String S_VAR_PLATFORM_NINTENDO_DS = "NINTENDO-DS";
    public static final String S_VAR_PLATFORM_SOFTBANK    = "SOFTBANK";
    public static final String S_VAR_PLATFORM_WILLCOM     = "WILLCOM";

    public SimpleHttpServingThread(Socket socket) {
        _socket = socket;
        _losLines = new LinkedList<String>();
    }

    public long getClientCacheTime() {

        String s = getInputLine(S_IF_MODIFIED_SINCE);
        long lTime = 0;

        if (s != null) {
            try {
                lTime = SimpleHttpServer.getDateFormat().parse(s).getTime();

            } catch (ParseException ex) {
                SimpleHttpd.logger.warning(httpLogHead(false) + " time: "
                 + ex.toString());
            }
        }

        return (lTime);
    }

    public String getInputLine(String sName) {

        for (String s : _losLines) {

            if (s.startsWith(sName)) {
                return (s.substring(sName.length()));
            }
        }

        return (null);
    }

    public Collection<String> getInputLines() {
        return (_losLines);
    }

    public Collection<String[]> getParameters() {

        String s = _losLines.getFirst();

        if (s.startsWith(S_HTTP_GET)) {
            int idx = s.indexOf("?");
            s = (idx >= 0 ? s.substring(idx + 1, s.lastIndexOf(" ")) : "");

        } else {
            s = _losLines.getLast();
        }

        LinkedList<String[]> loaos = new LinkedList<String[]>();
        String[] aos = s.split("&");

        for (int i = 0; i < aos.length; i++) {

            String[] aosArg = aos[i].split("=");

            if (aosArg.length > 1) {

                try {
                    aosArg[1] = URLDecoder.decode(aosArg[1], "UTF-8");

                } catch (UnsupportedEncodingException unused) {
                }

                loaos.add(aosArg);
            }
        }

        return (loaos);
    }

    public Locale getRemoteLocale() {

        String[] aosLC = null;
        String sAgent = "";

        for (String s : _losLines) {

            if (s.startsWith(S_ACCEPT_LANGUAGE)) {
                
                s = s.substring(S_ACCEPT_LANGUAGE.length());

                int idxJa = s.indexOf(S_ISO_639_JAPANESE);
                int idxEn = s.indexOf(S_ISO_639_ENGLISH);

                aosLC = (idxEn >= 0 && idxEn < idxJa || idxJa < 0
                 ? AOS_ENGLISH_USA : AOS_JAPANESE_JAPAN);
                
            } else if (s.startsWith(S_USER_AGENT)) {
                sAgent = s.substring(S_USER_AGENT.length());

                if (sAgent.startsWith(S_VAR_PLATFORM_DOCOMO)) {
                    aosLC = AOS_JAPANESE_JAPAN;
                }
            }
        }

        if (aosLC == null) {
            aosLC = AOS_JAPANESE_JAPAN;
        }

        return (new Locale(aosLC[IDX_LANG], aosLC[IDX_COUNTRY], sAgent));
    }

    public ResourceBundle getRemoteLocaleResourceBundle() {
        return (ResourceBundle.getBundle(SimpleHttpd.S_CLASS_RESOURCE,
         getRemoteLocale()));
    }

    public String getTerminalUID() {

        String sUID = null;
        String sAgent = getInputLine(S_USER_AGENT);

        SimpleHttpd.logger.fine("user-agent: " + sAgent);

        if (sAgent.startsWith("DoCoMo") || sAgent.startsWith("docomo")) {

            int index = sAgent.indexOf(";icc");

            if (index > 0) {

                int index1 = sAgent.indexOf(")", index);
                sUID = "docomo"
                 + sAgent.substring(index + ";icc".length(), index1);

            } else if ((index = sAgent.indexOf("/ser")) > 0) {
                sUID = "docomo" + sAgent.substring(index + "/ser".length());
            }

        } else if (sAgent.startsWith("KDDI")
         || sAgent.startsWith("UP.Browser")) {

            sUID = getInputLine("x-up-subno: ");
            sUID = (sUID == null ? null : "au" + sUID);

        } else if (sAgent.startsWith("Vodafone")
         || sAgent.startsWith("SoftBank")
         || sAgent.startsWith("J-PHONE")) {

            int index = sAgent.indexOf("/SN");

            if (index > 0) {
                int index1 = sAgent.indexOf(" ", index);
                sUID = "softbank" + sAgent.substring(index + "/SN".length(),
                 index1);
            }
        }

        SimpleHttpd.logger.fine("terminal UID: " + sUID);
        return (sUID);
    }

    @Override
    public void run() {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
             _socket.getInputStream()));

            String s;
            int iLength = 0;

            while ((s = reader.readLine()) != null) {

                _losLines.add(s);

                if (s.startsWith(S_CONTENT_LENGTH)) {

                    try {
                        iLength = Integer.parseInt(
                         s.substring(S_CONTENT_LENGTH.length()));

                    } catch (NumberFormatException unused) {
                    }

                } else if (s.length() <= 0) {

                    for (int i = 0; i < iLength; i++) {
                        s += (char)reader.read();
                    }

                    if (s.length() > 0) {
                        _losLines.add(s);
                    }

                    String sLog = "";

                    for (String sLine : _losLines) {
                        sLog += sLine + "\n";
                    }

                    SimpleHttpd.logger.fine(httpLogHead(false) + "\n" + sLog);

                    s = _losLines.getFirst();

                    int index0 = s.indexOf("/");
                    int index1 = s.indexOf("/", index0 + 1);
                    int index2 = s.indexOf(" ", index0 + 1);
                    int index3 = s.indexOf(".", index0 + 1);

                    String sFile = (index2 == index0 + 1
                     ? S_DIR_CURRENT : s.substring(index0 + 1, index2));

                    try {
                        sFile = URLDecoder.decode(sFile, "UTF-8");

                    } catch (UnsupportedEncodingException unused) {
                    }

                    if (!sFile.startsWith(S_DIR_RESOURCE)) {

                        String[] aos = sFile.split("/");

                        sFile = aos[0];

                        for (int i = 1; i < aos.length; i++) {
                            sFile += File.separator + aos[i];
                        }
                    }

                    publicFileTransfer(sFile);

                    _losLines.clear();
                    iLength = 0;
                }
            }

        } catch (IOException unused) {

            /*
             * usually, it is the case that the connection is closed by the
             * peer, which is a some kind of browser (including QuickTime for
             * Java) just finished downloading.
             */

        } finally {

            try {
                if (reader != null) {
                    reader.close();
                }

                _socket.close();

            } catch (IOException unused) {
            }
        }
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private SimpleHttpResource directoryResource(File dir, String sName) {

        SimpleHttpResource res = null;

        try {
            res = new SimpleHttpResource(new File(dir, S_INDEX_DEFAULT));

        } catch (Exception unused) {
        }

        if (res != null) {
            return (res);
        }

        String s = SimpleHttpServer.S_BEGIN_HTML5 + dir.getName()
         + SimpleHttpServer.S_BODY + "\n";

        File[] aoFiles = dir.listFiles();

        s += SimpleHttpServer.S_BEGIN_TABLE + "\n";

        s += getIndexTableHeaderRowString();

        if (!dir.getName().equals(".")) {
            s += getIndexTableRowString("../", S_NA, S_NA);
        }

        SimpleDateFormat formatTime = new SimpleDateFormat(S_FORMAT_TIME);
        DecimalFormat formatDec = new DecimalFormat("#.#");

        for (File file : aoFiles) {

            String sFile = file.getName();

            if (!sFile.startsWith(".")) {

                boolean isDir = file.isDirectory();

                s += getIndexTableRowString(sFile + (isDir ? "/" : ""),
                 formatTime.format(new Date(file.lastModified())),
                 (isDir ? S_NA : getByteSizeString(file.length(), formatDec)));
            }
        }

        s += SimpleHttpServer.S_END_TABLE + "\n"
         + SimpleHttpServer.S_ADDRESS + SimpleHttpServer.S_END_HTML5;

        try {
            return (new SimpleHttpResource(sName, s, "UTF-8"));

        } catch (UnsupportedEncodingException unused) {
            return (null);
        }
    }

    private String getByteSizeString(long lSize, DecimalFormat format) {

        long x;

        if ((x = lSize / L_GB) > 0) {
            return (format.format(lSize / (double)L_GB)
             + SimpleHttpServer.S_END_TABLE_DATA
             + SimpleHttpServer.S_BEGIN_TABLE_DATA
             + SimpleHttpd.getString("LABEL_BYTES_GB"));
        }

        if ((x = lSize / L_MB) > 0) {
            return (format.format(lSize / (double)L_MB)
             + SimpleHttpServer.S_END_TABLE_DATA
             + SimpleHttpServer.S_BEGIN_TABLE_DATA
             + SimpleHttpd.getString("LABEL_BYTES_MB"));
        }

        if ((x = lSize / L_KB) > 0) {
            return (String.valueOf(x)
             + SimpleHttpServer.S_END_TABLE_DATA
             + SimpleHttpServer.S_BEGIN_TABLE_DATA
             + SimpleHttpd.getString("LABEL_BYTES_KB"));
        }

        return (String.valueOf(lSize)
         + SimpleHttpServer.S_END_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_TABLE_DATA
         + SimpleHttpd.getString("LABEL_BYTES"));
    }

    private String getIndexTableHeaderRowString() {
        return (SimpleHttpServer.S_BEGIN_TABLE_ROW
         + SimpleHttpServer.S_BEGIN_TABLE_HEADER
         + SimpleHttpServer.S_END_TABLE_HEADER
         + SimpleHttpServer.S_BEGIN_TABLE_HEADER
         + SimpleHttpd.getString("LABEL_NAME")
         + SimpleHttpServer.S_END_TABLE_HEADER
         + SimpleHttpServer.S_BEGIN_TABLE_HEADER
         + SimpleHttpd.getString("LABEL_LAST_MODIFIED")
         + SimpleHttpServer.S_END_TABLE_HEADER
         + SimpleHttpServer.S_BEGIN_TABLE_HEADER_COLSPAN_2
         + SimpleHttpd.getString("LABEL_SIZE")
         + SimpleHttpServer.S_END_TABLE_HEADER
         + SimpleHttpServer.S_END_TABLE_ROW + "\n");
    }

    private String getIndexTableRowString(String sFile, String sDate,
     String sSize) {

        String s = "";

        if (sFile.endsWith("/")) {
            s = S_PATH_ICON_FOLDER;

        } else {

            String sType = MIME.getFileType(sFile);

            if (sType.equals(MIME.S_TYPE_HTML)) {
                s = S_PATH_ICON_HTML;

            } else if (sType.equals(MIME.S_TYPE_CSS)) {
                s = S_PATH_ICON_CSS;

            } else if (sType.equals(MIME.S_TYPE_JS)) {
                s = S_PATH_ICON_JS;

            } else if (sType.equals(MIME.S_TYPE_PDF)) {
                s = S_PATH_ICON_PDF;

            } else if (MIME.isImage(sType)) {
                s = S_PATH_ICON_IMAGE;

            } else if (MIME.isText(sType)) {
                s = S_PATH_ICON_TEXT;

            } else if (MIME.isArchive(sType)) {
                s = S_PATH_ICON_ZIP;

            } else if (MIME.isAudio(sType)) {
                s = S_PATH_ICON_AUDIO;

            } else if (MIME.isVideo(sType)) {
                s = S_PATH_ICON_VIDEO;

            } else {
                s = S_PATH_ICON_DEFAULT;
            }
        }

        return (SimpleHttpServer.S_BEGIN_TABLE_ROW
         + SimpleHttpServer.S_BEGIN_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_ICON + s + SimpleHttpServer.S_END_ICON
         + SimpleHttpServer.S_END_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_ANCHOR + sFile
         + SimpleHttpServer.S_BEGIN_ANCHOR_CLOSE
         + sFile + SimpleHttpServer.S_END_ANCHOR
         + SimpleHttpServer.S_END_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_TABLE_DATE
         + sDate
         + SimpleHttpServer.S_END_TABLE_DATA
         + SimpleHttpServer.S_BEGIN_TABLE_NUMBER
         + sSize
         + SimpleHttpServer.S_END_TABLE_DATA
         + SimpleHttpServer.S_END_TABLE_ROW + "\n");
    }

    private String httpLogHead(boolean isOutput) {

        InetSocketAddress addr
         = (InetSocketAddress)_socket.getRemoteSocketAddress();

        return ("http " + String.format("%08x", this.hashCode()) + " "
         + (isOutput
         ? SimpleHttpdLogger.S_HTTP_OUTPUT : SimpleHttpdLogger.S_HTTP_INPUT)
         + " [" + addr.getAddress() + "]:" + addr.getPort());
    }

    private void publicFileTransfer(String sPath) throws IOException {

        OutputStream output = _socket.getOutputStream();

        SimpleHttpResource res = null;
        InputStream input = null;
        String sExc = null;
        File file = null;

        try {
            if (sPath.startsWith(S_DIR_RESOURCE)) {
                res = new SimpleHttpResource(getClass().getResource(
                 sPath.substring(S_DIR_RESOURCE.length())));

            } else {
                res = new SimpleHttpResource(file = new File(sPath));
            }

        } catch (SimpleHttpForbiddenException ex) {
            sExc = SimpleHttpServer.getForbiddenResponse();

        } catch (SimpleHttpNotFoundException ex) {
            sExc = SimpleHttpServer.getNotFoundResponse();

        } catch (SimpleHttpNotModifiedException ex) {
            sExc = SimpleHttpServer.getNotModifiedResponse();

        } catch (SimpleHttpRedirectException ex) {
            sExc = SimpleHttpServer.getRedirectResponse(ex.getURL());

        } catch (SimpleHttpOverloadedException ex) {
            sExc = SimpleHttpServer.getOverloadedResponse();

        } catch (FileNotFoundException ex) {
            sExc = SimpleHttpServer.getNotFoundResponse();

        } catch (Exception ex) {
            sExc = SimpleHttpServer.getInternalServerErrorResponse(ex);
        }

        if (sExc == null) {

            if (file != null && file.isDirectory()) {
                res = directoryResource(file, res.getName());
            }

            String sOut = SimpleHttpServer.getOKResponse(
             res.getContentLength(), res.getType());

            SimpleHttpd.logger.fine(httpLogHead(true) + "\n" + sOut);
            output.write(sOut.getBytes());

            input = res.getInputStream();

            byte[] buf = new byte[SIZE_BLOCK];
            int length;

            while ((length = input.read(buf, 0, buf.length)) >= 0) {
                output.write(buf, 0, length);
            }

            if (SimpleHttpd.isCheckingXML() && file != null
             && res.getType().equals(MIME.S_TYPE_HTML)) {

                XML xml = new XML(file);
                int iCode = xml.parse();

                Object[] ao = {
                    file.getName(),
                    iCode == XML.DONE ? "OK" : "NG"
                };

                String s = MessageFormat.format(
                 SimpleHttpd.getString("MSG_XML_CHECK"), ao) + "\n";

                for (XMLParseError error : xml.getErrors()) {

                    s += SimpleHttpd.getString("LABEL_LINE") + " "
                     + error.getLineNumber() + " : "
                     + MessageFormat.format(SimpleHttpd.getString(
                     error.getResourceKey()), error.getAdditionals()) + "\n";
                }

                SimpleHttpd.logger.fine(s + "\n");
            }

        } else {

            output.write(sExc.getBytes());

            int idx = sExc.indexOf("<");

            if (idx >= 0) {
                sExc = sExc.substring(0, idx);
            }

            SimpleHttpd.logger.fine(httpLogHead(true) + "\n" + sExc);
        }

        output.close();
    }

    private LinkedList<String> _losLines;

    private Socket _socket;

    private static final String S_FORMAT_TIME = "yyyy-MM-dd HH:mm";

    private static final String[] AOS_ENGLISH_USA
                                 = {S_ISO_639_ENGLISH, S_ISO_3166_USA};
    private static final String[] AOS_JAPANESE_JAPAN
                                 = {S_ISO_639_JAPANESE, S_ISO_3166_JAPAN};

    private static final long L_KB = 1024;
    private static final long L_MB = 1024 * L_KB;
    private static final long L_GB = 1024 * L_MB;

    private static final int IDX_LANG    = 0;
    private static final int IDX_COUNTRY = 1;

    private static final int SIZE_BLOCK = 4096;
}

// end of SimpleHttpServingThread.java
