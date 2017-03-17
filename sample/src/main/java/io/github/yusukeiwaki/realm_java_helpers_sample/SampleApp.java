package io.github.yusukeiwaki.realm_java_helpers_sample;

import android.app.Application;

import io.realm.Realm;

public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
