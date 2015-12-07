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

    private ImageView mIcon;
    private TextView mDescription;

    public MenuChildItem(Context context) {
        this(context, null);
    }

    public MenuChildItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_child_item, this);
        mIcon = (ImageView) findViewById(R.id.menu_child_item_icon);
        mDescription = (TextView) findViewById(R.id.menu_child_item_description);
        AttributeRetriever.fillAttributes(getClass(), this, context, attrs);
    }

    public Bitmap getIcon() {
        return ((BitmapDrawable) mIcon.getDrawable()).getBitmap();
    }

    @AttributeRetriever.AttributeHelper(source = "mciIcon")
    public void setIcon(Bitmap icon) {
        mIcon.setImageBitmap(icon);
    }

    public String getDescription() {
        return mDescription.getText().toString();
    }

    @AttributeRetriever.AttributeHelper(source = "mciDescription")
    public void setDescription(String descr) {
        mDescription.setText(descr);
    }

}
