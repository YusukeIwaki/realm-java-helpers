package io.github.yusukeiwaki.realm_java_helpers_sample;

import io.github.yusukeiwaki.realm_java_helper.RealmObjectObserver;
import io.github.yusukeiwaki.realm_java_helper.RealmObserverQuery;
import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * An observer that observes last User by name, reactively.
 */
public class LastUserObserver extends RealmObjectObserver<User> {
    private static final RealmObserverQuery<User> query = new RealmObserverQuery<User>() {
        @Override
        public RealmQuery<User> query(Realm realm) {
            return realm.where(User.class).sort("name");
        }
    };

    public LastUserObserver() {
        super(query);
    }

    @Override
    protected User extractObjectFromResults(RealmResults<User> results) {
        return results.last(null);
    }
}
