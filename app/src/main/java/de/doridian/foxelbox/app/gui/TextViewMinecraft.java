package de.doridian.foxelbox.app.gui;

import android.content.Context;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewMinecraft extends TextView {
    public TextViewMinecraft(Context context) {
        super(context);
    }

    public TextViewMinecraft(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewMinecraft(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable spannable = ChatFormatterUtility.formatString(text.toString());
        super.setText(spannable, BufferType.SPANNABLE);
    }
}
