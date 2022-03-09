package com.example.fireapp;

import com.example.fireapp.slice.LoginAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {

        super.onStart(intent);
        super.setMainRoute(LoginAbilitySlice.class.getName());

    }
}
