package io.github.yusukeiwaki.realm_java_helpers_sample.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    @PrimaryKey
    public long id;
    public String name;
}
