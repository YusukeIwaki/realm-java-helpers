package jp.co.crowdworks.realm_java_helpers;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class RealmListObserver<T extends RealmObject> {
    protected abstract RealmResults<T> queryItems(Realm realm);
    protected abstract void onCollectionChanged(List<T> models);

    private Subscription mSub;
    private Realm mRealm;

    public void sub() {
        unsub();

        mRealm = RealmHelper.get();
        Observable<RealmResults<T>> observable = queryItems(mRealm).asObservable();
        mSub = observable
                .map(new Func1<RealmResults<T>, List<T>>() {
                    @Override
                    public List<T> call(RealmResults<T> results) {
                        return RealmHelper.copyFromRealm(results);
                    }
                })
                .distinctUntilChanged(new Func1<List<T>, String>() {
                    @Override
                    public String call(List<T> list) {
                        return RealmHelper.getJSONForRealmObjectList(list);
                    }
                })
                .subscribe(new Action1<List<T>>() {
                    @Override
                    public void call(List<T> results) {
                        onCollectionChanged(results);
                    }
                });
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