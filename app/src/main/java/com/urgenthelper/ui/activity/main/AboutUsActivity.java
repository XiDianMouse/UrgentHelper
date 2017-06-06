package com.urgenthelper.ui.activity.main;

import android.os.Bundle;
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

public class AboutUsActivity extends BaseActivity {

    @BindView(R.id.aboutus_toolbar)
    Toolbar mAboutusToolbar;

    private Unbinder binder;

    @Override
    public int getLayoutId() {
        return R.layout.activity_aboutus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = ButterKnife.bind(this);
        setToolBar(mAboutusToolbar,"关于我们");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(binder!=null){
            binder.unbind();
        }
    }
}
