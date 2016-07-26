package jp.co.crowdworks.realm_java_helpers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;
import rx.Subscriber;

public class RealmHelper {
    private static Realm get() {
        return Realm.getDefaultInstance();
    }

    private static final Gson sGson = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    public static <E extends RealmObject> List<E> copyFromRealm(Iterable<E> objects) {
        Realm realm = get();
        List<E> l = realm.copyFromRealm(objects);
        if (!realm.isClosed()) realm.close();
        return l;
    }

    public static <E extends RealmObject> E copyFromRealm(E object) {
        Realm realm = get();
        E e = realm.copyFromRealm(object);
        if (!realm.isClosed()) realm.close();
        return e;
    }

    public static void executeTransactionForRead(Realm.Transaction transaction) {
        Realm realm = get();
        try {
            transaction.execute(realm);
        }
        finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    public static Observable<Void> rxExecuteTransactionAsync(final Realm.Transaction transaction) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                final Realm realm = get();
                realm.executeTransactionAsync(transaction, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                        if (realm != null && !realm.isClosed()) realm.close();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        subscriber.onError(error);
                        if (!realm.isClosed()) realm.close();
                    }
                });
            }
        });
    }

    public static String getJSONForRealmObject(RealmObject obj) {
        return sGson.toJson(obj);
    }

    public static String getJSONForRealmObjectList(List<? extends RealmObject> obj) {
        return sGson.toJson(obj);
    }
}
