# realm-java-helpers

Some utility classes for [realm-java](https://realm.io/jp/docs/java/latest/)

## Setup

```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile 'com.github.yusukeiwaki:realm-java-helpers:1.2.1'
}
```

Also it is required to setup the Realm properly like [this](https://realm.io/docs/java/latest/#installation), because this library doesn't include Realm.

## Usage

### Scoped reading

```
//bad example (causes resource leak)
//  User u = Realm.getDefaultInstance().where(User.class).equals("id", id).findFirst();
//  setTitle(u.getName());

User u = RealmHelper.executeTransactionForRead(new RealmHelper.Transaction<User>() {
    @Override
    public void execute(Realm realm) {
        return realm.where(User.class).equals("id", id).findFirst();
    }
}); // realm is automatically closed after Transaction!

setTitle(u.getName());
```

### executeTransactionAsync with Bolts

```
RealmHelper.executeTransaction(realm -> {
    realm.createOrUpdateObjectFromJson(User.class, "{'id': 3, 'name': 'John'}");
}).onSuccess(task -> {
    Log.d(TAG, "done");
}); // realm is automatically closed after callback!
```

## copyFromRealm

Similar to `Realm#copyFromRealm`, the difference is that `RealmHelper#copyFromRealm()` automatically closes the realm after copying.

## RealmObjectObserver

```
RealmObjectObserver<User> observer = new RealmObjectObserver<User>() {
    @Override
    protected RealmQuery<User> query(Realm realm) {
        return realm.where(User.class).equalTo("id", 3);
    }

    @Override
    protected void onChange(User user) {
        modifyTitle(user.getName());
    }
};
observer.sub();

//realm is automatically closed when 'observer.unsub()' is called.
```

## RealmRecyclerViewAdapter

Define your own RecyclerView adapter as below:

```
class IssueListAdapter extends RealmRecyclerViewAdapter<Issue, IssueViewHolder> {
    public IssueListAdapter(RealmHelper.Query<Issue> query) {
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
recyclerView.setAdapter(new IssueListAdapter(new RealmHelper.Query<Issue>() {
    @Override
    public RealmResults<Issue> query(Realm realm) {
        return realm.where(Issue.class).equalTo("assignee", "me").findAllSorted("updated_at", Sort.DESCENDING);
    }
}));
```

then, the result is auto-updated only while the RecyclerView is active!

**Realm instance is automatically allocated and released internally** with this adapter class!
