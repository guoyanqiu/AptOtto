/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanqiu.otto.bus.subscribe;


import java.lang.reflect.Method;

/**
 * Base class for generated subscriber meta info classes created by annotation processing.
 */
public class SubscriberInfo {

    //@Subscribe的宿主，比如MainActivity
    private final Class subscriberClass;

    private final SubscriberMethodInfo[] methodInfos;

    public SubscriberInfo(Class subscriberClass, SubscriberMethodInfo[] methodInfos) {
        this.subscriberClass = subscriberClass;
        this.methodInfos = methodInfos;
    }

    public Class getSubscriberClass() {
        return subscriberClass;
    }


    public SubscriberMethod createSubscriberMethod(String methodName, Class<?> paramType) {
        try {
            //根据方法名和参数获取subscriberClass的对应方法
            Method method = subscriberClass.getDeclaredMethod(methodName, paramType);
            return new SubscriberMethod(method, paramType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    //在需要的时候再形成SubscriberMethod
    public synchronized SubscriberMethod[] getSubscriberMethods() {
        int length = methodInfos.length;
        SubscriberMethod[] methods = new SubscriberMethod[length];
        for (int i = 0; i < length; i++) {
            SubscriberMethodInfo info = methodInfos[i];
            methods[i] = createSubscriberMethod(info.methodName, info.paramClass);
        }
        return methods;
    }

}
