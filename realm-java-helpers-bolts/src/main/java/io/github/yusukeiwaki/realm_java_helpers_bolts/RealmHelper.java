package io.github.yusukeiwaki.realm_java_helpers_bolts;

import android.os.Looper;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
import io.realm.RealmObject;

public class RealmHelper {

    private static final String TAG = RealmHelper.class.getSimpleName();

    static Realm get() {
        return Realm.getDefaultInstance();
    }

    private interface RealmHandling<T> {
        T execute(Realm realm) throws Exception;
        T onError(Exception exception);
    }
    private static <T> T withRealm(RealmHandling<T> process) {
        Realm realm = null;
        T ret;
        try {
            realm = get();
            ret = process.execute(realm);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            ret = process.onError(e);
        } finally {
            if (realm != null) realm.close();
        }
        return ret;
    }

    public static <E extends RealmObject> List<E> copyFromRealm(final Iterable<E> objects) {
        if (objects==null) return Collections.emptyList();

        return withRealm(new RealmHandling<List<E>>() {
            @Override
            public List<E> execute(Realm realm) throws Exception {
                return realm.copyFromRealm(objects);
            }

            @Override
            public List<E> onError(Exception e) {
                return Collections.emptyList();
            }
        });
    }

    public static <E extends RealmObject> E copyFromRealm(final E object) {
        if (object==null) return null;

        return withRealm(new RealmHandling<E>() {
            @Override
            public E execute(Realm realm) throws Exception {
                return realm.copyFromRealm(object);
            }

            @Override
            public E onError(Exception e) {
                return null;
            }
        });
    }

    public interface Transaction<T> {
        T execute(io.realm.Realm realm) throws Exception;
    }

    public static <T extends RealmObject> T executeTransactionForRead(final Transaction<T> transaction) {
        return withRealm(new RealmHandling<T>() {
            @Override
            public T execute(Realm realm) throws Exception {
                return RealmHelper.copyFromRealm(transaction.execute(realm));
            }

            @Override
            public T onError(Exception e) {
                return null;
            }
        });
    }

    private static boolean shouldUseSync() {
        // ref: realm-java:realm/realm-library/src/main/java/io/realm/AndroidNotifier.java
        // #isAutoRefreshAvailable()

        if (Looper.myLooper() == null) {
            return true;
        }

        String threadName = Thread.currentThread().getName();
        return threadName != null && threadName.startsWith("IntentService[");
    }

    public static Task<Void> executeTransaction(final Transaction transaction) {
        if (shouldUseSync()) return executeTransactionSync(transaction);
        else return executeTransactionAsync(transaction);
    }

    private static Task<Void> executeTransactionSync(final RealmHelper.Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        Realm realm = get();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        transaction.execute(realm);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }
            });
            task.setResult(null);
        } catch (Exception exception) {
            task.setError(exception);
        } finally {
            realm.close();
        }

        return task.getTask();
    }

    private static Task<Void> executeTransactionAsync(final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();
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
                task.setResult(null);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                realm.close();
                if (error instanceof Exception) {
                    task.setError((Exception) error);
                } else {
                    task.setError(new Exception(error));
                }
            }
        });

        return task.getTask();
    }
}
