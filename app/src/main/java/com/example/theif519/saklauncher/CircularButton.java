package com.example.theif519.saklauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by theif519 on 10/21/2015.
 */
public class CircularButton extends Button {

    private static final String TAG = "CircularButton";

    private Drawable mImage;
    private Button mButton;
    private Method mCallbackMethod = null;

    public CircularButton(Context context) {
        this(context, null);
    }

    public CircularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.roundbutton);
        invalidate();
        requestLayout();
        Utils.AttributeRetriever.fillAttributes(this.getClass(), this, context, attrs);
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String[] options = getResources().getStringArray(R.array.circular_button_options);
                new AlertDialog.Builder(getContext()).setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "Selected: " + options[which], Toast.LENGTH_SHORT).show();
                    }
                }).setTitle("Button Menu").show();
                return true;
            }
        });
    }

    public void setImage(Drawable img) {
        mImage = img;
    }
}
