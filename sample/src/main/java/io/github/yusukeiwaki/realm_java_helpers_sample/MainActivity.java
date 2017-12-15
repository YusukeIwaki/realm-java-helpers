package io.github.yusukeiwaki.realm_java_helpers_sample;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.github.yusukeiwaki.realm_java_helper.RealmObjectObserver;
import io.github.yusukeiwaki.realm_java_helper.RealmRecyclerViewAdapter;
import io.github.yusukeiwaki.realm_java_helper.RxRealmHelper;
import io.github.yusukeiwaki.realm_java_helpers_sample.model.User;
import io.github.yusukeiwaki.realm_java_helpers_sample.view.UserViewHolder;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private RealmRecyclerViewAdapter<User, UserViewHolder> adapter;
    private LastUserObserver lastUserObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirstUserText();
        setupLastUserText();
        setupRecyclerView();
        setupButtons();
    }

    private void setupFirstUserText() {
        new FirstUserLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                TextView textView = findViewById(R.id.txt_first_user);
                if (user == null) {
                    textView.setText("~(o_x)~");
                } else {
                    textView.setText("firstUser.name = " + user.name);
                }
            }
        });
    }

    private void setupLastUserText() {
        lastUserObserver = new LastUserObserver();
        lastUserObserver.setOnUpdateListener(new RealmObjectObserver.OnUpdateListener<User>() {
            @Override
            public void onUpdateRealmObject(@Nullable User user) {
                TextView textView = findViewById(R.id.txt_last_user);
                if (user == null) {
                    textView.setText("~(x_x)~");
                } else {
                    textView.setText("lastUser.name = " + user.name);
                }
            }
        });
    }

    private void setupRecyclerView() {
        RealmRecyclerViewAdapter.Query<User> query = new RealmRecyclerViewAdapter.Query<User>() {
            @Override
            public RealmResults<User> query(Realm realm) {
                return realm.where(User.class).sort("name").findAll();
            }
        };

        adapter = new RealmRecyclerViewAdapter<User, UserViewHolder>(query) {
            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                return new UserViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(UserViewHolder holder, int position) {
                holder.bind(adapter.getItem(position));
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupButtons() {
        findViewById(R.id.btn_bolts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        JSONArray users = new JSONArray();
                        users.put(new JSONObject().put("id", 1).put("name", "Taro"));
                        users.put(new JSONObject().put("id", 2).put("name", "Hanako"));
                        realm.createOrUpdateAllFromJson(User.class, users);
                    }
                }).onSuccess(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        User u = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<User>() {
                            @Override
                            public User execute(Realm realm) throws Exception {
                                return realm.where(User.class).equalTo("id", 2).findFirst();
                            }
                        });

                        if (!u.name.equals("Hanako")) throw new RuntimeException();

                        return null;
                    }
                }).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            Log.e("MainActivity", e.getMessage(), e);
                        }
                        return null;
                    }
                });
            }
        });

        findViewById(R.id.btn_delete_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.where(User.class).sort("name").findAll().deleteFirstFromRealm();
                    }
                });
            }
        });

        findViewById(R.id.btn_delete_last).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.where(User.class).sort("name").findAll().deleteLastFromRealm();
                    }
                });
            }
        });

        findViewById(R.id.btn_rxjava2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxRealmHelper.getInstance().executeTransaction(new RxRealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Throwable {
                        JSONArray users = new JSONArray();
                        users.put(new JSONObject().put("id", 3).put("name", "Yusuke"));
                        users.put(new JSONObject().put("id", 4).put("name", "Iwaki"));
                        realm.createOrUpdateAllFromJson(User.class, users);
                    }
                }).flatMap(new Function() {
                    @Override
                    public Single apply(@NonNull Object o) throws Exception {
                        User u3 = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<User>() {
                            @Override
                            public User execute(Realm realm) throws Exception {
                                return realm.where(User.class).equalTo("id", 3).findFirst();
                            }
                        });

                        if (!u3.name.equals("Yusuke")) throw new RuntimeException();

                        return RxRealmHelper.getInstance().executeTransaction(new RxRealmHelper.Transaction() {
                            @Override
                            public void execute(Realm realm) throws Throwable {
                                JSONArray users = new JSONArray();
                                users.put(new JSONObject().put("id", 5).put("name", "Realm"));
                                realm.createOrUpdateAllFromJson(User.class, users);
                            }
                        });
                    }
                }).subscribe(new Consumer() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        User u5 = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<User>() {
                            @Override
                            public User execute(Realm realm) throws Exception {
                                return realm.where(User.class).equalTo("id", 5).findFirst();
                            }
                        });

                        if (!u5.name.equals("Realm")) throw new RuntimeException();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastUserObserver.subscribe();
    }

    @Override
    protected void onPause() {
        lastUserObserver.unsubscribe();
        super.onPause();
    }
}
