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
import java.util.logging.*;

public class SimpleHttpdLogger extends Logger {

    public static final String S_HTTP_INPUT  = "<-:";
    public static final String S_HTTP_OUTPUT = "->:";

    public SimpleHttpdLogger(String sName) {

        super(sName, null);

        _sName = sName;

        LogManager.getLogManager().addLogger(this);

        setUseParentHandlers(false);
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private StreamHandler _handler;

    private String _sName;
}

// end of SimpleHttpdLogger.java
