package io.github.yusukeiwaki.realm_java_helper;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

public class RealmHelper extends BaseRealmHelper {
    private static final RealmHelper INSTANCE = new RealmHelper();

    public static RealmHelper getInstance() {
        return INSTANCE;
    }

    public interface Transaction {
        void execute(Realm realm) throws Exception;
    }
    public final Task<Void> executeTransaction(final Transaction transaction) {
        if (shouldUseSyncTransaction()) return executeTransactionSync(transaction);
        else return executeTransactionAsync(transaction);
    }

    private Task<Void> executeTransactionSync(final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        Realm realm = getRealm();
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
        }

        return task.getTask();
    }

    private Task<Void> executeTransactionAsync(final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();
        final Realm realm = getRealm();
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
