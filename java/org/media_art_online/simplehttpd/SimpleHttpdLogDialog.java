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
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;

public class SimpleHttpdLogDialog extends Handler
 implements ActionListener, WindowListener {

    public SimpleHttpdLogDialog() {

        setFormatter(new SimpleHttpdLogFormatter(
         SimpleHttpd.S_CLASS_RESOURCE.substring(
         SimpleHttpd.S_CLASS_RESOURCE.lastIndexOf(".") + 1)));
        setLevel(Level.ALL);

        _dialog = new JDialog();
        _dialog.setTitle(SimpleHttpd.getString("TITLE_LOG"));
        _dialog.addWindowListener(this);
        _dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        _dialog.getContentPane().add(
         new JLabel(" " + SimpleHttpd.getString("TITLE_PORT")
         + ": " + SimpleHttpd.iPort), BorderLayout.NORTH);

        _pane = new JTextPane();
        _pane.setEditable(false);

        _paneScroll = new JScrollPane(_pane,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        _paneScroll.setPreferredSize(new Dimension(DIM_W, DIM_H));

        _doc = (DefaultStyledDocument)_pane.getDocument();

        Style style = _doc.addStyle(S_STYLE_OUTPUT,
         _doc.getStyle(S_STYLE_DEFAULT));
        StyleConstants.setForeground(style, COLOR_OUTPUT);

        _dialog.getContentPane().add(_paneScroll, BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());

        _buttonCopy = createButton("LABEL_COPY", panel);
        _buttonClear = createButton("LABEL_CLEAR", panel);
        _buttonExit = createButton("LABEL_EXIT", panel);

        _dialog.getContentPane().add(panel, BorderLayout.SOUTH);

        _dialog.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();

        if (source == _buttonExit) {

            tryExit();

        } else if (source == _buttonCopy) {

            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                 new StringSelection(_doc.getText(0, _doc.getLength())), null);

            } catch (BadLocationException unused) {
            }

        } else if (source == _buttonClear) {

            try {
                _doc.remove(0, _doc.getLength());

            } catch (BadLocationException unused) {
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    public void setVisible(boolean isVisible) {
        _dialog.setVisible(isVisible);
    }

    @Override
    synchronized public void publish(LogRecord record) {

        int iOffset = _doc.getLength();

        try {
            _doc.insertString(iOffset, getFormatter().format(record), null);

        } catch (BadLocationException unused) {
        }

        if (record.getMessage().indexOf(SimpleHttpdLogger.S_HTTP_OUTPUT)
         >= 0) {
            _doc.setCharacterAttributes(iOffset, _doc.getLength() - iOffset,
             _doc.getStyle(S_STYLE_OUTPUT), true);
        }

        _pane.setCaretPosition(_doc.getLength());
    }

    @Override
    public void windowActivated(WindowEvent unused) {
    }

    @Override
    public void windowClosed(WindowEvent unused) {
        setVisible(true);
    }

    @Override
    public void windowClosing(WindowEvent unused) {
        tryExit();
    }

    @Override
    public void windowDeactivated(WindowEvent unused) {
    }

    @Override
    public void windowDeiconified(WindowEvent unused) {
    }

    @Override
    public void windowIconified(WindowEvent unused) {
    }

    @Override
    public void windowOpened(WindowEvent unused) {
    }

//===========================================================================
// BARRIER: Anything below is not open to other files.
//===========================================================================

    private JButton createButton(String sLabel, JPanel panel) {

        JButton button = new JButton(SimpleHttpd.getString(sLabel));
        button.addActionListener(this);
        panel.add(button);

        return (button);
    }

    private void tryExit() {

        Object[] aoMessages = {SimpleHttpd.getString("MSG_EXIT")};
        Object[] aoOptions = {
            SimpleHttpd.getString("LABEL_YES"),
            SimpleHttpd.getString("LABEL_NO")
        };

        switch (JOptionPane.showOptionDialog(null, aoMessages, "",
         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
         aoOptions, aoOptions[1])) {

            case JOptionPane.OK_OPTION:
                System.exit(0);

            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
            default:
                break;
        }
    }

    private DefaultStyledDocument _doc;

    private JButton _buttonClear;
    private JButton _buttonCopy;
    private JButton _buttonExit;

    private JDialog _dialog;

    private JTextPane _pane;
    private JScrollPane _paneScroll;

    private PipedOutputStream _streamOutput;

    private static final Color COLOR_OUTPUT = Color.blue;

    private static final String S_STYLE_DEFAULT = "default";
    private static final String S_STYLE_OUTPUT  = "output";

    private static final int DIM_W = 600;
    private static final int DIM_H = 600;
}

class SimpleHttpdLogFormatter extends Formatter {

    public SimpleHttpdLogFormatter(String sNameShort) {
        _sNameShort = sNameShort;
        _format = new SimpleDateFormat(S_FORMAT_TIME);
        _lTime = 0;
    }

    @Override
    public String format(LogRecord record) {

        String sHead = (_lTime > 0
         && System.currentTimeMillis() - _lTime >= INTERVAL
         ? getTimeString() : "");

        String s = formatMessage(record);

        return (sHead + "[" + AOS_LEVELS[record.getLevel().intValue() / 100]
         + "] " + s + (s.endsWith("\n") ? "" : "\n"));
    }

    public String getHead(Handler handler) {
        return (getTimeString());
    }

    public String getTail(Handler handler) {
        return ("\n");
    }

    private String getTimeString() {
        return (_format.format(new Date(_lTime = System.currentTimeMillis()))
         + " [" + _sNameShort + "]\n");
    }

    private SimpleDateFormat _format;

    private String _sNameShort;

    private long _lTime;

    private static final String[] AOS_LEVELS = {
        "finest",  /*  0 */
        "finest",  /*  1 */
        "finest",  /*  2 */
        "finest",  /*  3 */
        "finer",   /*  4 */
        "fine",    /*  5 */
        "fine",    /*  6 */
        "config",  /*  7 */
        "info",    /*  8 */
        "warning", /*  9 */
        "severe"   /* 10 */
    };

    private static final String S_FORMAT_TIME = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final int INTERVAL = 1000;
}

// end of SimpleHttpdLogDialog.java
