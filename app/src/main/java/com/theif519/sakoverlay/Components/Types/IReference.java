package com.theif519.sakoverlay.Components.Types;

import com.theif519.sakoverlay.Components.Types.Actions.Impl.Actions;
import com.theif519.sakoverlay.Components.Types.Conditionals.Impl.Conditionals;

/**
 * Created by theif519 on 1/22/2016.
 */
public interface IReference<T> {
    Class<? extends Conditionals> getConditionals();

    Class<? extends Actions> getActions();

    //Class<? extends Getters> getGetters();

    //Class<? extends Setters> getSetters();

    String getKey();

    Class<T> getType();
}
