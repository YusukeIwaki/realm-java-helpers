package jp.co.crowdworks.realm_java_helpers_bolts;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmListObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
    private static final String TAG = RealmListObserver.class.getSimpleName();

    private String previousResultsString;

    @Override
    protected RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            @Override
            public void onChange(RealmResults<T> results) {
                String currentResultString = results != null ? results.toString() : "";
                if (previousResultsString != null && previousResultsString.equals(currentResultString)) {
                    return;
                }
                previousResultsString = currentResultString;
                onCollectionChanged(results);
            }
        };
    }

    protected abstract void onCollectionChanged(List<T> models);
}