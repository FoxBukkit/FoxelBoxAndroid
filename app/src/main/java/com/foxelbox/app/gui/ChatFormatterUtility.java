package com.foxelbox.app.gui;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.text.*;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.foxelbox.app.R;
import com.foxelbox.app.util.WebUtility;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatterUtility {
    private static final char COLOR_CHAR = '\u00a7';
    private static final Pattern FIX_REDUNDANT_TAGS = Pattern.compile("<([a-z]+)[^>]*>(\\s*)</\\1>", Pattern.CASE_INSENSITIVE);
    private static final Pattern REMOVE_COLOR_CHAR = Pattern.compile(COLOR_CHAR + ".");

    private static final Map<String, Integer> colorNameSpans;
    private static final Map<String, OnClickSpanFactory> onClickSpans;
    private static final Map<Character, String> colorNames = new HashMap<>();

    interface OnClickSpanFactory {
        ClickableSpan newSpan(String parameter);
    }

    static {
        colorNameSpans = new HashMap<>();
        onClickSpans = new HashMap<>();

        colorNames.put('0', "black");
        colorNames.put('1', "dark_blue");
        colorNames.put('2', "dark_green");
        colorNames.put('3', "dark_aqua");
        colorNames.put('4', "dark_red");
        colorNames.put('5', "dark_purple");
        colorNames.put('6', "gold");
        colorNames.put('7', "gray");
        colorNames.put('8', "dark_gray");
        colorNames.put('9', "blue");
        colorNames.put('a', "green");
        colorNames.put('b', "aqua");
        colorNames.put('c', "red");
        colorNames.put('d', "light_purple");
        colorNames.put('e', "yellow");
        colorNames.put('f', "white");

        colorNameSpans.put("black", Color.parseColor("#000000"));
        colorNameSpans.put("dark_blue", Color.parseColor("#0000BE"));
        colorNameSpans.put("dark_green", Color.parseColor("#00BE00"));
        colorNameSpans.put("dark_aqua", Color.parseColor("#00BEBE"));
        colorNameSpans.put("dark_red", Color.parseColor("#BE0000"));
        colorNameSpans.put("dark_purple", Color.parseColor("#BE00BE"));
        colorNameSpans.put("gold", Color.parseColor("#D9A334"));
        colorNameSpans.put("gray", Color.parseColor("#BEBEBE"));
        colorNameSpans.put("dark_gray", Color.parseColor("#3F3F3F"));
        colorNameSpans.put("blue", Color.parseColor("#3F3FFE"));
        colorNameSpans.put("green", Color.parseColor("#3FFE3F"));
        colorNameSpans.put("aqua", Color.parseColor("#3FFEFE"));
        colorNameSpans.put("red", Color.parseColor("#FE3F3F"));
        colorNameSpans.put("light_purple", Color.parseColor("#FE3FFE"));
        colorNameSpans.put("yellow", Color.parseColor("#FEFE3F"));
        colorNameSpans.put("white", Color.parseColor("#FFFFFF"));

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
                        Log.w("foxelbox_xml", "Unknown chat function pattern:" + onClick);
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

    public static String convertLegacyColors(String in) {
        StringBuilder out = new StringBuilder("<color name=\"white\">");

        int lastPos = 0; char currentColor = 'f';

        Set<String> openTagsSet = new HashSet<>();
        Stack<String> openTags = new Stack<>();
        openTagsSet.add("color");
        openTags.push("color");

        while(true) {
            int pos = in.indexOf(COLOR_CHAR, lastPos);
            if(pos < 0) {
                if(lastPos == 0) {
                    return in;
                }
                break;
            }
            char newColor = in.charAt(pos + 1);

            if(pos > 0) {
                out.append(in.substring(lastPos, pos));
            }

            lastPos = pos + 2;

            if((newColor >= '0' && newColor <= '9') || (newColor >= 'a' && newColor <= 'f') || newColor == 'r') {

                boolean doesNotChangeColor = newColor == 'r' || currentColor == newColor;

                while(!openTags.empty()) {
                    String tag = openTags.pop();
                    if(doesNotChangeColor && tag.equals("color")) {
                        continue;
                    }
                    out.append("</");
                    out.append(tag);
                    out.append('>');
                }
                openTagsSet.clear();

                openTagsSet.add("color");
                openTags.push("color");

                if(doesNotChangeColor) {
                    continue;
                }

                out.append("<color name=\"");
                out.append(colorNames.get(newColor));
                out.append("\">");

                currentColor = newColor;
            } else {
                switch (newColor) {
                    case 'l':
                        if(!openTagsSet.contains("b")) {
                            openTags.push("b");
                            openTagsSet.add("b");
                            out.append("<b>");
                        }
                        break;
                    case 'm':
                        if(!openTagsSet.contains("s")) {
                            openTags.push("s");
                            openTagsSet.add("s");
                            out.append("<s>");
                        }
                        break;
                    case 'n':
                        if(!openTagsSet.contains("u")) {
                            openTags.push("u");
                            openTagsSet.add("u");
                            out.append("<u>");
                        }
                        break;
                    case 'o':
                        if(!openTagsSet.contains("i")) {
                            openTags.push("i");
                            openTagsSet.add("i");
                            out.append("<i>");
                        }
                        break;
                }
            }
        }

        if(lastPos < in.length()) {
            out.append(in.substring(lastPos));
        }
        while(!openTags.empty()) {
            String tag = openTags.pop();
            out.append("</");
            out.append(tag);
            out.append('>');
        }

        return FIX_REDUNDANT_TAGS.matcher(out.toString()).replaceAll("$2");
    }

    public static String XMLEscape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("\"", "&quot;");
        s = s.replace("'", "&apos;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");

        return s;
    }

    public static Spanned formatString(String string, boolean parseXml) {
        String noColorCode = REMOVE_COLOR_CHAR.matcher(string).replaceAll("");

        if(!parseXml) {
            noColorCode = convertLegacyColors(XMLEscape(string));
        }

        Log.d("fbdd", noColorCode);

        return Html.fromHtml("<root>" + noColorCode + "</root>", null, new MCHtmlTagHandler());
    }
}
