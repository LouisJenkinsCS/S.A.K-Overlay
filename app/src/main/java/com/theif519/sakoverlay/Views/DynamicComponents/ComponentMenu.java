package com.theif519.sakoverlay.Views.DynamicComponents;

import android.util.ArrayMap;
import android.util.Log;

import com.theif519.sakoverlay.Rx.Transformers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by theif519 on 1/4/2016.
 *
 * TODO: Need to have a POJO to contain the BaseComponent, and list of Conditionals and Actions rather than a simple map.
 * TODO: This way I can use an Observable easily to map each.
 */
public class ComponentMenu {
    private ArrayMap<BaseComponent, List<ComponentMethodWrapper>> mMap;

    public ComponentMenu(List<BaseComponent> components) {
        mMap = new ArrayMap<>(components.size());
        Observable
                .create(subscriber -> {
                    for(BaseComponent component: components){
                        for(Method method: component.getConditionals().getClass().getMethods()){
                            List<ComponentMethodWrapper> wrapperList = mMap.get(component);
                            if(wrapperList == null) {
                                mMap.put(component, new ArrayList<>());
                                wrapperList = mMap.get(component);
                            }
                            wrapperList.add(new ComponentMethodWrapper(component, method));
                        }
                        subscriber.onCompleted();
                    }
                })
                .compose(Transformers.backgroundIO())
                .subscribe();
    }

    public class ComponentMethodWrapper {
        private BaseComponent mComponent;
        private Method mMethod;
        private String mDescription;

        public ComponentMethodWrapper(BaseComponent component, Method mMethod) {
            this.mMethod = mMethod;
            this.mComponent = component;
            Observable.<String>create(subscriber -> {
                StringBuilder methodStr = new StringBuilder();
                methodStr.append(mMethod.getReturnType().getSimpleName());
                methodStr.append(" ");
                methodStr.append(mMethod.getName());
                methodStr.append("(");
                boolean preceded = false;
                for(Class<?> params : mMethod.getParameterTypes()){
                    if(preceded) methodStr.append(", ");
                    methodStr.append(params.getSimpleName());
                    preceded = true;
                }
                methodStr.append(")");
                subscriber.onNext(methodStr.toString());
                subscriber.onCompleted();
            }).compose(Transformers.backgroundIO()).subscribe(msg -> {
                mDescription = msg;
                Log.i(getClass().getName(), "Parsed method: " + mDescription);
            });
        }

        public Object invoke(Object... params){
            try {
                return mMethod.invoke(mComponent, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error while attempting to invoke method: \"" + e.getMessage() + "\"");
            }
        }

        public Class<?>[] getParameterTypes(){
            return mMethod.getParameterTypes();
        }

        public Class<?> getReturnType(){
            return mMethod.getReturnType();
        }

        public String getDescription(){
            return mDescription;
        }

        public String getMethodName(){
            return mMethod.getName();
        }
    }
}
