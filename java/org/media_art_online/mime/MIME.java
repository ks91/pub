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
package org.media_art_online.mime;

//---------------------------------------------------------------------------
// Import
//---------------------------------------------------------------------------
// none.

public class MIME {

    public static final String S_TYPE_CSS    = "text/css";
    public static final String S_TYPE_HTML   = "text/html";
    public static final String S_TYPE_JS     = "application/x-javascript";
    public static final String S_TYPE_PDF    = "application/pdf";
    public static final String S_TYPE_TEXT   = "text/plain";

    public static final String S_TYPE_DEFAULT= "application/octet-stream";
    public static final String S_TYPE_FLASH  = "application/x-shockwave-flash";
    public static final String S_TYPE_KML
                                    = "application/vnd.google-earth.kml+xml";
    public static final String S_TYPE_SDP    = "application/sdp";
    public static final String S_TYPE_X_MPEG = "application/x-mpeg";

    public static final String S_TYPE_AUDIO  = "audio/";
    public static final String S_TYPE_IMAGE  = "image/";
    public static final String S_TYPE_VIDEO  = "video/";

    public static final String S_TYPE_TEXT_  = "text";
    public static final String S_TYPE_ZIP_   = "zip";

	public static String getFileType(String sFile) {

		int index = sFile.lastIndexOf(".");

		return (getType(index <= 0 ? "" : sFile.substring(index + 1)));
	}

    public static String getType(String sExtension) {

        String sType = S_TYPE_DEFAULT;

        sExtension = sExtension.toLowerCase();

        for (int min = 0, max = _aoTypes.length - 1; min <= max; ) {

            int i    = (min + max) / 2;
            int iRes = sExtension.compareTo(_aoTypes[i].sExtension);

            if (iRes == 0) {
                sType = _aoTypes[i].sType;
                break;

            } else if (iRes < 0) {
                max = i - 1;

            } else {
                min = i + 1;
            }
        }

        return (sType);
    }

	public static boolean isArchive(String sType) {
		return (sType.indexOf(S_TYPE_ZIP_) >= 0);
	}

    public static boolean isAudio(String sType) {
        return (sType.startsWith(S_TYPE_AUDIO));
    }

    public static boolean isImage(String sType) {
        return (sType.startsWith(S_TYPE_IMAGE));
    }

	public static boolean isText(String sType) {
		return (sType.indexOf(S_TYPE_TEXT_) >= 0);
	}

    public static boolean isVideo(String sType) {
        return (sType.startsWith(S_TYPE_VIDEO)
         || sType.equals(S_TYPE_FLASH) || sType.equals(S_TYPE_SDP)
         || sType.equals(S_TYPE_X_MPEG));
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private static MIMEType[] _aoTypes = {
        new MIMEType("3g2",   "video/3gpp2"),
        new MIMEType("3gp",   "video/3gpp"),
        new MIMEType("ai",    "application/postscript"),
        new MIMEType("aif",   "audio/x-aiff"),
        new MIMEType("aiff",  "audio/x-aiff"),
        new MIMEType("amc",   "application/x-mpeg"),
        new MIMEType("asc",   "text/plain"),
        new MIMEType("asp",   "application/x-asap"),
        new MIMEType("au",    "audio/basic"),
        new MIMEType("avi",   "video/avi"),
        new MIMEType("bin",   "application/octet-stream"),
        new MIMEType("bmp",   "image/bmp"),
        new MIMEType("bz2",   "application/x-bzip2"),
        new MIMEType("class", "application/octet-stream"),
        new MIMEType("css",   "text/css"),
        new MIMEType("csv",   "text/csv"),
        new MIMEType("doc",   "application/msword"),
        new MIMEType("dll",   "application/octet-stream"),
        new MIMEType("dmg",   "application/octet-stream"),
        new MIMEType("dvi",   "application/x-dvi"),
        new MIMEType("eps",   "application/postscript"),
        new MIMEType("exe",   "application/octet-stream"),
        new MIMEType("gif",   "image/gif"),
        new MIMEType("gz",    "x-gzip"),
        new MIMEType("hlp",   "application/winhelp"),
        new MIMEType("hqx",   "application/mac-binhex40"),
        new MIMEType("htm",   "text/html"),
        new MIMEType("html",  "text/html"),
        new MIMEType("ico",   "image/x-icon"),
        new MIMEType("jar",   "application/java-archive"),
        new MIMEType("jnlp",  "application/x-java-jnlp-file"),
        new MIMEType("jpeg",  "image/jpeg"),
        new MIMEType("jpg",   "image/jpeg"),
        new MIMEType("js",    "application/x-javascript"),
        new MIMEType("kml",   "application/vnd.google-earth.kml+xml"),
        new MIMEType("lzh",   "application/x-lzh"),
        new MIMEType("m4a",   "audio/mp4"),
        new MIMEType("mid",   "audio/midi"),
        new MIMEType("midi",  "audio/midi"),
        new MIMEType("mov",   "video/quicktime"),
        new MIMEType("mp3",   "audio/mpeg"),
        new MIMEType("mp4",   "video/mp4"),
        new MIMEType("mpeg",  "video/mpeg"),
        new MIMEType("mpg",   "video/mpeg"),
        new MIMEType("mpg4",  "video/mp4"),
        new MIMEType("pdf",   "application/pdf"),
        new MIMEType("pict",  "image/x-pict"),
        new MIMEType("pm",    "application/x-perl"),
        new MIMEType("png",   "image/png"),
        new MIMEType("ppt",   "application/vnd.ms-powerpoint"),
        new MIMEType("ps",    "application/postscript"),
        new MIMEType("ra",    "audio/x-realaudio"),
        new MIMEType("ram",   "audio/x-realaudio"),
        new MIMEType("rm",    "audio/x-realaudio"),
        new MIMEType("rpm",   "audio/x-pn-RealAudio-plugin"),
        new MIMEType("rtf",   "application/rtf"),
        new MIMEType("sdp",   "application/sdp"),
        new MIMEType("sea",   "application/x-stuffit"),
        new MIMEType("sh",    "application/x-sh"),
        new MIMEType("sit",   "application/x-stuffit"),
        new MIMEType("snd",   "audio/basic"),
        new MIMEType("swf",   "application/x-shockwave-flash"),
        new MIMEType("tar",   "application/x-tar"),
        new MIMEType("tex",   "application/x-tex"),
        new MIMEType("tgz",   "application/x-tar"),
        new MIMEType("tif",   "image/tiff"),
        new MIMEType("tiff",  "image/tiff"),
        new MIMEType("txt",   "text/plain"),
        new MIMEType("wav",   "audio/x-wav"),
        new MIMEType("wmf",   "application/x-msmetafile"),
        new MIMEType("wmv",   "video/x-ms-wmv"),
        new MIMEType("xhtml", "application/xhtml+xml"),
        new MIMEType("xls",   "application/vnd.ms-excel"),
        new MIMEType("xml",    "text/xml"),
        new MIMEType("zip",   "application/zip"),
    };
}

class MIMEType {

    public MIMEType(String sExtension, String sType) {
        this.sExtension = sExtension;
        this.sType      = sType;
    }

    public String sExtension;
    public String sType;
}

// end of MIME.java
