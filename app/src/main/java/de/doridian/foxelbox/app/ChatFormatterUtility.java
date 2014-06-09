package de.doridian.foxelbox.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.regex.Pattern;

public class ChatFormatterUtility {
    private static final char COLOR_CHAR = '\u00a7';
    private static final Pattern REMOVE_COLOR_CHAR = Pattern.compile("\u00a7.");

    final static boolean[] isColorCode;
    final static ForegroundColorSpan[] colorCodeSpans;
    static {
        isColorCode = new boolean[256];
        colorCodeSpans = new ForegroundColorSpan[256];

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

        colorCodeSpans['0'] = new ForegroundColorSpan(Color.parseColor("#000000"));
        colorCodeSpans['1'] = new ForegroundColorSpan(Color.parseColor("#0000BE"));
        colorCodeSpans['2'] = new ForegroundColorSpan(Color.parseColor("#00BE00"));
        colorCodeSpans['3'] = new ForegroundColorSpan(Color.parseColor("#00BEBE"));
        colorCodeSpans['4'] = new ForegroundColorSpan(Color.parseColor("#BE0000"));
        colorCodeSpans['5'] = new ForegroundColorSpan(Color.parseColor("#BE00BE"));
        colorCodeSpans['6'] = new ForegroundColorSpan(Color.parseColor("#D9A334"));
        colorCodeSpans['7'] = new ForegroundColorSpan(Color.parseColor("#BEBEBE"));
        colorCodeSpans['8'] = new ForegroundColorSpan(Color.parseColor("#3F3F3F"));
        colorCodeSpans['9'] = new ForegroundColorSpan(Color.parseColor("#3F3FFE"));

        colorCodeSpans['a'] = new ForegroundColorSpan(Color.parseColor("#3FFE3F"));
        colorCodeSpans['b'] = new ForegroundColorSpan(Color.parseColor("#3FFEFE"));
        colorCodeSpans['c'] = new ForegroundColorSpan(Color.parseColor("#FE3F3F"));
        colorCodeSpans['d'] = new ForegroundColorSpan(Color.parseColor("#FE3FFE"));
        colorCodeSpans['e'] = new ForegroundColorSpan(Color.parseColor("#FEFE3F"));
        colorCodeSpans['f'] = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
    }

    public static Spannable formatString(String string) {
        String noColorCode = REMOVE_COLOR_CHAR.matcher(string).replaceAll("");
        string = string.toLowerCase();
        Spannable stringBuilder = new SpannableString(noColorCode);
        int offset = 0, lastPos, pos = -1;
        char currentColor = 'f', newCode = 'f'; boolean bold = false, underline = false, strikethrough = false, italic = false;
        while(true) {
            lastPos = pos;
            pos = string.indexOf(COLOR_CHAR, pos + 1);
            if(pos >= 0)
                newCode = string.charAt(pos + 1);

            if(lastPos >= 0) {
                int startPos = lastPos - (offset - 2);
                int endPos = pos - offset;
                if(pos < 0)
                    endPos = noColorCode.length() - 1;
                if(endPos != startPos) {
                    stringBuilder.setSpan(colorCodeSpans[currentColor], startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    if (bold && italic)
                        stringBuilder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    else if (bold)
                        stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    else if (italic)
                        stringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            if(pos < 0)
                break;

            offset += 2;

            if(isColorCode[newCode]) {
                currentColor = newCode;
                bold = false;
                underline = false;
                strikethrough = false;
                italic = false;
            } else {
                switch(newCode) {
                    case 'l':
                        bold = true;
                        break;
                    case 'm':
                        strikethrough = true;
                        break;
                    case 'n':
                        underline = true;
                        break;
                    case 'o':
                        italic = true;
                        break;
                    case 'r':
                        bold = false;
                        underline = false;
                        strikethrough = false;
                        italic = false;
                        break;
                }
            }
        }

        return stringBuilder;
    }
}
