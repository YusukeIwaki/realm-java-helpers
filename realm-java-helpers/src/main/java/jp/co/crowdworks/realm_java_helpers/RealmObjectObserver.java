package jp.co.crowdworks.realm_java_helpers;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class RealmObjectObserver<T extends RealmObject> {
    protected abstract RealmQuery<T> query();
    protected abstract void onChange(T model);

    private Subscription mSub;

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last();
    }

    public void sub() {
        unsub();

        Observable<RealmResults<T>> observable = query().findAll().asObservable();
        mSub = observable
                .filter(new Func1<RealmResults<T>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<T> results) {
                        return !results.isEmpty();
                    }
                })
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
                });
    }

    public void unsub() {
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }
}