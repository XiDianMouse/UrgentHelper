package com.urgenthelper.ui.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.urgenthelper.R;
import com.urgenthelper.ui.activity.base.BaseActivity;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public class SplashActivity extends BaseActivity{
    @Override
    public int getLayoutId(){
        return R.layout.activity_splash;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                toMainActivity();
            }
        },500);
    }

    private void toMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.screen_zoom_in,R.anim.screen_zoom_out);
        finish();
    }
}
