package io.github.yusukeiwaki.realm_java_helper;

import android.arch.lifecycle.LiveData;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public abstract class RealmListLiveData<T extends RealmObject> extends LiveData<List<T>> {
  private final AbstractRealmResultsObserver<T> observer;

  public RealmListLiveData() {
    this.observer = new AbstractRealmResultsObserver<T>() {
      @Override protected RealmResults<T> queryItems(Realm realm) {
        return execQuery(query(realm));
      }

      @Override protected RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
          private String previousResultsString;

          @Override
          public void onChange(RealmResults<T> results) {
            String currentResultsString = getComparationStringFor(results);
            if (previousResultsString != null && previousResultsString.equals(currentResultsString)) {
              return;
            }
            previousResultsString = currentResultsString;
            postValue(observer.copyFromRealm(results));
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

  protected String getComparationStringFor(RealmResults<T> results) {
    return results.toString();
  }
}