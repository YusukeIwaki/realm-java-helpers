package io.github.yusukeiwaki.realm_java_helper;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmQuery;

public interface RealmObserverQuery<T extends RealmModel> {
    RealmQuery<T> query(Realm realm);
}
