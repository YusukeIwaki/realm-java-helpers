package io.github.yusukeiwaki.realm_java_helpers_bolts;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 */
abstract class AbstractRealmResultsObserver<T extends RealmObject> {
    protected Realm realm;
    private RealmChangeListener<RealmResults<T>> listener;
    private RealmResults<T> results;

    protected abstract RealmResults<T> queryItems(Realm realm);

    protected abstract RealmChangeListener<RealmResults<T>> getListener();

    public void sub() {
        unsub();

        realm = RealmHelper.get();
        listener = getListener();
        results = queryItems(realm);

        listener.onChange(results);
        results.addChangeListener(listener);
    }

    public void unsub() {
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
}
