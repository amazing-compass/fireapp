package com.example.fireapp.slice;

import com.example.fireapp.ResourceTable;
import com.example.fireapp.orm.User;
import com.example.fireapp.orm.UserDataBase;
import com.example.fireapp.provider.TabPageSliderProvider;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.*;
import ohos.data.DatabaseHelper;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;

import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {

    int id=0;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);



        if(intent!=null){
            IntentParams params = intent.getParams();
            id = (int) params.getParam("userid");
            System.out.println(id+"~~~~~~~~~~~~~~~~~~~~~~~");
        }


        //初始化Tablist
        TabList tablist = findComponentById(ResourceTable.Id_tab_list);
        String[] tablistTags = {"首页","地图","我的"};
        for(int i=0;i<tablistTags.length;i++){
            TabList.Tab tab = tablist.new Tab(this);
            tab.setText(tablistTags[i]);
            tablist.addTab(tab);
        }

        //初始化pageslider
        List<Integer> layoutFileIds = new ArrayList<>();
        layoutFileIds.add(ResourceTable.Layout_ability_main_index);
        layoutFileIds.add(ResourceTable.Layout_ability_main_map);
        layoutFileIds.add(ResourceTable.Layout_ability_main_user);

        PageSlider pageSlider = findComponentById(ResourceTable.Id_page_slider);
        pageSlider.setProvider(new TabPageSliderProvider(layoutFileIds,this));

        //tablist与pageslider联动
        tablist.addTabSelectedListener(new TabList.TabSelectedListener() {
            @Override
            public void onSelected(TabList.Tab tab) {
                int index = tab.getPosition();
                pageSlider.setCurrentPage(index);
                if(index==0){

                }else if(index==1){

                }else if(index==2){
                    inituser(pageSlider,id);

                }
            }

            @Override
            public void onUnselected(TabList.Tab tab) {}

            @Override
            public void onReselected(TabList.Tab tab) {}
        });

        pageSlider.addPageChangedListener(new PageSlider.PageChangedListener() {
            @Override
            public void onPageSliding(int i, float v, int i1) {}

            @Override
            public void onPageSlideStateChanged(int i) {}

            @Override
            public void onPageChosen(int i) {
                if(tablist.getSelectedTabIndex()!=i){
                    tablist.selectTabAt(i);
                }
            }
        });

        tablist.selectTabAt(0);

    }

    private void inituser(PageSlider pageSlider, int id) {

        OrmContext context = getUserOrmContext();

        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userid",id);

        List<User> users = context.query(predicates);

        context.close();

        Text userid = findComponentById(ResourceTable.Id_userid);
        Text username = findComponentById(ResourceTable.Id_username);

        userid.setText(users.get(0).getUserid()+"");
        username.setText(users.get(0).getUserName());



        Button changepwdbtn = findComponentById(ResourceTable.Id_changepwd_btn);

        changepwdbtn.setClickedListener(component -> {
            Intent intent = new Intent();
            intent.setParam("userid",id);
            //System.out.println(id+"~~~~~~~~~~~~~~~~~~~");
            present(new changepwdAbilitySlice(),intent);
        });


    }


    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private OrmContext getUserOrmContext() {
        DatabaseHelper helper = new DatabaseHelper(this);
        OrmContext context = helper.getOrmContext("UserDataBase", "user.db", UserDataBase.class);
        return context;
    }
}
