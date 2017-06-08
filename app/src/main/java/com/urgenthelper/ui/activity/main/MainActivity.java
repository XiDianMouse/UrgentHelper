package com.urgenthelper.ui.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.ItemEntry.MenuEntry;
import com.urgenthelper.R;
import com.urgenthelper.adapter.MenuAdapter;
import com.urgenthelper.listeners.OnItemClickListener;
import com.urgenthelper.tcp.TcpController;
import com.urgenthelper.ui.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public class MainActivity extends BaseActivity implements OnItemClickListener<MenuEntry> {

    @BindView(R.id.main_toolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.main_drawlayout)
    DrawerLayout mMainDrawlayout;
    @BindView(R.id.menu_recyclerview)
    RecyclerView mMenuRecyclerview;
    @BindView(R.id.bmapView)
    MapView mBmapView;

    private Unbinder bind;
    private BaiduMap mBaiduMap;
    protected LocationClient mLocationClient;
    private LocationResponse mLocationResponse;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ButterKnife.bind(this);
        setSupportActionBar(mMainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        initMenuView();
        mBaiduMap = mBmapView.getMap();
        mLocationResponse = new LocationResponse(this);
        //百度定位
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(mLocationResponse);    //注册监听函数
        initLocation();//初始化
    }

    private void initMenuView() {
        mMenuRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        MenuAdapter menuAdapter = new MenuAdapter(this);
        menuAdapter.addItems(prepareMenuItems());
        menuAdapter.setItemClickListener(this);
        mMenuRecyclerview.setAdapter(menuAdapter);
    }

    private List<MenuEntry> prepareMenuItems() {
        List<MenuEntry> menuItems = new ArrayList<>();
        menuItems.add(new MenuEntry(R.drawable.theme_color, "个性换肤"));
        menuItems.add(new MenuEntry(R.drawable.about_us, "关于我们"));
        menuItems.add(new MenuEntry(R.drawable.setting, "设置"));
        menuItems.add(new MenuEntry(R.drawable.feedback, "意见反馈"));
        menuItems.add(new MenuEntry(R.drawable.exit_app, "退出"));
        return menuItems;
    }

    //综合定位功能指的是根据用户实际需求，返回用户当前位置的基础定位服务,包含GPS和网络定位(WiFi定位和基站
    //定位)功能。基本定位功能同时还支持位置描述信息功能，离线定位功能，位置提醒功能和位置语义化功能。
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        /*
        * 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
        *低功耗定位模式：这种定位模式下，不会使用GPS进行定位，只会使用网络定位（WiFi定位和基站定位）；
        *仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
        * */
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onStart(){
        super.onStart();
        //开始定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mBmapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mBmapView.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
    }

    @OnClick({R.id.fl_title_menu,R.id.login,R.id.urgent_alarm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fl_title_menu:
                mMainDrawlayout.openDrawer(GravityCompat.START);
                break;
            case R.id.login:
                ToastUtils.showShortToast("暂未开发");
                break;
            case R.id.urgent_alarm:
                ToastUtils.showShortToast("正在为你开启紧急救援");
                alaremHelp();
                break;
        }
    }

    private void alaremHelp(){
        //确定TCP套接字是否存在且连接是否失效，否则建立套接字。
        TcpController instance = TcpController.getInstance();
        if(instance!=null && instance.mSocket!=null){
            try{
                instance.mSocket.sendUrgentData(0xFF);//发送心跳包
            }catch(Exception e){
                new Thread(instance).start();//重新建立套接字
            }
        }else if(instance!=null){
            new Thread(instance).start();//建立套接字
        }
        //开始定位
        mLocationClient.start();
    }

    @Override
    public void onClick(MenuEntry entry) {
        switch (entry.mIconResId) {
            case R.drawable.theme_color:
                ToastUtils.showShortToast("尚未开发");
                break;
            case R.drawable.about_us:
                startActivity(new Intent(this, AboutUsActivity.class));
                break;
            case R.drawable.setting:
                ToastUtils.showShortToast("尚未开发");
                break;
            case R.drawable.feedback:
                ToastUtils.showShortToast("尚未开发");
                //startActivity(new Intent(this,FeedbackActivity.class));
                break;
            case R.drawable.exit_app:
                ToastUtils.showShortToast("尚未开发");
                //killAll();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBmapView.onDestroy();
        if (bind != null) {
            bind.unbind();
        }
    }
}
