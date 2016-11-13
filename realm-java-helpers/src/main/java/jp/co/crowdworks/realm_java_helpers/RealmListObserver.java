package jp.co.crowdworks.realm_java_helpers;

import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class RealmListObserver<T extends RealmObject> {
    private static final String TAG = RealmListObserver.class.getSimpleName();

    protected abstract RealmResults<T> queryItems(Realm realm);
    protected abstract void onCollectionChanged(List<T> models);

    private Subscription mSub;
    private Realm mRealm;

    /**
     *
     * if more precious result is required, use like this:
     *
     * return results
     * .map(new Func1<RealmResults<T>, List<T>>() {
     *    @Override
     *    public List<T> call(RealmResults<T> results) {
     *    return RealmHelper.copyFromRealm(results);
     *    }
     *    })
     * .distinctUntilChanged(new Func1<List<T>, String>() {
     *    @Override
     *    public String call(List<T> list) {
     *    return RealmHelper.getJSONForRealmObjectList(list);
     *    }
     *    })
     *
     * Remark that it increse the frequency of GC.
     */
    protected Observable<? extends List<T>> filter(Observable<RealmResults<T>> results) {
        return results;
    }

    public void sub() {
        unsub();

        mRealm = RealmHelper.get();
        Observable<RealmResults<T>> observable = queryItems(mRealm).asObservable();
        mSub = filter(observable)
                .subscribe(new Action1<List<T>>() {
                    @Override
                    public void call(List<T> results) {
                        onCollectionChanged(results);
                    }
                });
    }

    public void keepalive() {
        if (mRealm == null || mRealm.isClosed()) {
            try {
                unsub();
            }
            catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }
            sub();
        }
    }

    public void unsub() {
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }
}