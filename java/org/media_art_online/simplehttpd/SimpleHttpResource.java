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
import org.media_art_online.mime.*;

public class SimpleHttpResource {

    public SimpleHttpResource(File file) throws IOException {
        this(file.toURI().toURL());
    }

    public SimpleHttpResource(String sName, String s, String sEncoding)
     throws UnsupportedEncodingException {
        this(sName, s, sEncoding, MIME.S_TYPE_HTML);
    }

    public SimpleHttpResource(String sName, String s, String sEncoding,
     String sType) throws UnsupportedEncodingException {

        byte[] aob = s.getBytes(sEncoding);

        _input = new ByteArrayInputStream(aob);
        _length = aob.length;
        _sName = sName;
        _sType = sType;
    }

    public SimpleHttpResource(URL url) throws IOException {

        if (url == null) {
            throw (new SimpleHttpNotFoundException());
        }

        URLConnection connection = url.openConnection();

        _input = connection.getInputStream();
        _length = connection.getContentLength();
        _sName = url.getFile();

        int index = _sName.lastIndexOf(".");

        _sType = MIME.getType(index <= 0 ? "" : _sName.substring(index + 1));
    }

    public long getContentLength() {
        return (_length);
    }

    public InputStream getInputStream() {
        return (_input);
    }

    public String getName() {
        return (_sName);
    }

    public String getType() {
        return (_sType);
    }

    public void setType(String sType) {
        _sType = sType;
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private InputStream _input;

    private String _sName;
    private String _sType;

    private long _length;
}

// end of SimpleHttpResource.java
