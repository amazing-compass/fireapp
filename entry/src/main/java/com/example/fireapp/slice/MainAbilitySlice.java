package com.example.fireapp.slice;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
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
import ohos.agp.window.dialog.ToastDialog;
import ohos.bundle.IBundleManager;
import ohos.data.orm.OrmContext;
import ohos.data.orm.OrmPredicates;
import ohos.global.resource.NotExistException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.Location;
import ohos.location.Locator;
import ohos.location.LocatorCallback;
import ohos.location.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice implements LocationSource {

    int id=0;
    User user;
    private MapView mapView;
    private AMap aMap;

    // 地理定位“总管家”
    private Locator locator;
    // 定位回调
    private MylocatorCallBack myLocatorCallback = new MylocatorCallBack() ;


    private static final String PERM_LOCATION = "ohos.permission.LOCATION";
    private OnLocationChangedListener mListener = null;
    public static final int MY_LOCATION_PERMISSION = 6;
    //日志信息
    private static final HiLogLabel hilog = new HiLogLabel(HiLog.DEBUG, 0x0000, "APP_LOG");


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        if(intent!=null){
            IntentParams params = intent.getParams();
            id = (int) params.getParam("userid");
        }

        OrmContext context = Utils.getUserOrmContext(this);

        OrmPredicates predicates = new OrmPredicates(User.class)
                .equalTo("userid",id);

        List<User> users = context.query(predicates);
        user = users.get(0);
        context.close();


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
        pageSlider.setProvider(new TabPageSliderProvider(layoutFileIds,this));

        //tablist与pageslider联动
        tablist.addTabSelectedListener(new TabList.TabSelectedListener() {
            @Override
            public void onSelected(TabList.Tab tab) {
                int index = tab.getPosition();
                pageSlider.setCurrentPage(index);
                if(index==0){
                    initIndex(user);
                }else if(index==1){
                    initMapView();
                }else if(index==2){
                    inituser(user,id);
                }
            }

            @Override
            public void onUnselected(TabList.Tab tab) {}

            @Override
            public void onReselected(TabList.Tab tab) {}
        });

        tablist.selectTabAt(0);
        pageSlider.setSlidingPossible(false);

    }

    private void initMapView() {

        //1.地图UI加载
        mapView = new MapView(this);


        DirectionalLayout layout = findComponentById(ResourceTable.Id_map_directionlayout);
        DirectionalLayout.LayoutConfig config = new DirectionalLayout.LayoutConfig(
                DirectionalLayout.LayoutConfig.MATCH_PARENT, 1785);
        mapView.setLayoutConfig(config);
        layout.addComponent(mapView);

        mapView.onCreate(null);
        mapView.onResume();

        //申请定位权限
        requestLocationPermission();

        aMap = mapView.getMap();

        requestLocationPermission();

        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

            }
        });

        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                // todo
            }
        });
    }

    //2.获取定位权限
    public void requestLocationPermission(){
        if (verifySelfPermission("ohos.permission.LOCATION") != IBundleManager.PERMISSION_GRANTED ){

            if (canRequestPermission("ohos.permission.LOCATION") ){
                //是否可以申请"位置"的动态弹框授权(首次申请或者用户未选择禁止且不再提示)
                requestPermissionsFromUser(
                        new String[]{"ohos.permission.LOCATION","ohos.permission.LOCATION_IN_BACKGROUND"}, MY_LOCATION_PERMISSION
                );
            }else{
                //提示用户进入设置界面进行授权
                new ToastDialog(getContext()).setText("请进入手机系统【设置】中，开启应用的定位权限").show();
            }
        }else{
            //此应用已授予权限
            new ToastDialog(getContext()).setText("此应用已授权,可进行定位功能的初始化").show();
            HiLog.info(hilog,"此应用已授权，开始定位" );
            //通过开启定位开关，显示定位蓝点
            isOpenLocationSwitch();
        }
    }

    // 3.是否开启"位置信息"开关
    public void isOpenLocationSwitch(){
        HiLog.info(hilog,"获取定位器" );
        locator = new Locator(getContext());

        //判断“位置信息”开关是否已开启
        if(locator.isLocationSwitchOn()){
            HiLog.info(hilog,"[位置信息]开关已开启" );
            onLocationChangeListener();
        } else {
            new ToastDialog(getContext()).setText("请在控制中心打开【位置信息】开关，以便获取当前所在位置").show();
            //结束定位
            locator.stopLocating(myLocatorCallback);
        }
    }

    //4.获取手机定位
    public void onLocationChangeListener() {

        HiLog.info(hilog,"定位功能开始" );
        // 定位类型，选择"导航场景"
        RequestParam requestParam = new RequestParam(RequestParam.SCENE_NAVIGATION);

        HiLog.info(hilog,"开启定位" );
        // 开启定位
        locator.startLocating(requestParam,myLocatorCallback);

        // 绘制蓝点
        HiLog.info(hilog,"绘制蓝点");
        getMyLocationStyle();
    }

    // 5.将定位点绘制成蓝点
    private void getMyLocationStyle(){
        HiLog.info(hilog,"绘制定位小蓝点" );
        MyLocationStyle locationStyle = new MyLocationStyle();//初始化定位蓝点样式
        //locationStyle.anchor(0.0f,1.0f);
        aMap.setMyLocationStyle(locationStyle);
        locationStyle.interval(1000);//设置连续定位模式下的的定位间隔，值在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒
        //locationStyle.showMyLocation(false);
        //locationStyle.strokeColor(Color.BLUE); //圆圈的边框颜色为蓝色
        //locationStyle.radiusFillColor(Color.LTGRAY); //圆圈的填充颜色
        //locationStyle.strokeWidth(40);
        aMap.setMyLocationStyle(locationStyle);
        //设置定位蓝点的style

        aMap.getUiSettings().setMyLocationButtonEnabled(false); // 设置默认定位按钮是否显示，非必须设置

        aMap.setLocationSource(this);

        aMap.setMyLocationEnabled(true);//设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false
    }

    /**
     * 激活定位器
     * @param onLocationChangedListener
     * LocationSource接口实现方法
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }


    /**
     * 关闭定位器
     * LocationSource接口实现方法
     */
    @Override
    public void deactivate() {
        mListener = null;
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



    public class MylocatorCallBack implements LocatorCallback {

        //获取定位数据
        @Override
        public void onLocationReport(final Location location) {

            HiLog.info(hilog,"AMap定位，Latitude为：" + location.getLatitude()+",Longitude为：" + location.getLongitude());
            getUITaskDispatcher().asyncDispatch(() -> {
                getLocation(location);
            });
        }

        @Override
        public void onStatusChanged(int i) {

        }

        @Override
        public void onErrorReport(int i) {

        }
    }

    public void getLocation(Location location){

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
        HiLog.info(hilog,"AMap定位，Latitude为：" + location.getLatitude()+",Longitude为：" + location.getLongitude());

        //绘制定位图标-小蓝点的样式
        getMyLocationStyle();
        //通过监听器OnLocationChangedListener，触发小蓝点在地图上的出现
        mListener.onLocationChanged(location);
    }


    private void inituser(User user, int id) {

        Text userid = findComponentById(ResourceTable.Id_userid);
        Text username = findComponentById(ResourceTable.Id_username);
        userid.setText(user.getUserid()+"");
        username.setText(user.getUserName());

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
    private void initIndex(User user) {
        Text index_username = findComponentById(ResourceTable.Id_index_username);
        index_username.setText(user.getUserName());

        Image index_image = findComponentById(ResourceTable.Id_index_image);
        index_image.setCornerRadius(40);
    }


    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
}
