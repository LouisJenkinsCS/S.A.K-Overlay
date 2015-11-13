package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.R;
import com.theif519.utils.AttributeRetriever;

/**
 * Created by theif519 on 11/7/2015.
 */
public class TitleBar extends RelativeLayout {

    private TextView mTitle;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.title_bar, this);
        mTitle = (TextView) findViewById(R.id.title_bar_text);
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    @com.theif519.utils.AttributeRetriever.AttributeHelper(source = "tbTitle")
    public void setTitle(String title){
        mTitle.setText(title);
        mTitle.invalidate();
        mTitle.requestLayout();
    }

}
