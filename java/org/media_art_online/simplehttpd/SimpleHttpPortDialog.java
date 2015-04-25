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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SimpleHttpPortDialog extends JDialog implements ActionListener {

    public static final int I_PORT_REGISTERED_BEGIN =  1024;
    public static final int I_PORT_REGISTERED_END   = 49151;

    public SimpleHttpPortDialog(JFrame frame) {
        super(frame, SimpleHttpd.getString("TITLE_PORT"), true);

        ((JPanel)getContentPane()).setBorder(
         new EmptyBorder(INSET, INSET, INSET, INSET));

        ((BorderLayout)getContentPane().getLayout()).setHgap(INSET);
        ((BorderLayout)getContentPane().getLayout()).setVgap(INSET);

        getContentPane().add(new JLabel(SimpleHttpd.getString("LABEL_PORT")),
         BorderLayout.NORTH);

        getContentPane().add(new JLabel(""), BorderLayout.WEST);
        getContentPane().add(new JLabel(""), BorderLayout.EAST);

        SpinnerModel model
         = new SpinnerNumberModel(SimpleHttpd.iPort,
         I_PORT_REGISTERED_BEGIN, I_PORT_REGISTERED_END, 1);

        _spinner = new JSpinner(model);
        _spinner.setEditor(new JSpinner.NumberEditor(_spinner, "#"));
        getContentPane().add(_spinner, BorderLayout.CENTER);

        JButton button = new JButton(SimpleHttpd.getString("LABEL_START"));
        button.addActionListener(this);

        getContentPane().add(button, BorderLayout.SOUTH);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent unused) {

        SimpleHttpd.iPort = ((Integer)_spinner.getValue()).intValue();
        setVisible(false);
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private JSpinner _spinner;

    private static final int INSET = 5;
}

// end of SimpleHttpPortDialog.java
