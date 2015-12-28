package com.theif519.sakoverlay.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.DynamicComponents.BaseComponent;
import com.theif519.sakoverlay.Views.DynamicComponents.ComponentFactory;
import com.theif519.utils.Misc.Callbacks;

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
        mTextButton.setOnClickListener(this::createItem);
        mImageViewButton.setOnClickListener(this::createItem);
        mLayoutButton.setOnClickListener(this::createItem);
        mButtonButton.setOnClickListener(this::createItem);
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

    private void createItem(View v) {
        BaseComponent component = ComponentFactory.getComponent(this, (String) v.getTag());
        if(component == null){
            Log.w(getClass().getName(), "ComponentFactory returned null for the tag: \"" + v.getTag() + "\"");
            return;
        }
        mLayout.addView(component);
    }
}
