package com.yanqiu.otto.bus;

import com.yanqiu.otto.bus.subscribe.SubscriberMethod;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by mac on 18/6/26.
 */

public class EventHandler {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    EventHandler(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
    }

    public void handleEvent(Object event) {
        try {
            subscriberMethod.method.invoke(subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean isMatch(Class<?> event){
        return subscriberMethod.eventType == event;
    }


}
