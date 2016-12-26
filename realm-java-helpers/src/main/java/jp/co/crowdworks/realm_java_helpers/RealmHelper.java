package jp.co.crowdworks.realm_java_helpers;

import android.os.Looper;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Completable;
import rx.CompletableSubscriber;

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

        try (Realm realm = get()) {
            return realm.copyFromRealm(objects);
        }
        catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static <E extends RealmObject> E copyFromRealm(E object) {
        if (object==null) return null;

        try (Realm realm = get()) {
            return realm.copyFromRealm(object);
        }
        catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            return null;
        }
    }

    public interface Transaction<T> {
        T execute(Realm realm) throws Throwable;
    }

    public static <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
        try (Realm realm = get()) {
            return RealmHelper.copyFromRealm(transaction.execute(realm));
        } catch (Throwable throwable) {
            Log.w(TAG, throwable.getMessage(), throwable);
            return null;
        }
    }

    public static Completable rxExecuteTransaction(final Transaction transaction) {
        if (Looper.myLooper()==null) return rxExecuteTransactionSync(transaction);
        else return rxExecuteTransactionAsync(transaction);
    }

    private static Completable rxExecuteTransactionSync(final Transaction transaction) {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(final CompletableSubscriber completableSubscriber) {
                try (Realm realm = get()) {
                    realm.beginTransaction();
                    try {
                        transaction.execute(realm);
                        realm.commitTransaction();
                        completableSubscriber.onCompleted();
                    } catch (Throwable e) {
                        if (realm.isInTransaction()) {
                            realm.cancelTransaction();
                        }
                        completableSubscriber.onError(e);
                    }
                } catch (Throwable throwable) {
                    completableSubscriber.onError(throwable);
                }
            }
        });
    }

    private static Completable rxExecuteTransactionAsync(final Transaction transaction) {
        return Completable.create(new Completable.OnSubscribe() {
            @Override
            public void call(final CompletableSubscriber completableSubscriber) {
                final Realm realm = get();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            transaction.execute(realm);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        realm.close();
                        completableSubscriber.onCompleted();
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        realm.close();
                        completableSubscriber.onError(error);
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
