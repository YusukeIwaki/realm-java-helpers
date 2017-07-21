package io.github.yusukeiwaki.realm_java_helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmRecyclerViewAdapter<T extends RealmObject, S extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<S> {
    public interface Query<T extends RealmObject> {
        RealmResults<T> query(Realm realm);
    }

    private final Query<T> query;

    public RealmRecyclerViewAdapter(Query<T> query) {
        this.query = query;
    }

    private Realm realm;
    private Realm getOrCreateRealm() {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }
    private final RealmChangeListener<RealmResults<T>> listener = new RealmChangeListener<RealmResults<T>>() {
        private String previousResultsString;

        @Override
        public void onChange(RealmResults<T> results) {
            String currentResultString = results != null ? results.toString() : "";
            if (previousResultsString != null && previousResultsString.equals(currentResultString)) {
                return;
            }
            previousResultsString = currentResultString;

            notifyDataSetChanged();
        }
    };

    private @Nullable RealmResults<T> adapterData;

    private @NonNull RealmResults<T> getOrCreateAdapterData() {
        if (adapterData == null) {
            adapterData = query.query(getOrCreateRealm());
        }
        return adapterData;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (isDataValid()) {
            getOrCreateAdapterData().addChangeListener(listener);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (isDataValid()) {
            getOrCreateAdapterData().removeChangeListener(listener);
        }
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public int getItemCount() {
        //noinspection ConstantConditions
        return isDataValid() ? getOrCreateAdapterData().size() : 0;
    }

    public final @Nullable T getItem(int index) {
        //noinspection ConstantConditions
        return isDataValid() ? getOrCreateAdapterData().get(index) : null;
    }

    public final @NonNull RealmResults<T> getData() {
        return getOrCreateAdapterData();
    }

    private boolean isDataValid() {
        return getOrCreateAdapterData().isValid();
    }
}
