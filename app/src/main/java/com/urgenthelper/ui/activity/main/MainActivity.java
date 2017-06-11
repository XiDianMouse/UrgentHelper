package com.urgenthelper.ui.activity.main;

import android.content.Intent;
import android.content.IntentFilter;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.ItemEntry.MenuEntry;
import com.urgenthelper.R;
import com.urgenthelper.adapter.MenuAdapter;
import com.urgenthelper.broadcastreceivers.NetReceiver;
import com.urgenthelper.listeners.MyLocationListener;
import com.urgenthelper.listeners.OnItemClickListener;
import com.urgenthelper.sms.PhoneControl;
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

    private Unbinder bind;

    //百度地图
    @BindView(R.id.bmapView)
    MapView mBmapView;
    public BaiduMap mBaiduMap;

    public PhoneControl mPhoneControl;

    /****************************百度定位*****************************************
     * 目前系统自带的网络定位服务精度低，且服务不稳定、精度低，并且从未来的趋势看，基站定位是不可控的
     * （移动公司随时可能更改基站编号以垄断定位服务），而Wi-Fi定位则不然，它是一种精度更高、不受管制的定位方法。
     * 国内其它使用Wi-Fi定位的地图软件，Wi-Fi定位基本不可用，百度的定位服务量化指标优秀，网络接口返回速度快
     * （服务端每次定位响应时间50毫秒以内），平均精度70米，其中Wi-Fi精度40米左右，基站定位精度200米左右，
     * 覆盖率98%，在国内处于一枝独秀的地位。
     */
    protected LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private BitmapDescriptor mBitmapDescriptor;

    private NetReceiver mNetReceiver;

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
        initMapView();
        initLocation();//定位
        mPhoneControl = new PhoneControl(this);
    }

    private void initMenuView() {
        mMenuRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        MenuAdapter menuAdapter = new MenuAdapter(this);
        menuAdapter.addItems(prepareMenuItems());
        menuAdapter.setItemClickListener(this);
        mMenuRecyclerview.setAdapter(menuAdapter);

        mNetReceiver = new NetReceiver();
        registerReceiver(mNetReceiver,myIntentFilter());
    }

    /*
    *   ①LatLng类只有两个属性 用来描述地理坐标基本数据结构(纬度,经度)
        ②MapStatus.Builder是地图状态构造器 target设置地图中心点 zoom设置地图缩放级别
            build()方法生成一个MapStatus地图状态对象
        ③MapStatusUpdateFactory,生成地图状态将要发生的变化描述 newMapStatus()方法设置地图新状态
            MapStatus就是地图状态对象  MapStatusUpdate对象是生成的地图变化描述
        ④BaiduMap对象用setMapStatus()方法改变地图状态(参数的含义是根据描述去改变)

        其实听起来有些难理解  通过面向对象的思维 我这样理解 :
            地图要改变-->地图操作者去改变-->按照客户要求(可能有新的中心点,缩放度)生成一个地图状态
                -->地图操作者看不懂状态需要人描述一下状态的含义-->地图操作者根据描述去开始操作地图
        对应的类的关系 :
        MapView要改变-->用BaiduMap去操作-->根据MapStatus的状态改变-->
            BaiduMap看不懂状态MapStatus  需要MapStatusUpdate去描述-->BaiduMap通过描述去改变-->
            MapView改变
    * */
    private void initMapView(){
        //通过BaiduMap对象设置地图的一些属性
        mBaiduMap = mBmapView.getMap();
        MapStatus mapStatus = new MapStatus.Builder().zoom(18).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
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
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
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
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mMyLocationListener = new MyLocationListener(this);
        mLocationClient.registerLocationListener(mMyLocationListener);    //注册监听函数
        mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon);
        MyLocationConfiguration configuration = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING,true,mBitmapDescriptor);
        mBaiduMap.setMyLocationConfiguration(configuration);
    }

    private IntentFilter myIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");

        return intentFilter;
    }

    @Override
    protected void onStart(){
        super.onStart();
        //开始定位
        if(!mLocationClient.isStarted()){
            mBaiduMap.setMyLocationEnabled(true);
            mLocationClient.start();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mBmapView.onResume();
        //开始定位
        if(!mLocationClient.isStarted()){
            mBaiduMap.setMyLocationEnabled(true);
            mLocationClient.start();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        mBmapView.onPause();
        //停止定位
        if(mLocationClient.isStarted()){
            mBaiduMap.setMyLocationEnabled(false);
            mLocationClient.stop();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        //停止定位
        if(mLocationClient.isStarted()) {
            mBaiduMap.setMyLocationEnabled(false);
            mLocationClient.stop();
        }
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
        //开始紧急报警定位
        mMyLocationListener.cmdStyle = 1;
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
