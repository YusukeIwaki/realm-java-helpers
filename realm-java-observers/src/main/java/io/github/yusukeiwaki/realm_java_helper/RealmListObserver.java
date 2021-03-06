package io.github.yusukeiwaki.realm_java_helper;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmListObserver<T extends RealmModel> extends AbstractRealmResultsObserver<T> {
    public interface Query<T extends RealmModel> {
        RealmResults<T> query(Realm realm);
    }

    public interface OnUpdateListener<T extends RealmModel> {
        void onUpdateRealmList(@NonNull List<T> results);
    }

    private final RealmObserverQuery<T> query;

    private OnUpdateListener<T> onUpdateListener;

    public RealmListObserver(RealmObserverQuery<T> query) {
        this.query = query;
    }

    public void setOnUpdateListener(OnUpdateListener<T> onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    @Override
    protected RealmQuery<T> query(Realm realm) {
        return query.query(realm);
    }

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
                if (onUpdateListener != null) {
                    onUpdateListener.onUpdateRealmList(realm.copyFromRealm(results));
                }
            }
        };
    }

    protected String getComparationStringFor(RealmResults<T> results) {
        return results.toString();
    }
}