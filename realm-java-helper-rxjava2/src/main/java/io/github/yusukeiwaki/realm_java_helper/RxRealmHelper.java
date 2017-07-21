package io.github.yusukeiwaki.realm_java_helper;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;

public class RxRealmHelper extends BaseRealmHelper {
    private static final RxRealmHelper INSTANCE = new RxRealmHelper();

    public static RxRealmHelper getInstance() {
        return INSTANCE;
    }

    public interface Transaction {
        void execute(Realm realm) throws Throwable;
    }

    public static final Object SUCCESS = new Object();

    public final Single executeTransaction(final Transaction transaction) {
        if (shouldUseSyncTransaction()) return executeTransactionSync(transaction);
        else return executeTransactionAsync(transaction);
    }

    private Single executeTransactionSync(final Transaction transaction) {
        return Single.create(new SingleOnSubscribe() {
            @Override
            public void subscribe(@NonNull SingleEmitter singleEmitter) throws Exception {
                Realm realm = getRealm();
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                transaction.execute(realm);
                            } catch (Throwable throwable) {
                                throw new RuntimeException(throwable);
                            }
                        }
                    });
                    singleEmitter.onSuccess(SUCCESS);
                } catch (Throwable throwable) {
                    singleEmitter.onError(throwable);
                }
            }
        });
    }

    private Single executeTransactionAsync(final Transaction transaction) {
        return Single.create(new SingleOnSubscribe() {
            @Override
            public void subscribe(@NonNull final SingleEmitter singleEmitter) throws Exception {
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
                        singleEmitter.onSuccess(SUCCESS);
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        realm.close();
                        singleEmitter.onError(error);
                    }
                });

            }
        });
    }
}
