# realm-java-helpers

Some utility classes for [realm-java](https://realm.io/jp/docs/java/latest/)

## Setup

```
repositories {
    maven {
        url 'https://github.com/YusukeIwaki/realm-java-helpers/raw/master/repo'
    }
}

dependencies {
    compile 'jp.co.crowdworks:realm-java-helpers:0.0.4'
}
```


## Usage

### Scoped reading

```
//bad example (causes resource leak)
//  User u = Realm.getDefaultInstance().where(User.class).equals("id", id).findFirst();
//  setTitle(u.getName());

RealmHelper.executeTransactionForRead(new RealmHelper.Transaction<User>() {
    @Override
    public void execute(Realm realm) {
        return realm.where(User.class).equals("id", id).findFirst();
    }
}); // realm is automatically closed after Transaction!

setTitle(u.getName());
```

### executeTransactionAsync with Rx

```
RealmHelper.rxExecuteTransactionAsync(realm -> {
    realm.createOrUpdateObjectFromJson(User.class, "{'id': 3, 'name': 'John'}");
}).subscribe((xx) -> {
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
