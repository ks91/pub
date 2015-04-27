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
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import org.media_art_online.plugin.*;
import org.media_art_online.twitter.*;
import org.media_art_online.web.*;
import org.media_art_online.xml.*;

public class SimpleHttpd implements SimpleHttpdVersion {

    public static final String S_CLASS_RESOURCE
                             = "org.media_art_online.simplehttpd.simplehttpd";

    public static final int I_PORT_DEFAULT = 8480;

    public SimpleHttpd() {

        System.out.println(S_NAME + " " + S_VERSION + "\n"
         + S_COPYRIGHT_PLAIN);

        Locale locale = Locale.getDefault();

        _bundle = ResourceBundle.getBundle(S_CLASS_RESOURCE, locale);

        _frame = new JFrame();
        _dialog = new SimpleHttpdLogDialog();

        logger = new SimpleHttpdLogger(S_CLASS_RESOURCE);
        logger.addHandler(_dialog);
        logger.setLevel(Level.ALL);

        logger.config("os: " + System.getProperty("os.name") + " ("
         + System.getProperty("os.arch") + ") "
         + System.getProperty("os.version"));
        logger.config("vm: " + System.getProperty("java.vm.name") + " ("
         + System.getProperty("java.vm.vendor") + ") "
         + System.getProperty("java.vm.version"));
        logger.config("java: " + System.getProperty("java.version"));

        logger.config("locale: " + locale.getLanguage() + "_"
         + locale.getCountry());

        logger.config("user home: " + System.getProperty("user.home"));
        logger.config("user name: " + System.getProperty("user.name")
         + "\n\n");
    }

    public static String getString(String sTag) {
        return (_bundle.getString(sTag));
    }

    public static void main(String[] unused) {
        (new SimpleHttpd()).run();
    }

    public void run() {

        (new SimpleHttpPortDialog(_frame)).setVisible(true);
        _dialog.setVisible(true);

        try {
            (new SimpleHttpServer()).run();

        } catch (Exception ex) {

            if (ex.getClass() == RuntimeException.class
             && ex.getCause().getClass() == BindException.class) {

                JOptionPane.showMessageDialog(null, getString(
                 ex.getCause().getMessage().matches(S_REGEX_PERMISSION_DENIED)
                 ? "MSG_FORBIDDEN" : "MSG_DUP") + "\n"
                 + ex.getCause().getMessage() + ".");

            } else {
                ex.printStackTrace();
            }

            System.exit(0);
        }
    }

    public static SimpleHttpdLogger logger;

    public static int iPort = I_PORT_DEFAULT;

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private JFrame _frame;

    private static ResourceBundle _bundle;

    private SimpleHttpdLogDialog _dialog;

    private static final String S_REGEX_PERMISSION_DENIED
                                                     = ".*[Pp]ermission.*";
}

// end of SimpleHttpd.java
