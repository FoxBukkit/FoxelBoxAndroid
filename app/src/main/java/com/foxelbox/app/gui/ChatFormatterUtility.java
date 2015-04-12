package com.foxelbox.app.gui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.text.*;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.foxelbox.app.R;
import com.foxelbox.app.util.WebUtility;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatterUtility {
    private static final char COLOR_CHAR = '\u00a7';
    private static final Pattern REMOVE_COLOR_CHAR = Pattern.compile("\u00a7.");

    final static boolean[] isColorCode;
    final static int[] colorCodeSpans;
    final static Map<String, Integer> colorNameSpans;
    final static Map<String, OnClickSpanFactory> onClickSpans;

    interface OnClickSpanFactory {
        ClickableSpan newSpan(String parameter);
    }

    static {
        isColorCode = new boolean[256];
        colorCodeSpans = new int[256];
        colorNameSpans = new HashMap<>();
        onClickSpans = new HashMap<>();

        isColorCode['0'] = true;
        isColorCode['1'] = true;
        isColorCode['2'] = true;
        isColorCode['3'] = true;
        isColorCode['4'] = true;
        isColorCode['5'] = true;
        isColorCode['6'] = true;
        isColorCode['7'] = true;
        isColorCode['8'] = true;
        isColorCode['9'] = true;

        isColorCode['a'] = true;
        isColorCode['b'] = true;
        isColorCode['c'] = true;
        isColorCode['d'] = true;
        isColorCode['e'] = true;
        isColorCode['f'] = true;

        colorCodeSpans['0'] = Color.parseColor("#000000");
        colorCodeSpans['1'] = Color.parseColor("#0000BE");
        colorCodeSpans['2'] = Color.parseColor("#00BE00");
        colorCodeSpans['3'] = Color.parseColor("#00BEBE");
        colorCodeSpans['4'] = Color.parseColor("#BE0000");
        colorCodeSpans['5'] = Color.parseColor("#BE00BE");
        colorCodeSpans['6'] = Color.parseColor("#D9A334");
        colorCodeSpans['7'] = Color.parseColor("#BEBEBE");
        colorCodeSpans['8'] = Color.parseColor("#3F3F3F");
        colorCodeSpans['9'] = Color.parseColor("#3F3FFE");

        colorCodeSpans['a'] = Color.parseColor("#3FFE3F");
        colorCodeSpans['b'] = Color.parseColor("#3FFEFE");
        colorCodeSpans['c'] = Color.parseColor("#FE3F3F");
        colorCodeSpans['d'] = Color.parseColor("#FE3FFE");
        colorCodeSpans['e'] = Color.parseColor("#FEFE3F");
        colorCodeSpans['f'] = Color.parseColor("#FFFFFF");

        colorNameSpans.put("black", colorCodeSpans['0']);
        colorNameSpans.put("dark_blue", colorCodeSpans['1']);
        colorNameSpans.put("dark_green", colorCodeSpans['2']);
        colorNameSpans.put("dark_aqua", colorCodeSpans['3']);
        colorNameSpans.put("dark_red", colorCodeSpans['4']);
        colorNameSpans.put("dark_purple", colorCodeSpans['5']);
        colorNameSpans.put("gold", colorCodeSpans['6']);
        colorNameSpans.put("gray", colorCodeSpans['7']);
        colorNameSpans.put("dark_gray", colorCodeSpans['8']);
        colorNameSpans.put("blue", colorCodeSpans['9']);
        colorNameSpans.put("green", colorCodeSpans['a']);
        colorNameSpans.put("aqua", colorCodeSpans['b']);
        colorNameSpans.put("red", colorCodeSpans['c']);
        colorNameSpans.put("light_purple", colorCodeSpans['d']);
        colorNameSpans.put("yellow", colorCodeSpans['e']);
        colorNameSpans.put("white", colorCodeSpans['f']);

        onClickSpans.put("suggest_command", new OnClickSpanFactory() {
            @Override
            public ClickableSpan newSpan(final String parameter) {
                return new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        ActionBarActivity activity = (ActionBarActivity)widget.getContext();
                        EditText chatBar = (EditText)activity.findViewById(R.id.textChatMessage);
                        chatBar.setText(parameter);
                        chatBar.setSelection(parameter.length());
                        if(chatBar.requestFocus()) {
                            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
            }
        });

        onClickSpans.put("run_command", new OnClickSpanFactory() {
            @Override
            public ClickableSpan newSpan(final String parameter) {
                return new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        WebUtility.sendChatMessage((ActionBarActivity)widget.getContext(), widget, parameter);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
            }
        });
    }

    private static class MCHtmlTagHandler implements Html.TagHandler {
        private static Field elementField = null, attsField = null, dataField = null, lengthField = null;

        private String exfiltrateAttribute(XMLReader reader, String attribute) {
            try {
                if(elementField == null) {
                    elementField = reader.getClass().getDeclaredField("theNewElement");
                    elementField.setAccessible(true);
                }
                final Object element = elementField.get(reader);
                if(attsField == null) {
                    attsField = element.getClass().getDeclaredField("theAtts");
                    attsField.setAccessible(true);
                }
                final Object atts = attsField.get(element);
                if(dataField == null) {
                    dataField = atts.getClass().getDeclaredField("data");
                    dataField.setAccessible(true);
                }
                final String[] data = (String[]) dataField.get(atts);
                if(lengthField == null) {
                    lengthField = atts.getClass().getDeclaredField("length");
                    lengthField.setAccessible(true);
                }
                final int len = (Integer) lengthField.get(atts);

                for (int i = 0; i < len; i++) {
                    if (attribute.equals(data[i * 5 + 1])) {
                        return data[i * 5 + 4];
                    }
                }
            } catch (Exception e) {
                Log.w("foxelbox_exfxml", "ExfXMLError", e);
            }

            return null;
        }

        private class ColorMarker {
            public final int color;
            ColorMarker(int color) {
                this.color = color;
            }
        }

        private class SpanMarker {
            public final String onClick;
            SpanMarker(String onClick) {
                this.onClick = onClick;
            }
        }

        private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([^(]+)\\('(.*)'\\)$");

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            final int len = output.length();
            if(tag.equalsIgnoreCase("color")) {
                if(opening) {
                    String colorName = exfiltrateAttribute(xmlReader, "name");
                    if(!colorNameSpans.containsKey(colorName)) {
                        return;
                    }
                    int color = colorNameSpans.get(colorName);

                    output.setSpan(new ColorMarker(color), len, len, Spannable.SPAN_MARK_MARK);
                } else {
                    ColorMarker obj = getLast(output, ColorMarker.class);
                    if(obj == null) {
                        return;
                    }

                    int color = obj.color;
                    int where = output.getSpanStart(obj);
                    output.removeSpan(obj);

                    output.setSpan(new ForegroundColorSpan(color), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if(tag.equalsIgnoreCase("span")) {
                if(opening) {
                    String onClick = exfiltrateAttribute(xmlReader, "onclick");
                    output.setSpan(new SpanMarker(onClick), len, len, Spannable.SPAN_MARK_MARK);
                } else {
                    SpanMarker obj = getLast(output, SpanMarker.class);
                    if(obj == null) {
                        return;
                    }
                    int where = output.getSpanStart(obj);
                    output.removeSpan(obj);
                    String onClick = obj.onClick;
                    if(onClick == null) {
                        return;
                    }

                    final Matcher matcher = FUNCTION_PATTERN.matcher(onClick);
                    if (!matcher.matches()) {
                        Log.w("foxelbox_xml", "Unknown chat function pattern");
                        return;
                    }

                    final String eventType = matcher.group(1).toLowerCase();
                    final String eventString = matcher.group(2);

                    if(!onClickSpans.containsKey(eventType)) {
                        Log.w("foxelbox_xml", "Unknown chat function: " + eventType);
                        return;
                    }

                    OnClickSpanFactory factory = onClickSpans.get(eventType);
                    output.setSpan(factory.newSpan(eventString), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        private <T> T getLast(Editable text, Class<T> kind) {
            T[] objs = text.getSpans(0, text.length(), kind);
            if(objs.length == 0) {
                return null;
            } else {
                return objs[objs.length-1];
            }
        }
    }

    private static <T> void copySpans(Spanned source, Spannable dest, Class<T> type) {
        for(T span : source.getSpans(0, source.length(), type)) {
            dest.setSpan(span, source.getSpanStart(span), source.getSpanEnd(span), source.getSpanFlags(span));
        }
    }

    public static Spannable formatString(String string, boolean parseXml) {
        String noColorCode = REMOVE_COLOR_CHAR.matcher(string).replaceAll("");

        Spannable stringBuilder;
        if(parseXml) {
            string = Html.fromHtml(string).toString();
            Spanned xmlParsed = Html.fromHtml("<root>" + noColorCode + "</root>", null, new MCHtmlTagHandler());
            stringBuilder = new SpannableString(xmlParsed);
            noColorCode = xmlParsed.toString();

            copySpans(xmlParsed, stringBuilder, ForegroundColorSpan.class);
            copySpans(xmlParsed, stringBuilder, ClickableSpan.class);
        } else {
            stringBuilder = new SpannableString(noColorCode);
        }

        string = string.toLowerCase();

        int offset = 0, lastPos, pos = -1;
        char currentColor = 'f', newCode = 'f';
        while(true) {
            lastPos = pos;
            pos = string.indexOf(COLOR_CHAR, pos + 1);
            if(pos >= 0) {
                newCode = string.charAt(pos + 1);
            }
            if(lastPos >= 0) {
                int startPos = lastPos - (offset - 2);
                int endPos = pos - offset;
                if(pos < 0) {
                    endPos = noColorCode.length();
                }
                if(endPos != startPos) {
                    endPos = stringBuilder.nextSpanTransition(startPos, endPos, ForegroundColorSpan.class);
                    stringBuilder.setSpan(new ForegroundColorSpan(colorCodeSpans[currentColor]), startPos, endPos, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }

            if(pos < 0) {
                break;
            }

            offset += 2;

            if(isColorCode[newCode]) {
                currentColor = newCode;
            } else {
                switch(newCode) {
                    case 'l':
                        //bold = true;
                        //break;
                    case 'm':
                        //strikethrough = true;
                        //break;
                    case 'n':
                        //underline = true;
                        //break;
                    case 'o':
                        //italic = true;
                        //break;
                    case 'r':
                        //bold = false;
                        //underline = false;
                        //strikethrough = false;
                        //italic = false;
                        break;
                    default:
                        Log.w("foxelbox_decoder", "Invalid color code: " + newCode);
                        break;
                }
            }
        }

        return stringBuilder;
    }
}
