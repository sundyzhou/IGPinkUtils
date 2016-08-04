package com.igpink.app.sundyzhou.igpinkutilslib.core;

import android.app.Application;

/**
 * Created by sundyzhou on 8/4/16.
 */
public class App extends Application {
    public static final java.lang.String APP_KEY = "key";
    public static final String QQID = "id";
    public static final String WX_APP_ID = "id";

    public static App instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setInstance(this);
    }

    public static App getInstance() {
        return instance;
    }

    public static void setInstance(App instance) {
        App.instance = instance;
    }
}
