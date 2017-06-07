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

import com.baidu.mapapi.map.MapView;
import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.ItemEntry.MenuEntry;
import com.urgenthelper.R;
import com.urgenthelper.adapter.MenuAdapter;
import com.urgenthelper.app.MyApp;
import com.urgenthelper.listeners.OnItemClickListener;
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
    private MyApp mMyApp;

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
        mMyApp = (MyApp)getApplicationContext();
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

    @Override
    public void onResume(){
        super.onResume();
        mBmapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mBmapView.onPause();
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
                mMyApp.mLocationClient.start();
                break;
        }
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
