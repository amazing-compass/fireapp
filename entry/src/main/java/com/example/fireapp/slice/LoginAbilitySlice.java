package com.example.fireapp.slice;

import com.example.fireapp.ResourceTable;
import com.example.fireapp.Utils;
import com.example.fireapp.orm.Token;
import com.example.fireapp.orm.User;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
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

        //查询token数据库中的值：如果数据库不为空，静默登录；如果数据库为空，需要重新登录
        OrmContext context = Utils.getTokenOrmContext(this);
        OrmPredicates predicates = new OrmPredicates(Token.class);
        predicates.clear();

        //context要及时关闭
        List<Token> Tokens = context.query(predicates);
        context.close();


        if(Tokens.isEmpty()){
            //如果数据库为空
            register = findComponentById(ResourceTable.Id_register);

            //注册账号按钮的响应事件
            register.setClickedListener(component -> {
                Intent intent1 = new Intent();
                present(new RegisterAbilitySlice(), intent1);
            });

            loginName = findComponentById(ResourceTable.Id_login_name_textfield);
            loginPwd = findComponentById(ResourceTable.Id_login_pwd_textfield);
            login = findComponentById(ResourceTable.Id_login_btn);

            loginName.setText("");
            loginPwd.setText("");

            login.setClickedListener(this);

        }else{
            //如果数据库不为空
            //跳转到主页面
            Intent intent1 = new Intent();
            intent1.setParam("userid", Tokens.get(0).getUseraccount());
            present(new MainAbilitySlice(),intent1);
        }
    }




    @Override
    public void onClick(Component component) {

        OrmContext context = Utils.getUserOrmContext(this);
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
            OrmContext context1 = Utils.getTokenOrmContext(this);
            Token token = new Token(users.get(0).getUserid());
            context1.insert(token);
            context1.flush();
            context1.close();

            //跳转到主页面
            Intent intent1 = new Intent();
            intent1.setParam("userid",users.get(0).getUserid());
            present(new MainAbilitySlice(),intent1);
        }else{
            Utils.showToast(this,"账号或密码不正确");
        }
    }


}
