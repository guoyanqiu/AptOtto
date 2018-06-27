package com.apt.otto;

import android.app.Activity;
import android.os.Bundle;

import com.yanqiu.otto.auto.SubscribeUtils;
import com.yanqiu.otto.bus.subscribe.Subscribe;
import com.yanqiu.otto.bus.subscribe.SubscriberInfo;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SubscriberInfo subscriberInfo = new SubscribeUtils().getSubscriberInfo(MainActivity.class);
//
        System.out.println(subscriberInfo.getSubscriberMethods().length + "注解的方法属");

    }

    @Subscribe
    public void sub1(Demo demo) {

    }

    @Subscribe
    public void sub2(String view) {
    }

    @Subscribe
    public void sub3(String view) {

    }
}
