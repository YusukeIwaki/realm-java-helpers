package io.github.yusukeiwaki.realm_java_helper;

import android.arch.lifecycle.LiveData;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public abstract class RealmObjectLiveData<T extends RealmObject> extends LiveData<T> {
  private final AbstractRealmResultsObserver<T> observer;

  public RealmObjectLiveData() {
    this.observer = new AbstractRealmResultsObserver<T>() {
      @Override
      protected RealmQuery<T> query(Realm realm) {
        return RealmObjectLiveData.this.query(realm);
      }

      @Override
      protected RealmResults<T> execQuery(RealmQuery<T> realmQuery) {
        return RealmObjectLiveData.this.execQuery(realmQuery);
      }

      @Override protected RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
          private String previousResultString;

          @Override
          public void onChange(RealmResults<T> results) {
            T currentResult = extractObjectFromResults(results);
            String currentResultString = currentResult != null ? getComparationStringFor(currentResult) : "";
            if (previousResultString != null && previousResultString.equals(currentResultString)) {
              return;
            }
            previousResultString = currentResultString;
            postValue(observer.copyFromRealm(currentResult));
          }
        };
      }
    };
  }


  @Override protected void onActive() {
    super.onActive();
    observer.subscribe();
  }

  @Override protected void onInactive() {
    observer.unsubscribe();
    super.onInactive();
  }

  protected abstract RealmQuery<T> query(Realm realm);

  protected RealmResults<T> execQuery(RealmQuery<T> query) {
    return query.findAll();
  }

  protected T extractObjectFromResults(RealmResults<T> results) {
    return results.last(null);
  }

  protected String getComparationStringFor(T object) {
    return object.toString();
  }
}