package com.theif519.sakoverlay.Components.Misc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by theif519 on 1/16/2016.
 */
public class AttributeHelper {

    public abstract class BaseViewManager implements IViewManager {
        private View mView;

        public BaseViewManager(View v) {
            mView = v;
        }

        public View getView() {
            return mView;
        }
    }

    public interface IViewManager {
        Optional<String> validate();

        void handle();

        void reset();

        @NonNull
        Observable<Void> observeStateChanges();
    }

    private Map<String, List<BaseViewManager>> mViewMap;
    private ViewFlipper mFlipper;
    private WeakReference<Context> mContext;

    public AttributeHelper(Context context) {
        mContext = new WeakReference<>(context);
        mFlipper = new ViewFlipper(context);
        mViewMap = new ArrayMap<>();
    }

    private Context getContext() {
        Context context = mContext.get();
        if (context == null) {
            throw new RuntimeException("Potential Memory Leak avoided, Context returned null in AttributeHelper!");
        }
        return context;
    }

    public AttributeHelper add(String category, BaseViewManager manager) {
        List<BaseViewManager> managerList = mViewMap.get(category);
        if (managerList == null) {
            mViewMap.put(category, managerList = new ArrayList<>());
        }
        managerList.add(manager);
        mFlipper.addView(manager.getView());
        manager.observeStateChanges()
                .subscribe(ignoredParam -> manager.reset());
        return this;
    }

    private boolean validateAll() {
        StringBuilder errMsg = new StringBuilder();
        getAllViewManagers()
                .map(BaseViewManager::validate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(err -> errMsg.append("\"").append(err).append("\"\n"));
        if (!errMsg.toString().isEmpty()) {
            Toast.makeText(getContext(), "Error: {\n" + errMsg.toString() + "}", Toast.LENGTH_LONG).show();
            return false;
        } else return true;
    }

    private void handleAll() {
        getAllViewManagers()
                .forEach(BaseViewManager::handle);
    }

    private void resetAll() {
        getAllViewManagers()
                .forEach(BaseViewManager::reset);
    }

    private Stream<BaseViewManager> getViewManagers(String category) {
        return Stream.of(mViewMap)
                .filter(entry -> entry.getKey().equals(category))
                .flatMap(entry -> Stream.of(entry.getValue()));
    }

    private Stream<BaseViewManager> getAllViewManagers() {
        return Stream.of(mViewMap)
                .flatMap(entry -> Stream.of(entry.getValue()));
    }

}