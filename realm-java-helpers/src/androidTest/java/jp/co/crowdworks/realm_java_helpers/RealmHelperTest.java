package jp.co.crowdworks.realm_java_helpers;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RealmHelperTest {

    @BeforeClass
    public static void initializeRealm() {
        Context context = InstrumentationRegistry.getContext();
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .name("test-"+System.currentTimeMillis()+".realm")
                .build();

        Realm.setDefaultConfiguration(config);

        insertTestUsers();
    }

    private static void insertTestUsers() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createOrUpdateObjectFromJson(TestUser.class, "{ id: 1, name: 'Nozomi' }");
                realm.createOrUpdateObjectFromJson(TestUser.class, "{ id: 2, name: 'Hikari' }");
                realm.createOrUpdateObjectFromJson(TestUser.class, "{ id: 3, name: 'Hayate' }");
            }
        });
        realm.close();
    }

    @Test
    public void testExecuteTransactionForRead() throws InterruptedException {
        final TestUser user = RealmHelper.executeTransactionForRead(new RealmHelper.Transaction<TestUser>() {
            @Override
            public TestUser execute(Realm realm) {
                TestUser u = realm.where(TestUser.class).equalTo("id", 2).findFirst();
                assertEquals("Hikari",u.getName());
                return u;
            }
        });
        assertEquals("Hikari",user.getName());

        // check if User is already detached from Realm
        //  by accessing it from another thread.
        new Thread() {
            @Override
            public void run() {
                assertEquals(2, user.getId());
            }
        }.start();
        Thread.sleep(1000);
    }

}