package io.github.yusukeiwaki.realm_java_helper;

import android.os.Looper;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;

/*package*/ abstract class BaseRealmHelper {
    private static final String TAG = BaseRealmHelper.class.getSimpleName();

    protected final Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public final <E extends RealmModel> List<E> copyFromRealm(Iterable<E> objects) {
        if (objects==null) return Collections.emptyList();

        Realm realm = getRealm();
        try {
            return realm.copyFromRealm(objects);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            realm.close();
        }
    }

    public final <E extends RealmModel> E copyFromRealm(E object) {
        if (object==null) return null;

        Realm realm = getRealm();
        try {
            return realm.copyFromRealm(object);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage(), e);
            return null;
        } finally {
            realm.close();
        }
    }

    public interface TransactionForRead<T> {
        T execute(Realm realm) throws Exception;
    }

    public final <T extends RealmModel> T executeTransactionForRead(TransactionForRead<T> transaction) {
        Realm realm = getRealm();
        try {
            return copyFromRealm(transaction.execute(realm));
        } catch (Exception exception) {
            Log.w(TAG, exception.getMessage(), exception);
            return null;
        } finally {
            realm.close();
        }
    }

    protected final boolean shouldUseSyncTransaction() {
        // ref: realm-java:realm/realm-library/src/main/java/io/realm/AndroidNotifier.java
        // #isAutoRefreshAvailable()

        if (Looper.myLooper() == null) {
            return true;
        }

        String threadName = Thread.currentThread().getName();
        return threadName != null && threadName.startsWith("IntentService[");
    }
}
