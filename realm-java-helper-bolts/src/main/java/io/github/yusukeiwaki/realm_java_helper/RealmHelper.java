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
        Realm realm = getRealm();
        boolean isInTransaction = realm.isInTransaction();
        if (shouldUseSyncTransaction() || isInTransaction) return executeTransactionSync(realm, isInTransaction, transaction);
        else return executeTransactionAsync(realm, transaction);
    }

    private Task<Void> executeTransactionSync(Realm realm, boolean isInTransaction, final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        try {
            if (isInTransaction) {
                transaction.execute(realm);
            } else {
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
            }
            task.setResult(null);
        } catch (Exception exception) {
            task.setError(exception);
        }

        return task.getTask();
    }

    private Task<Void> executeTransactionAsync(final Realm realm, final Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();
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
