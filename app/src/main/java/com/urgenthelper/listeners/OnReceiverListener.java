package com.urgenthelper.listeners;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/7.
 */

public interface OnReceiverListener<T> {
    void onReceive(T data,T description);
}
