package io.github.yusukeiwaki.realm_java_helper;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmObjectObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
    public interface Query<T extends RealmObject> {
        RealmQuery<T> query(Realm realm);
    }

    public interface OnUpdateListener<T extends RealmObject> {
        void onUpdateRealmObject(@Nullable T object);
    }

    private final Query<T> query;
    private OnUpdateListener<T> onUpdateListener;

    public RealmObjectObserver(Query<T> query) {
        this.query = query;
    }

    public void setOnUpdateListener(OnUpdateListener<T> onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    @Override
    protected final RealmResults<T> queryItems(Realm realm) {
        return execQuery(query.query(realm));
    }

    @Override
    protected final RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            private String previousResultString;

            @Override
            public void onChange(RealmResults<T> results) {
                T currentResult = extractObjectFromResults(results);
                String currentResultString = currentResult != null ? getComparationStringFor(currentResult) : "";
                if (previousResultString != null && previousResultString.equals(currentResultString)) {
                    return;
                }
                previousResultString = currentResultString;
                if (onUpdateListener != null) {
                    onUpdateListener.onUpdateRealmObject(currentResult != null ? realm.copyFromRealm(currentResult) : null);
                }
            }
        };
    }

    protected RealmResults<T> execQuery(RealmQuery<T> query) {
        return query.findAll();
    }

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last(null);
    }

    protected String getComparationStringFor(T object) {
        return object.toString();
    }
}