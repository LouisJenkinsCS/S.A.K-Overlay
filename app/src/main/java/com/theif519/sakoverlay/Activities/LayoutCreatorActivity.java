package com.theif519.sakoverlay.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.DynamicComponent;
import com.theif519.utils.Misc.Callbacks;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by theif519 on 12/26/2015.
 */
public class LayoutCreatorActivity extends Activity {

    private Button mTextButton, mImageViewButton, mLayoutButton, mButtonButton;
    private ViewGroup mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_creater);
        mTextButton = (Button) findViewById(R.id.layout_create_text);
        mImageViewButton = (Button) findViewById(R.id.layout_create_image);
        mLayoutButton = (Button) findViewById(R.id.layout_create_layout);
        mButtonButton = (Button) findViewById(R.id.layout_create_button);
        mLayout = (ViewGroup) findViewById(R.id.layout_view);
        mTextButton.setOnClickListener(v -> createItem(TextView.class));
        mImageViewButton.setOnClickListener(v -> createItem(ImageView.class));
        mLayoutButton.setOnClickListener(v -> createItem(FrameLayout.class));
        mButtonButton.setOnClickListener(v -> createItem(Button.class));
        findViewById(R.id.layout_close).setOnClickListener(v -> {
            new Callbacks.CallbackOnRootChildren<View>() {
                @Override
                public void onChild(View child) {
                    if (child != mLayout) {
                        Log.i(getClass().getName(), child.getClass().getSimpleName() + ": { X: " +
                                child.getX() + ", Y: " + child.getY() + ", Width: " + child.getWidth() + ", Height: " +
                                child.getHeight() + " }");
                    }
                }
            }.onChild(mLayout);
            finish();
        });
    }

    private void createItem(Class<? extends View> clazz) {
        try {
            DynamicComponent component = new DynamicComponent(this);
            component.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            View v = clazz.getConstructor(Context.class).newInstance(this);
            v.setBackground(getDrawable(R.color.transparent_fragment));
            component.addView(v);
            mLayout.addView(component);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(getClass().getName(), "Error occurred while attempted to instantiate: \"" + clazz.getSimpleName()
                    + "\" with the exception: \"" + e.getClass().getSimpleName() + "\" with the exception: \"" + e.getMessage() + "\"");
        }
    }
}
