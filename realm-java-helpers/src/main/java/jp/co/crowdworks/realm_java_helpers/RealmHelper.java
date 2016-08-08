package jp.co.crowdworks.realm_java_helpers;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;
import rx.Subscriber;

public class RealmHelper {

    private static final String TAG = RealmHelper.class.getSimpleName();

    static Realm get() {
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
        if (objects==null) return Collections.emptyList();

        Realm realm = get();
        List<E> l = realm.copyFromRealm(objects);
        if (!realm.isClosed()) realm.close();
        return l;
    }

    public static <E extends RealmObject> E copyFromRealm(E object) {
        if (object==null) return null;

        Realm realm = get();
        E e = realm.copyFromRealm(object);
        if (!realm.isClosed()) realm.close();
        return e;
    }

    public interface Transaction<T> {
        T execute(Realm realm) throws Throwable;
    }

    public static <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
        Realm realm = get();

        T object;

        try {
            object = RealmHelper.copyFromRealm(transaction.execute(realm));
        } catch (Throwable throwable) {
            Log.w(TAG, throwable.getMessage(), throwable);
            object = null;
        } finally {
            if (!realm.isClosed()) realm.close();
        }

        return object;
    }

    public static Observable<Void> rxExecuteTransactionAsync(final Transaction transaction) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            boolean mError = false;

            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                final Realm realm = get();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            mError = false;
                            transaction.execute(realm);
                        } catch (Throwable e) {
                            subscriber.onError(e);
                            if (!realm.isClosed()) realm.close();
                            mError = true;
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (!mError) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                            if (realm != null && !realm.isClosed()) realm.close();
                        }
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
