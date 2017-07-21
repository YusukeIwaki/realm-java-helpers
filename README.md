# realm-java-helpers

Some utility classes for [realm-java](https://realm.io/jp/docs/java/latest/)

## Setup

```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // for RealmHelper
    compile 'com.github.YusukeIwaki.realm-java-helpers:realm-java-helper-bolts:2.0.0'
    compile 'com.parse.bolts:bolts-tasks:1.4.0' // You must include the latest version of bolts-tasks.

    // for RxRealmHelper
    compile 'com.github.YusukeIwaki.realm-java-helpers:realm-java-helper-rxjava2:2.0.0'
    compile 'io.reactivex.rxjava2:rxjava:2.1.1' // You must include any version of rxjava2


    // for RealmObjectObserver, RealmListObserver
    compile 'com.github.YusukeIwaki.realm-java-helpers:realm-java-observers:2.0.0'


    // for RealmRecyclerViewAdapter
    compile 'com.github.YusukeIwaki.realm-java-helpers:realm-java-adapters:2.0.0'
    compile 'com.android.support:recyclerview-v7:25.3.1' // You must include any version of recyclerview-v7.
}
```

Also it is required to setup the Realm properly like [this](https://realm.io/docs/java/latest/#installation), because this library doesn't include Realm.

## realm-java-helper-bolts / realm-java-helper-rxjava2

### Scoped reading

```
//bad example (causes resource leak)
//  User u = Realm.getDefaultInstance().where(User.class).equals("id", id).findFirst();
//  setTitle(u.getName());

User u = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<User>() {
    @Override
    public void execute(Realm realm) {
        return realm.where(User.class).equals("id", id).findFirst();
    }
}); // realm is automatically closed after Transaction!

setTitle(u.getName());
```

### executeTransaction with Bolts/RxJava

```
RealmHelper.getInstance().executeTransaction(realm -> {
    realm.createOrUpdateObjectFromJson(User.class, "{'id': 3, 'name': 'John'}");
}).onSuccess(task -> {
    Log.d(TAG, "done");
}); // realm is automatically closed after callback!
```

### copyFromRealm

Similar to `Realm#copyFromRealm`, the difference is that `RealmHelper#copyFromRealm()` automatically closes the realm after copying.


## realm-java-helper-observers

```
Query<User> query = new Query<User>() {
    @Override
    public RealmQuery<User> query(Realm realm) {
        return realm.where(User.class).equalTo("id", 3);
    }
};

RealmObjectObserver<User> observer = new RealmObjectObserver<User>(query);
observer.setOnUpdateListener(new RealmObjectObserver.OnUpdateListener<User>() {
    @Override
    public void onUpdateRealmObject(@Nullable User user) {
        if (user != null) modifyTitle(user.getName());
    }
});

observer.subscribe();

//realm is automatically closed when 'observer.unsubscribe()' is called.
```

Also, `RealmListObserver` is available for observing the result list of the query as well.

These classes might be very useful for enjoying something reactive, as below:

```
class SomeActivity extends Activity implements RealmObjectObserver.OnUpdateListener<User> {
    private static final Query<XX> query = ...;
    private RealmObjectObserver<XX> observer;

    @Override
    public void onCreate() {
        super.onCreate();

        observer = new RealmObjectObserver<User>(query);
        observer.setOnUpdateListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        observer.subscribe();
    }

    @Override
    public void onPause() {
        observer.unsubscribe();
        super.onPause();
    }

    @Override
    public void onUpdateRealmObject(@Nullable User user) {
        // enjoy something reactive here :)
    }
}
```


## RealmRecyclerViewAdapter

Define your own RecyclerView adapter as below:

```
class IssueListAdapter extends RealmRecyclerViewAdapter<Issue, IssueViewHolder> {
    public IssueListAdapter(RealmRecyclerViewAdapter.Query<Issue> query) {
        super(query);
    }

    @Override
    public IssueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ...        
    }

    @Override
    public void onBindViewHolder(IssueViewHolder holder, int position) {
        ...
    }

```


and just set it to the the RecyclerView.

```
recyclerView.setAdapter(new IssueListAdapter(new RealmRecyclerViewAdapter.Query<Issue>() {
    @Override
    public RealmResults<Issue> query(Realm realm) {
        return realm.where(Issue.class).equalTo("assignee", "me").findAllSorted("updated_at", Sort.DESCENDING);
    }
}));
```

then, the result is auto-updated only while the RecyclerView is active!

**Realm instance is automatically allocated and released internally** with this adapter class!
