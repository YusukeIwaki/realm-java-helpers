package jp.co.crowdworks.realm_java_helpers_bolts;

import android.os.Looper;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

public class RealmHelperBolts {
    private static Realm get() {
        return Realm.getDefaultInstance();
    }

    public interface Transaction<T> {
        T execute(Realm realm) throws Exception;
    }

    public static Task<Void> executeTransaction(final Transaction transaction) {
        return Looper.myLooper()==null ?
                executeTransactionSync(transaction) : executeTransactionAsync(transaction);
    }

    public static Task<Void> executeTransactionSync(final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        final Realm realm = get();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    transaction.execute(realm);
                    task.setResult(null);
                } catch (Exception e) {
                    task.setError(e);
                }
            }
        });
        if (!realm.isClosed()) realm.close();

        return task.getTask();
    }

    public static Task<Void> executeTransactionAsync(final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        final Realm realm = get();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    transaction.execute(realm);
                } catch (Exception e) {
                    task.setError(e);
                    if (!realm.isClosed()) realm.close();
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (task.trySetResult(null)) {
                    if (realm != null && !realm.isClosed()) realm.close();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (task.trySetError(new Exception(error))) {
                    if (!realm.isClosed()) realm.close();
                }
            }
        });

        return task.getTask();
    }
}
