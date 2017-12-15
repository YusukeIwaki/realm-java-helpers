package io.github.yusukeiwaki.realm_java_helpers_sample;

import io.github.yusukeiwaki.realm_java_helper.RealmObjectLiveData;
import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * An observer that observes last User by name, reactively.
 */
public class FirstUserLiveData extends RealmObjectLiveData<User> {
    @Override
    protected RealmQuery<User> query(Realm realm) {
        return realm.where(User.class).sort("name");
    }

    @Override
    protected User extractObjectFromResults(RealmResults<User> results) {
        return results.first(null);
    }
}
