package com.mcuking.mwbpcontainer.application;

import android.content.Context;


public class MWBPApplication extends android.app.Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getGlobalContext() {
        return context;
    }
}
