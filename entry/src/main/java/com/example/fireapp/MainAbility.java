package com.example.fireapp;

import com.example.fireapp.slice.LoginAbilitySlice;
import com.example.fireapp.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.bundle.IBundleManager;
import ohos.rpc.RemoteException;

public class MainAbility extends Ability {

    public static final int LOCATION_PEEMISSION_CODE = MainAbilitySlice.MY_LOCATION_PERMISSION;

    @Override
    public void onStart(Intent intent) {

        super.onStart(intent);

        try {
            System.out.println(getApplicationContext().getBundleManager().getBundleInfo(getBundleName(), 0).getAppId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.setMainRoute(LoginAbilitySlice.class.getName());

    }

    //调用requestPermissionsFromUser后的权限申请应答
    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case LOCATION_PEEMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == IBundleManager.PERMISSION_GRANTED){
                    Utils.showToast(this,"此应用已授权,可进行定位功能的初始化");
                }else{
                    Utils.showToast(this,"权限被拒绝");
                }
                return;
            }
        }
    }
}
