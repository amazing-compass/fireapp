package com.example.fireapp.slice;

import com.example.fireapp.ResourceTable;
import com.example.fireapp.Utils;
import com.example.fireapp.orm.User;
import com.example.fireapp.orm.UserDataBase;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.data.DatabaseHelper;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;

import java.util.List;

public class LoginAbilitySlice extends AbilitySlice implements Component.ClickedListener {

    Text register;
    TextField loginName;
    TextField loginPwd;
    Button login;


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_login);

        register = (Text) findComponentById(ResourceTable.Id_register);

        register.setClickedListener(component -> {
            Intent intent1 = new Intent();
            present(new RegisterAbilitySlice(), intent1);
        });

        loginName = (TextField)findComponentById(ResourceTable.Id_login_name_textfield);
        loginPwd = (TextField)findComponentById(ResourceTable.Id_login_pwd_textfield);
        login = (Button)findComponentById(ResourceTable.Id_login_btn);

        loginName.setText("");
        loginPwd.setText("");

        login.setClickedListener(this);



    }

    @Override
    public void onClick(Component component) {

        OrmContext context = getUserOrmContext();
        String userName = loginName.getText();
        String pwd = loginPwd.getText();

        if(userName == null || pwd == null){
            Utils.showToast(this,"账号或密码不能为空");
            return;
        }

        // 创建OrmPredicates对象，指定查询条件
        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userName",userName);

        List<User> users = context.query(predicates);

        context.close();

        System.out.println(users.get(0).getUserid()+"~~~~~~~~~~~~~~~~~~~~~~~");
        if(users.get(0).getPassword().equals(pwd)){
            Utils.showToast(this,"登录成功");
            //跳转到主页面
            Intent intent1 = new Intent();
            intent1.setParam("userid",users.get(0).getUserid());
            present(new MainAbilitySlice(),intent1);
        }else{
            Utils.showToast(this,"账号或密码不正确");
        }
    }

    private OrmContext getUserOrmContext() {
        DatabaseHelper helper = new DatabaseHelper(this);
        OrmContext context = helper.getOrmContext("UserDataBase", "user.db", UserDataBase.class);
        return context;
    }
}
