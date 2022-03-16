package com.example.fireapp.slice;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.example.fireapp.ResourceTable;
import com.example.fireapp.Utils;
import com.example.fireapp.orm.Token;
import com.example.fireapp.orm.User;
import com.example.fireapp.provider.TabPageSliderProvider;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;
import ohos.global.resource.NotExistException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {

    int id=0;
    private MapView mapView;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        if(intent!=null){
            IntentParams params = intent.getParams();
            id = (int) params.getParam("userid");
        }


        int[] icons = new int[3];
        icons[0] = ResourceTable.Media_ic_public_home;
        icons[1] = ResourceTable.Media_ic_gallery_map_all;
        icons[2] = ResourceTable.Media_ic_public_settings;
        //初始化Tablist
        TabList tablist = findComponentById(ResourceTable.Id_tab_list);
        tablist.removeAllComponents();
        String[] tablistTags = {"首页","地图","我的"};
        for(int i=0;i<tablistTags.length;i++){
            TabList.Tab tab = tablist.new Tab(this);
            tab.setText(tablistTags[i]);
            setTabImage(tab,icons[i]);
            tablist.addTab(tab);
        }

        //初始化pageslider
        List<Integer> layoutFileIds = new ArrayList<>();
        layoutFileIds.add(ResourceTable.Layout_ability_main_index);
        layoutFileIds.add(ResourceTable.Layout_ability_main_map);
        layoutFileIds.add(ResourceTable.Layout_ability_main_user);

        PageSlider pageSlider = findComponentById(ResourceTable.Id_page_slider);
        //pageSlider.removeAllComponents();
        pageSlider.setProvider(new TabPageSliderProvider(layoutFileIds,this));

        //tablist与pageslider联动
        tablist.addTabSelectedListener(new TabList.TabSelectedListener() {
            @Override
            public void onSelected(TabList.Tab tab) {
                int index = tab.getPosition();
                pageSlider.setCurrentPage(index);
                if(index==0){

                }else if(index==1){
                    initMapView();
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

        OrmContext context = Utils.getUserOrmContext(this);

        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userid",id);

        List<User> users = context.query(predicates);

        context.close();

        Text userid = findComponentById(ResourceTable.Id_userid);
        Text username = findComponentById(ResourceTable.Id_username);

        userid.setText(users.get(0).getUserid()+"");
        username.setText(users.get(0).getUserName());


        //修改密码
        Button changepwdbtn = findComponentById(ResourceTable.Id_changepwd_btn);
        changepwdbtn.setClickedListener(component -> {
            Intent intent = new Intent();
            intent.setParam("userid",id);
            present(new changepwdAbilitySlice(),intent);
        });

        //退出登录
        Button quitbtn = findComponentById(ResourceTable.Id_quit_btn);
        quitbtn.setClickedListener(component -> {
            OrmContext context1 = Utils.getTokenOrmContext(this);
            OrmPredicates predicates1 = new OrmPredicates(Token.class);
            predicates1.clear();
            context1.delete(predicates1);
            Intent intent1 = new Intent();
            present(new LoginAbilitySlice(),intent1);
        });



    }

    private void initMapView() {
        mapView = new MapView(this);

        mapView.onCreate(null);
        mapView.onResume();
        DirectionalLayout layout = findComponentById(ResourceTable.Id_map_directionlayout);

//        DirectionalLayout.LayoutConfig config = new DirectionalLayout.LayoutConfig(
//                DirectionalLayout.LayoutConfig.MATCH_PARENT, DirectionalLayout.LayoutConfig.MATCH_PARENT);
        DirectionalLayout.LayoutConfig config = new DirectionalLayout.LayoutConfig(
                DirectionalLayout.LayoutConfig.MATCH_PARENT, 1785);
        mapView.setLayoutConfig(config);
        layout.addComponent(mapView);


        AMap aMap = mapView.getMap();
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                // todo
            }
        });
    }


    @Override
    public void onActive() {
        super.onActive();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    void setTabImage(TabList.Tab tab, int image_id){
        try{
            tab.setIconElement(new PixelMapElement(getResourceManager().getResource(image_id)));
        }catch(IOException e){
            e.printStackTrace();
        }catch(NotExistException e) {
            e.printStackTrace();
        }
    }
}
