package io.github.yusukeiwaki.realm_java_helper;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

public abstract class AbstractRealmListObserver<T extends RealmModel> extends AbstractRealmResultsObserver<T> {
    @Override
    protected final RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            private String previousResultsString;

            @Override
            public void onChange(RealmResults<T> results) {
                String currentResultString = results != null ? getComparationStringFor(results) : "";
                if (previousResultsString != null && previousResultsString.equals(currentResultString)) {
                    return;
                }
                previousResultsString = currentResultString;
                onUpdateRealmList(realm.copyFromRealm(results));
            }
        };
    }

    protected abstract void onUpdateRealmList(@NonNull List<T> results);
    protected String getComparationStringFor(RealmResults<T> results) {
        return results.toString();
    }
}