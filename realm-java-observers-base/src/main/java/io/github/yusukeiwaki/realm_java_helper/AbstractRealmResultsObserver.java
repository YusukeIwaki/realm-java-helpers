package io.github.yusukeiwaki.realm_java_helper;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 */
abstract class AbstractRealmResultsObserver<T extends RealmModel> {
    protected Realm realm;
    private RealmChangeListener<RealmResults<T>> listener;
    private RealmResults<T> results;

    protected abstract RealmQuery<T> query(Realm realm);

    protected abstract RealmChangeListener<RealmResults<T>> getListener();

    public void subscribe() {
        unsubscribe();

        realm = Realm.getDefaultInstance();
        listener = getListener();
        results = execQuery(query(realm));

        listener.onChange(results);
        results.addChangeListener(listener);
    }

    public void unsubscribe() {
        if (realm != null) {
            if (results != null) {
                if (results.isValid()) {
                    results.removeChangeListener(listener);
                }
                results = null;
            }
            realm.close();
            realm = null;
        }
    }

    protected RealmResults<T> execQuery(RealmQuery<T> query) {
        return query.findAll();
    }

    T copyFromRealm(T object) {
        if (object == null) return null;
        return realm.copyFromRealm(object);
    }

    List<T> copyFromRealm(RealmResults<T> results) {
        if (results == null) return Collections.emptyList();
        return realm.copyFromRealm(results);
    }
}
