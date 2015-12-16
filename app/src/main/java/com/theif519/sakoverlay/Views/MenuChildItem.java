package com.theif519.sakoverlay.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theif519.sakoverlay.R;
import com.theif519.utils.Misc.AttributeRetriever;

/**
 * Created by theif519 on 11/27/2015.
 * <p/>
 * Encaspulates a Menu Option item. It should be used for any and all menu options, but for now it is
 * only used with the Icon menu, which lists all applications.
 */
public class MenuChildItem extends RelativeLayout {

    private ImageView mDescriptionIcon;
    private TextView mDescriptionText;

    public MenuChildItem(Context context) {
        super(context);
    }

    public MenuChildItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_option, this);
        mDescriptionIcon = (ImageView) findViewById(R.id.menu_option_icon);
        mDescriptionText = (TextView) findViewById(R.id.menu_option_description);
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    public Bitmap getIcon() {
        return ((BitmapDrawable) mDescriptionIcon.getDrawable()).getBitmap();
    }

    @AttributeRetriever.AttributeHelper(source = "mciIcon")
    public void setIcon(Bitmap icon) {
        mDescriptionIcon.setImageBitmap(icon);
    }

    public String getDescription() {
        return mDescriptionText.getText().toString();
    }

    @AttributeRetriever.AttributeHelper(source = "mciDescription")
    public void setDescription(String descr) {
        mDescriptionText.setText(descr);
    }

}
