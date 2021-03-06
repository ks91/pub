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
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class SimpleHttpdLogDialog extends Handler
 implements ActionListener, MouseListener, MouseMotionListener,
 PopupMenuListener, WindowListener {

    public SimpleHttpdLogDialog(JFrame frame) {

        setFormatter(new SimpleHttpdLogFormatter(
         SimpleHttpd.S_CLASS_RESOURCE.substring(
         SimpleHttpd.S_CLASS_RESOURCE.lastIndexOf(".") + 1)));
        setLevel(Level.ALL);

        _menuPopup = new JPopupMenu(SimpleHttpd.getString("TITLE_MENU"));
        _menuPopup.addPopupMenuListener(this);

        _itemCopy = new JMenuItem(SimpleHttpd.getString("MENU_COPY"));
        _itemCopy.addActionListener(this);
        _menuPopup.add(_itemCopy);

        _itemAll = new JMenuItem(SimpleHttpd.getString("MENU_ALL"));
        _itemAll.addActionListener(this);
        _menuPopup.add(_itemAll);

        _dialog = frame;
        _dialog.setTitle(SimpleHttpdVersion.S_NAME
         + " " + SimpleHttpd.getString("TITLE_LOG"));
        _dialog.addWindowListener(this);
        _dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        _dialog.getContentPane().add(
         _labelPort = new JLabel(" " + SimpleHttpd.getString("TITLE_PORT")
         + ": " + SimpleHttpd.iPort), BorderLayout.NORTH);

        _pane = new JTextPane();
        _pane.setEditable(false);
        _pane.addMouseListener(this);
        _pane.addMouseMotionListener(this);

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
        _checkXML = createCheckBox("LABEL_XML_CHECK", panel);

        _dialog.getContentPane().add(panel, BorderLayout.SOUTH);

        _dialog.pack();

        try {
            _doc.insertString(_doc.getLength(),
             getFormatter().getHead(this), null);

        } catch (BadLocationException unused) {
        }
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

        } else if (source == _itemCopy) {

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
             new StringSelection(_pane.getSelectedText()), null);

        } else if (source == _itemAll) {
            _pane.selectAll();

        } else if (source == _checkXML) {
            SimpleHttpd.setCheckingXML(_checkXML.isSelected());
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void mouseClicked(MouseEvent unused) {
    }

    @Override
    public void mouseDragged(MouseEvent unused) {
    }

    @Override
    public void mouseEntered(MouseEvent unused) {
        _pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent unused) {
        _pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        _pane.setToolTipText("");
    }

    @Override
    public void mouseMoved(MouseEvent event) {

        int index1 = _pane.viewToModel(event.getPoint());

        try {

            if (_pane.getText(index1 - 3, 6).indexOf("%") < 0) {
                _pane.setToolTipText("");
                return;
            }

            char c = '\0';

            while ((c = _pane.getText(index1, 1).charAt(0)) != '\n') {
                index1--;
            }

            int index2 = ++index1;

            while ((c = _pane.getText(++index2, 1).charAt(0))
             != '\n') {
            }

            _pane.setToolTipText(URLDecoder.decode(
             _pane.getText(index1, index2 - index1), "UTF-8"));

        } catch (BadLocationException unused) {

        /* perhaps trailing % of an IPv6 address */
        } catch (IllegalArgumentException unused) {

        } catch (UnsupportedEncodingException unused) {
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {

        if (event.getClickCount() == 1) {

            if (event.isPopupTrigger()
             || SwingUtilities.isRightMouseButton(event)) {
                _menuPopup.show(event.getComponent(), event.getX(),
                 event.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent unused) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent unused) {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent unused) {
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent unused) {
        _itemCopy.setEnabled(_pane.getSelectedText() != null);
    }

    public void setVisible(boolean isVisible) {

        if (isVisible) {

            _labelPort.setText(" " + SimpleHttpd.getString("TITLE_PORT")
             + ": " + SimpleHttpd.iPort);
        }

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

    private JCheckBox createCheckBox(String sLabel, JPanel panel) {

        JCheckBox check = new JCheckBox(SimpleHttpd.getString(sLabel));
        check.addActionListener(this);
        panel.add(check);

        return (check);
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

    private JCheckBox _checkXML;

    private JFrame _dialog;

    private JLabel _labelPort;

    private JMenuItem _itemAll;
    private JMenuItem _itemCopy;

    private JPopupMenu _menuPopup;

    private JTextPane _pane;
    private JScrollPane _paneScroll;

    private PipedOutputStream _streamOutput;

    private static final Color COLOR_OUTPUT = Color.blue;

    private static final String S_STYLE_DEFAULT = "default";
    private static final String S_STYLE_OUTPUT  = "output";

    private static final int DIM_W = 750;
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

    @Override
    public String getHead(Handler handler) {
        return (getTimeString());
    }

    @Override
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
