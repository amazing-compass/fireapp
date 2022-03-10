package com.example.fireapp;

import com.example.fireapp.slice.LoginAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.rpc.RemoteException;

public class MainAbility extends Ability {
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
}
