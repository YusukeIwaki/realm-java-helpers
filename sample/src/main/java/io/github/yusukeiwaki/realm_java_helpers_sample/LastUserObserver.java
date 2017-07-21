package io.github.yusukeiwaki.realm_java_helpers_sample;

import io.github.yusukeiwaki.realm_java_helper.RealmObjectObserver;
import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * An observer that observes last User by name, reactively.
 */
public class LastUserObserver extends RealmObjectObserver<User> {
    private static final Query<User> query = new Query<User>() {
        @Override
        public RealmQuery<User> query(Realm realm) {
            return realm.where(User.class);
        }
    };

    public LastUserObserver() {
        super(query);
    }

    @Override
    protected RealmResults<User> execQuery(RealmQuery<User> query) {
        return query.findAllSorted("name");
    }

    @Override
    protected User extractObjectFromResults(RealmResults<User> results) {
        return results.last(null);
    }
}
