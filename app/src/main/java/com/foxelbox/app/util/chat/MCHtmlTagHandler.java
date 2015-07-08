package com.foxelbox.app.util.chat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.*;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.foxelbox.app.R;
import com.foxelbox.app.util.WebUtility;
import org.xml.sax.Attributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MCHtmlTagHandler {
    private static final Map<String, Integer> colorNameSpans;
    private static final Map<String, OnClickSpanFactory> onClickSpans;

    public static final MCHtmlTagHandler instance = new MCHtmlTagHandler();
    private MCHtmlTagHandler() {
    }

    static {
        colorNameSpans = new HashMap<>();
        onClickSpans = new HashMap<>();

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
            public UnstyledClickableSpan newSpan(final String parameter) {
                return new UnstyledClickableSpan() {
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
                };
            }
        });

        onClickSpans.put("run_command", new OnClickSpanFactory() {
            @Override
            public UnstyledClickableSpan newSpan(final String parameter) {
                return new UnstyledClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        WebUtility.sendChatMessage((ActionBarActivity) widget.getContext(), widget, parameter);
                    }
                };
            }
        });
    }

    private abstract static class UnstyledClickableSpan extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {

        }
    }

    interface OnClickSpanFactory {
        UnstyledClickableSpan newSpan(String parameter);
    }

    @TagMarker.TagName("b")
    private static class BoldMarker extends StyleMarker {
        public BoldMarker(Attributes attributes) {
            super(attributes);
        }

        @Override
        protected int getStyle() {
            return Typeface.BOLD;
        }
    }

    @TagMarker.TagName("i")
    private static class ItalicMarker extends StyleMarker {
        public ItalicMarker(Attributes attributes) {
            super(attributes);
        }

        @Override
        protected int getStyle() {
            return Typeface.ITALIC;
        }
    }

    @TagMarker.TagName("u")
    private static class UnderlineMarker extends BasicTagMarker {
        public UnderlineMarker(Attributes attributes) {
            super(attributes);
        }

        @Override
        protected Object getSpan() {
            return new UnderlineSpan();
        }
    }

    @TagMarker.TagName("color")
    private static class ColorMarker extends BasicTagMarker {
        public final int color;

        public ColorMarker(Attributes attributes) {
            super(attributes);
            String colorName = attributes.getValue("name");
            Integer _color = colorNameSpans.get(colorName);
            if (_color == null) {
                _color = colorNameSpans.get("white");
            }
            this.color = _color;
        }

        @Override
        protected Object getSpan() {
            return new ForegroundColorSpan(color);
        }
    }

    @TagMarker.TagName("s")
    private static class StrikethroughMarker extends BasicTagMarker {
        public StrikethroughMarker(Attributes attributes) {
            super(attributes);
        }

        @Override
        protected Object getSpan() {
            return new StrikethroughSpan();
        }
    }

    @TagMarker.TagName("span")
    private static class SpanMarker extends TagMarker {
        public SpanMarker(Attributes attributes) {
            super(attributes);
        }
    }

    private abstract static class StyleMarker extends BasicTagMarker {
        public StyleMarker(Attributes attributes) {
            super(attributes);
        }

        protected abstract int getStyle();

        @Override
        protected Object getSpan() {
            return new StyleSpan(getStyle());
        }
    }

    private static abstract class BasicTagMarker extends TagMarker {
        public BasicTagMarker(Attributes attributes) {
            super(attributes);
        }

        protected abstract Object getSpan();

        @Override
        void applyTo(Editable output, int where, int len) {
            super.applyTo(output, where, len);

            output.setSpan(getSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static class GenericTagMarker extends TagMarker {
        public GenericTagMarker(Attributes attributes) {
            super(attributes);
        }
    }

    private static abstract class TagMarker {
        @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @interface TagName { String value(); }

        public final String onClick;

        public TagMarker(Attributes attributes) {
            this.onClick = attributes.getValue("onclick");
        }

        void applyTo(Editable output, int where, int len) {
            output.removeSpan(this);

            if (onClick == null) {
                return;
            }

            final Matcher matcher = FUNCTION_PATTERN.matcher(onClick);
            if (!matcher.matches()) {
                Log.w("foxelbox_xml", "Unknown chat function pattern:" + onClick);
                return;
            }

            final String eventType = matcher.group(1).toLowerCase();
            final String eventString = matcher.group(2);

            OnClickSpanFactory factory = onClickSpans.get(eventType);
            if (factory == null) {
                Log.w("foxelbox_xml", "Unknown chat function: " + eventType);
                return;
            }

            output.setSpan(factory.newSpan(eventString), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([^(]+)\\('(.*)'\\)$");

    private static final HashMap<String, Constructor<? extends TagMarker>> tagMarkerCtorMap;
    private static final HashMap<String, Class<? extends TagMarker>> tagMarkerClassMap;

    static {
        tagMarkerCtorMap = new HashMap<>();
        tagMarkerClassMap = new HashMap<>();
        try {
            __addMarkerClass(ColorMarker.class);
            __addMarkerClass(SpanMarker.class);
            __addMarkerClass(BoldMarker.class);
            __addMarkerClass(ItalicMarker.class);
            __addMarkerClass(UnderlineMarker.class);
            __addMarkerClass(StrikethroughMarker.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void __addMarkerClass(Class<? extends TagMarker> clazz) throws NoSuchMethodException {
        final TagMarker.TagName name = clazz.getAnnotation(TagMarker.TagName.class);
        if(name == null) {
            return;
        }
        final String strName = name.value().toLowerCase();
        tagMarkerClassMap.put(strName, clazz);
        tagMarkerCtorMap.put(strName, clazz.getConstructor(Attributes.class));
    }

    public void handleTag(boolean opening, String tag, Editable output, Attributes attributes) {
        tag = tag.toLowerCase();
        if(tag.equals("root")) {
            return;
        }

        final int len = output.length();

        if (opening) {
            try {
                Constructor<? extends TagMarker> ctor = tagMarkerCtorMap.get(tag);
                TagMarker obj;
                if (ctor == null) {
                    obj = new GenericTagMarker(attributes);
                } else {
                    obj = ctor.newInstance(attributes);
                }
                output.setSpan(obj, len, len, Spanned.SPAN_MARK_MARK);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            Class<? extends TagMarker> clazz = tagMarkerClassMap.get(tag);
            if (clazz == null) {
                clazz = GenericTagMarker.class;
            }
            TagMarker obj = getLast(output, clazz);
            if (obj == null) {
                return;
            }

            int where = output.getSpanStart(obj);

            obj.applyTo(output, where, len);
        }
    }

    <T> T getLast(Editable text, Class<T> kind) {
        T[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }
}
