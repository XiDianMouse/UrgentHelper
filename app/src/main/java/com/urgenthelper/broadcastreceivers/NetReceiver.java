package com.urgenthelper.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.urgenthelper.utils.NetUtils;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/11.
 */

public class NetReceiver extends BroadcastReceiver{

    public NetReceiver(){

    }

    @Override
    public void onReceive(Context context,Intent intent){
        NetUtils.getApnType();
    }
}
