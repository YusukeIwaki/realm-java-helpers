package io.github.yusukeiwaki.realm_java_helpers_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helpers_bolts.RealmHelper;
import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RealmHelper.executeTransaction(new RealmHelper.Transaction() {
            @Override
            public Object execute(Realm realm) throws Exception {
                JSONArray users = new JSONArray();
                users.put(new JSONObject().put("id", 1).put("name", "Taro"));
                users.put(new JSONObject().put("id", 2).put("name", "Hanako"));
                realm.createOrUpdateAllFromJson(User.class, users);
                return null;
            }
        }).onSuccess(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                User u = RealmHelper.executeTransactionForRead(new RealmHelper.Transaction<User>() {
                    @Override
                    public User execute(Realm realm) throws Exception {
                        return realm.where(User.class).equalTo("id", 2).findFirst();
                    }
                });

                if (!u.name.equals("Hanako")) throw new RuntimeException();

                return null;
            }
        });
    }
}
