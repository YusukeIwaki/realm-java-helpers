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
        T execute(Realm realm) throws Exception;
    }

    public static <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
        try (Realm realm = get()) {
            return RealmHelper.copyFromRealm(transaction.execute(realm));
        } catch (Throwable throwable) {
            Log.w(TAG, throwable.getMessage(), throwable);
            return null;
        }
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

        try (Realm realm = get()) {
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
