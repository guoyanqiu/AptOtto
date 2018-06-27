package com.yanqiu.otto.bus.subscribe;

import java.lang.reflect.Method;

/**
 * Created by mac on 18/6/26.
 */

public class SubscriberMethod {
    public final Method method;
    public final Class<?> eventType;
    public SubscriberMethod(Method method, Class<?> eventType) {
        this.method = method;
        this.eventType = eventType;
    }
}
