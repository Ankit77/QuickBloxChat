package com.indianic.qbchat.comman;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.indianic.qbchat.R;


/**
 * Purpose: This class set the font to textView according to attribute
 *
 * @author
 * @version 1.0
 * @date
 */
public class QbCustomTextViewMedium extends TextView {

    public QbCustomTextViewMedium(Context context) {
        super(context);
    }

    public QbCustomTextViewMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public QbCustomTextViewMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.customTextView);
        String font = a.getString(R.styleable.customTextView_font_name);
        if (font != null) {
            setCustomFont(font, context);
        } else {
            setCustomFont(context.getString(R.string.font_medium), context);
        }
        a.recycle();
    }

    /**
     * Sets a font on a textView
     *
     * @param font
     * @param context
     */
    private void setCustomFont(String font, Context context) {
        if (font == null) {
            return;
        }
        Typeface tf = QbFontCache.get(font, context);
        if (tf != null) {
            setTypeface(tf);
        }
    }
}