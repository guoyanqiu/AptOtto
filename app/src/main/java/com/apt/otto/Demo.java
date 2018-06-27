package com.apt.otto;


import com.yanqiu.otto.auto.SubscribeUtils;
import com.yanqiu.otto.bus.Bus;
import com.yanqiu.otto.bus.subscribe.Subscribe;

/**
 * Created by mac on 18/6/26.
 */

public class Demo {
    private static Bus bus = new Bus(new SubscribeUtils());

    public Demo() {
        bus.register(this);
    }

    @Subscribe
    public void subScribeDemo(String msg) {
        System.out.println("收到了消息:  " + msg);
    }

    public void destroy() {
        bus.unregister(this);
    }

    public static void main(String args[]) {
        Demo demo = new Demo();
        bus.post("Hello Apt Otto");
        demo.destroy();
        bus.post("Hello Apt Otto again");
    }
}
