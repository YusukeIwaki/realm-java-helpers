package io.github.yusukeiwaki.realm_java_helper;

import android.support.annotation.Nullable;

import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

public abstract class AbstractRealmObjectObserver<T extends RealmModel> extends AbstractRealmResultsObserver<T> {
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
                onUpdateRealmObject(currentResult != null ? realm.copyFromRealm(currentResult) : null);
            }
        };
    }

    protected abstract void onUpdateRealmObject(@Nullable T object);

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last(null);
    }

    protected String getComparationStringFor(T object) {
        return object.toString();
    }
}