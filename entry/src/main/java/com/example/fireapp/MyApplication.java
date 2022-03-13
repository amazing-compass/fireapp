package com.example.fireapp;

import com.amap.api.maps.MapsInitializer;
import ohos.aafwk.ability.AbilityPackage;

public class MyApplication extends AbilityPackage {
    @Override
    public void onInitialize() {

        String key = "fdfe1484ba8c946fe6b9976b02ab33bd";

        // 地图
        MapsInitializer.setApiKey(key);

        super.onInitialize();
    }
}
