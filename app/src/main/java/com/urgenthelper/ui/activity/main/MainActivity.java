package com.urgenthelper.ui.activity.main;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.urgenthelper.R;
import com.urgenthelper.ui.activity.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/6.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_toolbar)
    Toolbar mMainToolbar;

    private Unbinder bind;

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

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(bind!=null){
            bind.unbind();
        }
    }
}
