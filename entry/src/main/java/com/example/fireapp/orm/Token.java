package com.example.fireapp.orm;


import ohos.data.orm.OrmObject;
import ohos.data.orm.annotation.Entity;
import ohos.data.orm.annotation.PrimaryKey;

@Entity(tableName="token")
public class Token extends OrmObject {

    @PrimaryKey(autoGenerate = true)
    private int useraccount;


    public Token(int useraccount) {
        this.useraccount = useraccount;
    }

    public Token() {
    }

    public int getUseraccount() {
        return useraccount;
    }

    public void setUseraccount(int useraccount) {
        this.useraccount = useraccount;
    }
}
