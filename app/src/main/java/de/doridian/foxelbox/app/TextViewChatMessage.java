package de.doridian.foxelbox.app;

import android.content.Context;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewChatMessage extends TextView {
    public TextViewChatMessage(Context context) {
        super(context);
    }

    public TextViewChatMessage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewChatMessage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable spannable = ChatFormatterUtility.formatString(text.toString());
        super.setText(spannable, BufferType.SPANNABLE);
    }
}
