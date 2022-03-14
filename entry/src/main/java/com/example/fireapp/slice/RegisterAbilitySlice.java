package com.example.fireapp.slice;

import com.example.fireapp.ResourceTable;
import com.example.fireapp.Utils;
import com.example.fireapp.orm.User;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.TextField;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;

import java.util.List;

public class RegisterAbilitySlice extends AbilitySlice implements Component.ClickedListener{

    TextField registername;
    TextField registerpwd;
    Button registerbtn;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_register);
        registername = findComponentById(ResourceTable.Id_register_name_textfield);
        registerpwd = findComponentById(ResourceTable.Id_register_pwd_textfield);
        registerbtn = findComponentById(ResourceTable.Id_register_btn);
        registername.setText("");
        registerpwd.setText("");

        registerbtn.setClickedListener(this);
    }

    @Override
    public void onClick(Component component) {

        OrmContext context = Utils.getUserOrmContext(this);

        String UserName = registername.getText();
        String UserPwd = registerpwd.getText();

        if(UserName == null || UserPwd == null){
            Utils.showToast(this,"账号或密码不能为空!");
            return;
        }

        // 创建OrmPredicates对象，指定查询条件
        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userName",UserName);

        List<User> users = context.query(predicates);

        if(users.size()!=0){
            Utils.showToast(this,"该用户已注册");
            return;
        }

        User user = new User();
        user.setUserName(UserName);
        user.setPassword(UserPwd);

        boolean isSuccessed = context.insert(user);
        if (!isSuccessed) {
            Utils.showToast(this, "注册失败!");
            return;
        }
        // 提交操作
        isSuccessed = context.flush();
        if (!isSuccessed) {
            Utils.showToast(this, "注册失败!");
            return;
        }
        Utils.showToast(this, "注册成功!");
        // 使用完OrmContext对象后要及时关闭
        context.close();

        //跳转到登录页面
        Intent intent1 = new Intent();
        present(new LoginAbilitySlice(),intent1);

    }


}
