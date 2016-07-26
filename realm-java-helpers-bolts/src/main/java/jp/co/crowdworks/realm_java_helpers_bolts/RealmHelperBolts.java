package jp.co.crowdworks.realm_java_helpers_bolts;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;

public class RealmHelperBolts {
    private static Realm get() {
        return Realm.getDefaultInstance();
    }

    public static Task<Void> executeTransactionAsync(final Realm.Transaction transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

        final Realm realm = get();
        realm.executeTransactionAsync(transaction, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                task.setResult(null);
                if (realm != null && !realm.isClosed()) realm.close();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                task.setError(new Exception(error));
                if (!realm.isClosed()) realm.close();
            }
        });

        return task.getTask();
    }
}
