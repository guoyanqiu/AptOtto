package com.yanqiu.otto.bus.subscribe;

/**
 * Created by mac on 18/6/26.
 */

public class SubscriberMethodInfo {

    final String methodName;//方法名
    final Class<?> paramClass;//方法参数的类型

    public SubscriberMethodInfo(String methodName, Class<?> paramClass) {
        this.methodName = methodName;
        this.paramClass = paramClass;
    }
}
