package com.example.fireapp.slice;

import com.example.fireapp.ResourceTable;
import com.example.fireapp.Utils;
import com.example.fireapp.orm.User;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;
import ohos.data.rdb.ValuesBucket;

import java.util.List;

public class changepwdAbilitySlice extends AbilitySlice implements Component.ClickedListener {


    TextField oldpwd;          //旧密码
    TextField newpwd;          //新密码
    Button changebtn;          //修改密码按钮
    int id;                    //用户id
    List<User> users;          //数据库查询队列

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_changepwd);

        Text username = findComponentById(ResourceTable.Id_change_username);
        oldpwd = findComponentById(ResourceTable.Id_old_pwd_textfield);
        newpwd = findComponentById(ResourceTable.Id_new_pwd_textfield);
        changebtn = findComponentById(ResourceTable.Id_change_btn);

        oldpwd.setText("");
        newpwd.setText("");

        //将userid传递到该页面
        IntentParams params = intent.getParams();
        id = (int)params.getParam("userid");

        //查询数据库中userid==id的用户
        OrmContext context = Utils.getUserOrmContext(this);
        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userid",id);
        users = context.query(predicates);

        context.close();

        username.setText(users.get(0).getUserName());


        changebtn.setClickedListener(this);
    }



    //修改用户的密码
    @Override
    public void onClick(Component component) {
        if(users.get(0).getPassword().equals(oldpwd.getText())){
            OrmContext context = Utils.getUserOrmContext(this);
            OrmPredicates predicates = new OrmPredicates(User.class)
                    .equalTo("userid",id);

            ValuesBucket values = new ValuesBucket();
            values.putString("password",newpwd.getText());

            int count = context.update(predicates,values);
            System.out.println(count+"~~~~~~~~~~~~~~~~~~~~~~");
            boolean isSuccessed = context.flush();
            Utils.showToast(this,"密码修改成功");
            context.close();
        }else{
            Utils.showToast(this,"原密码不正确");
        }
    }
}
