package com.theif519.sakoverlay.Components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.Misc.AttributeMenuHelper;
import com.theif519.sakoverlay.Components.Misc.BaseViewManager;
import com.theif519.sakoverlay.Components.Types.Actions.Impl.Actions;
import com.theif519.sakoverlay.Components.Types.Actions.Impl.BaseActions;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.BaseConditionals;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.Conditionals;
import com.theif519.sakoverlay.Components.Types.IReference;
import com.theif519.sakoverlay.Core.Misc.Globals;
import com.theif519.sakoverlay.R;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by theif519 on 12/27/2015.
 */
public abstract class BaseComponent extends FrameLayout implements IReference {

    private FrameLayout mContainer, mRoot;
    private String mKey;
    private PublishSubject<Void> mSizeOrPositionChanged = PublishSubject.create();

    public BaseComponent(Context context, String key) {
        super(context);
        mKey = key;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dynamic_component, this);
        findViewById(R.id.component_wrapper_resize).setOnTouchListener(this::resize);
        findViewById(R.id.component_wrapper_move).setOnTouchListener(this::move);
        mContainer = (FrameLayout) findViewById(R.id.component_wrapper_container);
        mContainer.addView(createView(context));
        mRoot = (FrameLayout) findViewById(R.id.component_wrapper_root);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        createAttributeMenu().show();
        setup();
    }

    private float tmpX, tmpY;

    private boolean resize(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tmpX = mRoot.getX();
                tmpY = mRoot.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                mRoot.getLayoutParams().width = (int) Math.abs(event.getRawX() - tmpX);
                mRoot.getLayoutParams().height = (int) Math.abs(event.getRawY() - tmpY);
                mRoot.invalidate();
                mRoot.requestLayout();
                mSizeOrPositionChanged.onNext(null);
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    private float touchXOffset, touchYOffset;

    private boolean move(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchXOffset = (int) event.getRawX() - getX();
                touchYOffset = (int) event.getRawY() - getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                setX(event.getRawX() - touchXOffset);
                setY(event.getRawY() - touchYOffset);
                mSizeOrPositionChanged.onNext(null);
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    abstract protected View createView(Context context);


    protected AttributeMenuHelper createAttributeMenu() {
        return AttributeMenuHelper.getInstance(mKey)
                .add("Size & Position", createPositionAndSize());
    }

    protected void setup() {

    }

    public JSONObject serialize() {
        try {
            return new JSONObject()
                    .put(Globals.Keys.X, getX())
                    .put(Globals.Keys.Y, getY())
                    .put(Globals.Keys.WIDTH, getWidth())
                    .put(Globals.Keys.HEIGHT, getHeight());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    public String getKey(){
        return mKey;
    }

    public void deserialize(JSONObject obj) {
        try {
            setX((float) obj.getDouble(Globals.Keys.X));
            setY((float) obj.getDouble(Globals.Keys.Y));
            mRoot.getLayoutParams().width = obj.getInt(Globals.Keys.WIDTH);
            mRoot.getLayoutParams().height = obj.getInt(Globals.Keys.HEIGHT);
            mRoot.invalidate();
            mRoot.requestLayout();
        } catch (JSONException e) {
            throw new RuntimeException("Error deserializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    private void onFocus() {
        AttributeMenuHelper
                .getInstance(mKey)
                .show();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onFocus();
        }
        return false;
    }

    public Class<? extends Conditionals> getConditionalClass() {
        return BaseConditionals.class;
    }

    public Class<? extends Actions> getActionClass() {
        return BaseActions.class;
    }

    private BaseViewManager createPositionAndSize() {
        ViewGroup layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.component_base, null);
        final EditText coordX = (EditText) layout.findViewById(R.id.component_base_x);
        final EditText coordY = (EditText) layout.findViewById(R.id.component_base_y);
        final EditText sizeWidth = (EditText) layout.findViewById(R.id.component_base_width);
        final RadioButton widthWrapContent = (RadioButton) layout.findViewById(R.id.component_base_width_wrap);
        final RadioButton widthFillParent = (RadioButton) layout.findViewById(R.id.component_base_width_fill);
        final RadioButton widthCustom = (RadioButton) layout.findViewById(R.id.component_base_width_custom);
        final EditText sizeHeight = (EditText) layout.findViewById(R.id.component_base_height);
        final RadioButton heightWrapContent = (RadioButton) layout.findViewById(R.id.component_base_height_wrap);
        final RadioButton heightFillParent = (RadioButton) layout.findViewById(R.id.component_base_height_fill);
        final RadioButton heightCustom = (RadioButton) layout.findViewById(R.id.component_base_height_custom);
        widthWrapContent.setChecked(true);
        heightWrapContent.setChecked(true);
        return new BaseViewManager(layout) {
            @Override
            public Optional<String> validate() {
                StringBuilder errMsg = new StringBuilder();
                int maxX = Globals.MAX_X.get(), maxY = Globals.MAX_Y.get();
                if (widthCustom.isChecked()) {
                    if (Integer.parseInt(sizeWidth.getText().toString()) > maxX) {
                        errMsg
                                .append("Width must be less than ")
                                .append(maxX)
                                .append("\n");
                    }
                }
                if (heightCustom.isChecked()) {
                    if (Integer.parseInt(sizeHeight.getText().toString()) > maxY) {
                        errMsg
                                .append("Height must be less than ")
                                .append(maxY)
                                .append("\n");
                    }
                }
                if (Integer.parseInt(coordX.getText().toString()) > maxX) {
                    errMsg
                            .append("X-Coordinate must be less than ")
                            .append(maxX)
                            .append("\n");
                }
                if (Integer.parseInt(coordY.getText().toString()) > maxY) {
                    errMsg
                            .append("Y-Coordinate must be less than ")
                            .append(maxY)
                            .append("\n");
                }
                return errMsg.toString().isEmpty() ? Optional.empty() : Optional.of(errMsg.toString());
            }

            @Override
            public void handle() {
                // TODO: Fix this, as resizing will not work well at all.
                if (widthFillParent.isChecked()) {
                    mRoot.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                } else if (widthWrapContent.isChecked()) {
                    mRoot.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
                } else {
                    mRoot.getLayoutParams().width = Integer.parseInt(sizeWidth.getText().toString());
                }
                if (heightFillParent.isChecked()) {
                    mRoot.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                } else if (heightWrapContent.isChecked()) {
                    mRoot.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    mRoot.getLayoutParams().height = Integer.parseInt(sizeHeight.getText().toString());
                }
                setX(Integer.parseInt(coordX.getText().toString()));
                setY(Integer.parseInt(coordY.getText().toString()));
                mRoot.invalidate();
                mRoot.requestLayout();
            }

            @Override
            public void reset() {
                sizeWidth.setText("" + getWidth());
                sizeHeight.setText("" + getHeight());
                coordX.setText("" + (int) getX());
                coordY.setText("" + (int) getY());
                // TODO: Add a simple id to the root of both Width and Height
                ((ViewGroup) sizeWidth.getParent()).setVisibility(INVISIBLE);
                switch (mRoot.getLayoutParams().width) {
                    case ViewGroup.LayoutParams.MATCH_PARENT:
                        widthFillParent.setChecked(true);
                        break;
                    case ViewGroup.LayoutParams.WRAP_CONTENT:
                        widthWrapContent.setChecked(true);
                        break;
                    default:
                        widthCustom.setChecked(true);
                        ((ViewGroup) sizeWidth.getParent()).setVisibility(VISIBLE);
                        break;
                }
                ((ViewGroup) sizeHeight.getParent()).setVisibility(INVISIBLE);
                switch (mRoot.getLayoutParams().height) {
                    case ViewGroup.LayoutParams.MATCH_PARENT:
                        heightFillParent.setChecked(true);
                        break;
                    case ViewGroup.LayoutParams.WRAP_CONTENT:
                        heightWrapContent.setChecked(true);
                        break;
                    default:
                        heightCustom.setChecked(true);
                        ((ViewGroup) sizeHeight.getParent()).setVisibility(VISIBLE);
                        break;
                }
            }

            @NonNull
            @Override
            public Observable<Void> observeStateChanges() {
                return mSizeOrPositionChanged.asObservable();
            }
        };
    }

    @Override
    public Class<? extends Conditionals> getConditionals() {
        return BaseConditionals.class;
    }

    @Override
    public Class<? extends Actions> getActions() {
        return BaseActions.class;
    }

    @Override
    public Class<?> getType() {
        return getClass();
    }
}
