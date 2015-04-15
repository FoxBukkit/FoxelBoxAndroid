package com.foxelbox.app.util.chat;

import java.util.*;
import java.util.regex.Pattern;

public class LegacyConverter {
    private static final char COLOR_CHAR = '\u00a7';


    private static final Map<Character, String> colorNames;

    static {
        colorNames = new HashMap<>();

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
    }

    public static String convert(String in) {
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

        return XMLUtility.removeRedundantTags(out.toString());
    }
}
