package com.foxelbox.app.util.chat;

import android.text.Spanned;
import org.xml.sax.SAXException;

import java.io.IOException;

public class ChatFormatterUtility {
    public static Spanned formatString(String string, boolean parseXml) {
        if(!parseXml) {
            string = LegacyConverter.convert(XMLUtility.escape(string));
        }

        try {
            return XMLUtility.decodeMCXML("<root>" + string + "</root>", MCHtmlTagHandler.instance);
        } catch (SAXException|IOException e) {
            throw new RuntimeException(e);
        }
    }
}
