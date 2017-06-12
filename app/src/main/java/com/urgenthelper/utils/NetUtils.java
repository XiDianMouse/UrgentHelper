package com.urgenthelper.utils;

import com.blankj.utilcode.utils.NetworkUtils;
import com.blankj.utilcode.utils.ToastUtils;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/12.
 */

// 获取当前的网络状态 ：没有网络-0：WIFI网络1：移动网络-2
public class NetUtils {
    public static int getApnType(){
        if(NetworkUtils.isConnected()){
            //wifi在前,移动数据的判断在后面,wifi和移动网络同时连接时,优先使用wifi
            if(NetworkUtils.isWifiConnected()){
                if(NetworkUtils.isAvailableByPing()) {
                    ToastUtils.showLongToast("已通过wifi连接网络");
                    return 1;
                }
            }else if(NetworkUtils.getDataEnabled()){
                if(NetworkUtils.isAvailableByPing()) {
                    ToastUtils.showLongToast("已通过移动数据连接网络");
                    return 2;
                }
            }
        }
        ToastUtils.showLongToast("网络未连接,请连接网络");
        return 0;
    }
}
