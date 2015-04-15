package com.foxelbox.app.util.chat;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

public class XMLUtility {
    private static final Pattern FIX_REDUNDANT_TAGS = Pattern.compile("<([a-z]+)[^>]*>(\\s*)</\\1>", Pattern.CASE_INSENSITIVE);

    public static String removeRedundantTags(CharSequence str) {
        return FIX_REDUNDANT_TAGS.matcher(str).replaceAll("$2");
    }

    public static Spanned decodeMCXML(String str, final MCHtmlTagHandler tagHandler) throws SAXException, IOException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
        final SpannableStringBuilder result = new SpannableStringBuilder();
        ContentHandler handler = new DefaultHandler() {
            public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
                tagHandler.handleTag(true, localName, result, attributes);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                tagHandler.handleTag(false, localName, result, null);
            }

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                characters(ch, start, length);
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                result.append(new String(ch, start, length));
            }

            @Override
            public void processingInstruction(String target, String data) throws SAXException {

            }

            @Override
            public void skippedEntity(String name) throws SAXException {

            }

            @Override
            public void setDocumentLocator(Locator locator) {

            }

            @Override
            public void startDocument() throws SAXException {

            }

            @Override
            public void endDocument() throws SAXException {

            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {

            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {

            }
        };
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(str)));
        return result;
    }

    public static String escape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("\"", "&quot;");
        s = s.replace("'", "&apos;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");

        return s;
    }
}
