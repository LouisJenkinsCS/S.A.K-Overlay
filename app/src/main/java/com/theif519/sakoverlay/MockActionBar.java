package com.theif519.sakoverlay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by theif519 on 10/29/2015.
 */
public class MockActionBar extends RelativeLayout {

    private TextView mTitle;

    public MockActionBar(Context context) {
        this(context, null);
    }

    public MockActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mock_action_bar, this);
        mTitle = (TextView) findViewById(R.id.custom_action_title);
        com.theif519.utils.AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    @com.theif519.utils.AttributeRetriever.AttributeHelper(source = "mabTitle")
    public void setTitle(String title){
        mTitle.setText(title);
        mTitle.invalidate();
        mTitle.requestLayout();
    }

    public String getTitle(String title){
        return mTitle.getText().toString();
    }
}
