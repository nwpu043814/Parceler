package com.lzh.compiler.parcelerdemo.dispatcheres;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lzh.compiler.parceler.annotation.Arg;
import com.lzh.compiler.parceler.annotation.Dispatcher;

/**
 * Created by haoge on 2016/11/25.
 */

@Dispatcher
public class TestService extends Service {
    @Arg
    String username;
    @Arg
    String password;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
