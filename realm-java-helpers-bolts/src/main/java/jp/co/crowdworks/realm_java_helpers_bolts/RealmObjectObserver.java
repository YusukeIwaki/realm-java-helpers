package jp.co.crowdworks.realm_java_helpers_bolts;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public abstract class RealmObjectObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
    private static final String TAG = RealmObjectObserver.class.getSimpleName();

    private String previousResultString;

    @Override
    protected final RealmResults<T> queryItems(Realm realm) {
        return query(realm).findAll();
    }

    @Override
    protected final RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            @Override
            public void onChange(RealmResults<T> results) {
                T currentResult = extractObjectFromResults(results);
                String currentResultString = currentResult != null ? currentResult.toString() : "";
                if (previousResultString != null && previousResultString.equals(currentResultString)) {
                    return;
                }
                previousResultString = currentResultString;
                RealmObjectObserver.this.onChange(currentResult != null ? realm.copyFromRealm(currentResult) : null);
            }
        };
    }

    protected abstract RealmQuery<T> query(Realm realm);
    protected abstract void onChange(T model);

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last(null);
    }
}