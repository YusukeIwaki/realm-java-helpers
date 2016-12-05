package jp.co.crowdworks.realm_java_helpers;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class RealmObjectObserver<T extends RealmObject> {
    private static final String TAG = RealmObjectObserver.class.getSimpleName();

    protected abstract RealmQuery<T> query(Realm realm);
    protected abstract void onChange(T model);

    private Subscription mSub;
    private Realm mRealm;

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last(null);
    }

    public void sub() {
        unsub();

        mRealm = RealmHelper.get();
        Observable<RealmResults<T>> observable = query(mRealm).findAll().asObservable();
        mSub = observable
                .map(new Func1<RealmResults<T>, T>() {
                    @Override
                    public T call(RealmResults<T> results) {
                        return extractObjectFromResults(results);
                    }
                })
                .subscribe(new Action1<T>() {
                    @Override
                    public void call(T result) {
                        onChange(result);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "error", throwable);
                        unsub();
                    }
                });
    }

    public void unsub() {
        if (mRealm != null) {
            if (mSub != null && !mSub.isUnsubscribed()) {
                mSub.unsubscribe();
            }
            mRealm.close();
            mRealm = null;
        }
    }
}