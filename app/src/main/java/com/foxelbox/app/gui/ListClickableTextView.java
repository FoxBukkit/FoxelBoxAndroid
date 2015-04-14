package com.foxelbox.app.gui;

import android.content.Context;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.regex.Pattern;

public class ListClickableTextView extends TextView {
    public ListClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Object text = getText();
        if (text instanceof Spanned) {
            Spanned buffer = (Spanned)text;

            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();

                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off+1, ClickableSpan.class);

                if (link.length != 0) {
                    link[0].onClick(this);
                    return true;
                }
            }
        }
        return false;
    }
}
