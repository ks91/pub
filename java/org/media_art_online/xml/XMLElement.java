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
import java.util.Iterator;
import java.util.LinkedList;

public class XMLElement extends LinkedList<Object> {

    public XMLElement(String sTag) {
        _sTag = sTag;
        _loAttributes = new LinkedList<XMLAttribute>();
    }

    public XMLElement(String sTag, String[][] aoAttributes) {
        this(sTag);

        if (aoAttributes != null) {
            for (int i = 0; i < aoAttributes.length; i++) {
                XMLAttribute attr
                 = new XMLAttribute(aoAttributes[i][0], aoAttributes[i][1]);

                addAttribute(attr);
            }
        }
    }

    public XMLElement(String sTag, String[][] aoAttributes, Object[] aoObj) {
        this(sTag, aoAttributes);

        if (aoObj != null) {
            for (int i = 0; i < aoObj.length; i++) {
                if (aoObj[i] == null) {

                } else if (aoObj[i].getClass() == XMLElement.class) {
                    addln(aoObj[i]);

                } else {
                    add(aoObj[i]);
                }
            }
        }
    }

    public XMLElement(XMLElement element) {
        this(element.getTag(), element.getAttributes(), element.getElements());
    }

    public void addAttribute(XMLAttribute attribute) {
        int i;

        for (i = 0; i < getAttributeSize(); i++) {
            if (getAttribute(i).getName().compareTo(attribute.getName()) > 0) {
                break;
            }
        }

        _loAttributes.add(i, attribute);
    }

    public void addln(Object object) {
        if (size() == 0) {
            add("\n");
        }

        add(object);
        add("\n");
    }

    public XMLElement findByTag(String sTag) {

        XMLElement element;
        Object obj;

        for (int i = 0; i < size(); i++) {
            if ((obj = get(i)).getClass() == XMLElement.class) {
                if (((XMLElement)obj).getTag().equals(sTag)) {
                    return ((XMLElement)obj);

                } else if ((element = ((XMLElement)obj).findByTag(sTag))
                 != null) {
                    return (element);
                }
            }
        }

        return (null);
    }

    public XMLAttribute getAttribute(int index) {
        return (_loAttributes.get(index));
    }

    public XMLAttribute getAttribute(String sName) {

        for (int i = 0; i < _loAttributes.size(); i++) {
            XMLAttribute attr = _loAttributes.get(i);

            if (attr.getName().equals(sName)) {
                return (attr);
            }
        }

        return (null);
    }

    public String[][] getAttributes() {

        String[][] aoAttributes = new String[getAttributeSize()][];

        for (int i = 0; i < getAttributeSize(); i++) {

            XMLAttribute attr = _loAttributes.get(i);

            aoAttributes[i] = new String[] {attr.getName(), attr.getValue()};
        }

        return (aoAttributes);
    }

    public int getAttributeSize() {
        return (_loAttributes.size());
    }

    public String getAttributeValue(String sName) {

        XMLAttribute attr = getAttribute(sName);

        return (attr == null ? "" : attr.getValue());
    }

    public String getDefaultNameSpace() {

        return (getAttributeValue(XML.S_XMLNS));
    }

    public XMLElement getElement(String sTag) {

        Object obj;

        for (int i = 0; i < size(); i++) {
            if ((obj = get(i)).getClass() == XMLElement.class) {
                if (((XMLElement)obj).getTag().equals(sTag)) {
                    return ((XMLElement)obj);
                }
            }
        }

        return (null);
    }

    public Object[] getElements() {

        Object[] aoElements = new Object[getMarkupCount()];
        int i = 0;

        for (XMLIterator iterator = new XMLIterator(this);
         iterator.hasNextMarkup(); i++) {
            aoElements[i] = iterator.nextMarkup();
        }

        return (aoElements);
    }

    public XMLAttribute getFirstAttribute(String sName) {

        XMLAttribute attr = getAttribute(sName);

        if (attr != null) {
            return (attr);
        }

        XMLElement element;
        Object obj;

        for (Iterator iterator = iterator(); iterator.hasNext(); ) {

            if ((obj = iterator.next()).getClass() == XMLElement.class) {

                if ((attr = ((XMLElement)obj).getFirstAttribute(sName))
                 != null) {
                    return (attr);
                }
            }
        }

        return (null);
    }

