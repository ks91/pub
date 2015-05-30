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
// none.

public class XMLParseError {

    public XMLParseError(String sKey, Object[] ao, int iLine) {
        _sKey = sKey;
        _ao = ao;
        _iLine = iLine;
    }

    @Override
    public boolean equals(Object object) {

        if (object.getClass() != XMLParseError.class) {
            return (false);
        }

        Object[] ao = ((XMLParseError)object)._ao;

        if (_ao.length != ao.length) {
            return (false);
        }

        for (int i = 0; i < _ao.length; i++) {

            if (!_ao[i].equals(ao[i])) {
                return (false);
            }
        }

        return (_iLine == ((XMLParseError)object)._iLine
         && _sKey.equals(((XMLParseError)object)._sKey));
    }

    public Object[] getAdditionals() {
        return (_ao);
    }

    public int getLineNumber() {
        return (_iLine);
    }

    public String getResourceKey() {
        return (_sKey);
    }

    private Object[] _ao;

    private String _sKey;

    private int _iLine;
}

// end of XMLParseError.java
