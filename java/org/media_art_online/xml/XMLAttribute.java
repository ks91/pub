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

public class XMLAttribute {

    public XMLAttribute(final String sName, final String sValue) {
        _sName = sName;
        _sValue = sValue;
    }

    public String getName() {
        return (_sName);
    }

    public String getValue() {
        return (_sValue);
    }

    public void setValue(String sValue) {
        _sValue = sValue;
    }

    private String _sName;
    private String _sValue;
}

// end of XMLAttribute.java
