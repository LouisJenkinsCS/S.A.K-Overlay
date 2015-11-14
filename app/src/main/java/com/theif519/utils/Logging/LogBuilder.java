package com.theif519.utils.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by theif519 on 11/14/2015.
 *
 * A simple tool implementing the Builder pattern to reduce the tedium of creating a logger, without using
 * custom libraries. The reasoning is that Android's logger is not good at all for logging to custom files,
 * nor for customization.
 */
public class LogBuilder {

    private Logger mLogger, mParent = null;
    private ArrayList<Handler> mHandlers;
    private Filter mFilter = null;
    private Level mLevel = Level.ALL;
    private boolean mUseParentHandler = false;

    public LogBuilder(String loggerName){
        mLogger = Logger.getLogger(loggerName);
        mHandlers = new ArrayList<>();
    }

    public LogBuilder addHandlers(Handler ...handlers){
        Collections.addAll(mHandlers, handlers);
        return this;
    }

    public LogBuilder setFilter(Filter filter){
        mFilter = filter;
        return this;
    }

    public LogBuilder setLevel(Level level){
        mLevel = level;
        return this;
    }

    public LogBuilder setParent(Logger parent){
        mParent = parent;
        return this;
    }

    public LogBuilder useParentHandlers(Boolean flag){
        mUseParentHandler = flag;
        return this;
    }

    public Logger build(){
        if(mParent != null) {
            mLogger.setParent(mParent);
            mLogger.setUseParentHandlers(mUseParentHandler);
        }
        if(mFilter != null) mLogger.setFilter(mFilter);
        mLogger.setLevel(mLevel);
        for(Handler handler: mHandlers){
            handler.setLevel(mLevel);
            mLogger.addHandler(handler);
        }
        return mLogger;
    }
}