    public XMLElement getFirstMarkup() {

        Object obj = null;

        for (Iterator iterator = iterator(); iterator.hasNext(); ) {

            if ((obj = iterator.next()).getClass() == XMLElement.class) {
                return ((XMLElement)obj);
            }
        }

        return (null);
    }

    public int getMarkupCount() {

        int count = 0;

        for (Iterator iterator = iterator(); iterator.hasNext(); ) {

            Object object = iterator.next();

            if (object.getClass() == XMLElement.class) {
                count++;
            }
        }

        return (count);
    }

    public String getTag() {
        return (_sTag);
    }

    public String getTaggedString(String sTag) {
        XMLElement element;

        return ((element = getElement(sTag)) == null || element.size() == 0
         ? "" : (String)element.get(0));
    }

    public boolean hasTag(String sTag) {

        boolean isFound = false;
        Object obj;

        for (int i = 0; i < size(); i++) {
            if ((obj = get(i)).getClass() == XMLElement.class) {
                if (((XMLElement)obj).getTag().equals(sTag)
                 || ((XMLElement)obj).hasTag(sTag)) {
                    isFound = true;
                    break;
                }
            }
        }

        return (isFound);
    }

    public void removeAttribute(String sName) {
        for (int i = 0; i < _loAttributes.size(); i++) {
            XMLAttribute attr = _loAttributes.get(i);

            if (attr.getName().equals(sName)) {
                _loAttributes.remove(i);
            }
        }
    }

    public XMLElement removeMarkupln(String sName) {

        Object obj;

        for (int i = 0; i < size(); i++) {
            if ((obj = get(i)).getClass() == XMLElement.class
             && ((XMLElement)obj).getTag().equals(sName)) {
                remove(i);

                if ((obj = get(i + 1)).getClass() == String.class
                 && ((String)obj).equals("\n")) {
                    remove(i + 1);
                }
            } 
        }

        return (this);
    }

    public void setTag(String sTag) {
        _sTag = sTag;
    }

    public String toString() {

        int tag = (_sTag.equals(XML.S_XML) ? CASE_TAG_XML
         : (_sTag.equals(XML.S_STREAM_STREAM)
         ? CASE_TAG_STREAM_STREAM : CASE_TAG_GENERAL));

        String s = XML.S_OPENER;

        if (tag == CASE_TAG_XML) {
            s += XML.S_XQ;

        } else if (tag == CASE_TAG_STREAM_STREAM
         && _loAttributes.size() == 0) {
            s += XML.S_CLOSE_PREFIX;
        }

        s += _sTag;

        for (Iterator<XMLAttribute> iterator = _loAttributes.iterator();
         iterator.hasNext(); ) {

            XMLAttribute attr = iterator.next();

            s += " " + attr.getName() + XML.S_ASSIGN
             + XML.encodeAttribute(attr.getValue());
        }

        if (XML.isCanonical || size() > 0 || tag != CASE_TAG_GENERAL) {
            s += (tag == CASE_TAG_XML ? XML.S_XQ : "") + XML.S_CLOSER;
        }

        for (Iterator iterator = iterator(); iterator.hasNext(); ) {

            Object object = iterator.next();

            if (object.getClass() == XMLElement.class
             || object.getClass() == XMLEntity.class) {
                s += object.toString();

            } else {
                s += XML.encodeText((String)object);
            }
        }

        if (tag == CASE_TAG_GENERAL) {
            s += (XML.isCanonical || size() > 0
             ? (XML.S_OPENER + XML.S_CLOSE_PREFIX + _sTag + XML.S_CLOSER)
             : (XML.S_CLOSE_PREFIX + XML.S_CLOSER));
        }

        return (s);
    }

    private LinkedList<XMLAttribute> _loAttributes;

    private String _sTag;

    private static final int CASE_TAG_GENERAL       = 0;
    private static final int CASE_TAG_XML           = 1;
    private static final int CASE_TAG_STREAM_STREAM = 2;
}

// end of XMLElement.java
