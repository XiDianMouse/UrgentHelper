package com.urgenthelper.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import com.blankj.utilcode.utils.ToastUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        //监听WiFi的打开与关闭
        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
            switch (wifiState){
                case WifiManager.WIFI_STATE_DISABLED:
                    ToastUtils.showShortToast("wifi已断开");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    ToastUtils.showShortToast("wifi正在断开");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    ToastUtils.showShortToast("wifi正在连接");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
            }
        }

        /*
         这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
         .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
         在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
         当然刚打开wifi肯定还没有连接到有效的无线
        */
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                State state = networkInfo.getState();
                boolean isConnected = state == State.CONNECTED;// 当然，这边可以更精确的确定状态
//                if (ping()) {
//                    ToastUtils.showShortToast("当前WiFi连接可用");
//                } else {
//                    ToastUtils.showShortToast("当前WiFi连接不可用");
//                }
            }
        }

        /*
            这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。
            最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。
            这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
        */
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                ToastUtils.showShortToast(activeNetwork.isAvailable()+"~~~~"+activeNetwork.isConnected());
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        //当前WiFi连接可用,而wifi连接已经用了上面的广播进行了监听
                        //ToastUtils.showShortToast("当前WiFi连接可用");
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        ToastUtils.showShortToast("当前移动网络连接可用 ");
                    }
                } else {
                    ToastUtils.showShortToast("当前没有网络连接");
                }

            } else {   // not connected to the internet
                ToastUtils.showShortToast("当前没有网络连接");

            }

        }
    }


    private boolean ping() {
        ToastUtils.showShortToast("执行了");
        String result = null;
        try {
            String ip = "www.baidu.com";// 除非百度挂了，否则用这个应该没问题~
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping1次
            // 读取ping的内容，可不加。
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.i("TTT", "result content : " + stringBuffer.toString());
            // PING的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "successful~";
                return true;
            } else {
                result = "failed~ cannot reach the IP address";
            }
        } catch (IOException e) {
            result = "failed~ IOException";
        } catch (InterruptedException e) {
            result = "failed~ InterruptedException";
        } finally {
            Log.i("TTT", "result = " + result);
        }
        return false;
    }
}
