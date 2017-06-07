package com.urgenthelper.app;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.blankj.utilcode.utils.Utils;


/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public class MyApp extends Application{
    @Override
    public void onCreate(){
        super.onCreate();
        Utils.init(this);
        SDKInitializer.initialize(getApplicationContext());
    }
}
