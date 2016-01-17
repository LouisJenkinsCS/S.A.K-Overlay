package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.util.ArrayMap;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.theif519.sakoverlay.Components.View.AttributeMenuManager;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by theif519 on 1/16/2016.
 *
 * Warning: Not thread safe!
 */
public class AttributeMenuHelper {

    private static AttributeMenuHelper INSTANCE;

    public static void initialize(Context context){
        if(INSTANCE == null){
            INSTANCE = new AttributeMenuHelper(context);
        }
    }

    public static AttributeMenuHelper getInstance(String key){
        if(INSTANCE == null){
            throw new RuntimeException("Forgot to call initialize(Context) before getInstance(String)!");
        }
        INSTANCE.setCurrent(key);
        return INSTANCE;
    }

    private Map<String, AttributeMenuManager> mMappedManagers;
    private WeakReference<Context> mContext;
    private AttributeMenuManager mCurrentManager;

    public AttributeMenuHelper(Context context) {
        mContext = new WeakReference<>(context);
        mMappedManagers = new ArrayMap<>();
    }

    private Context getContext() {
        Context context = mContext.get();
        if (context == null) {
            throw new RuntimeException("Potential Memory Leak avoided, Context returned null in AttributeMenuHelper!");
        }
        return context;
    }

    public AttributeMenuHelper add(String category, BaseViewManager manager) {
        mCurrentManager.add(category, manager);
        return this;
    }

    public AttributeMenuHelper remove(String category){
        mCurrentManager.remove(category);
        return this;
    }

    private void setCurrent(String key){
        if(mMappedManagers.containsKey(key)){
            mCurrentManager = mMappedManagers.get(key);
        } else {
            mCurrentManager = new AttributeMenuManager(getContext(), key);
            mMappedManagers.put(key, mCurrentManager);
        }
    }

    // TODO: Add option to animate here when animations are implemented
    public boolean isValid(boolean silent) {
        Optional<String> errMsg = mCurrentManager.validate();
        if(errMsg.isPresent()){
            if(!silent){
                Toast.makeText(getContext(), errMsg.get(), Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    public AttributeMenuHelper handleInput(){
        mCurrentManager.handle();
        return this;
    }

    public AttributeMenuHelper reset() {
        mCurrentManager.reset();
        return this;
    }
}