package com.yanqiu.otto.bus;


import com.yanqiu.otto.bus.subscribe.SubscriberInfo;
import com.yanqiu.otto.bus.subscribe.SubscriberMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by mac on 18/6/26.
 */

public class Bus {
    private SubscriberInfoFinder subscriberInfoFinder;
    private final Map<Class<?>, CopyOnWriteArrayList<EventHandler>> eventHandlerMap;
    public Bus(SubscriberInfoFinder subscriberInfoFinder) {
        eventHandlerMap = new HashMap<>();
        this.subscriberInfoFinder = subscriberInfoFinder;
    }

    public void post(Object event) {
        Class<?> targetType = event.getClass();
        List<EventHandler> eventHandlers = eventHandlerMap.get(targetType);
        if (eventHandlers != null) {
            for (EventHandler eventHandler : eventHandlers) {
                if (eventHandler.isMatch(targetType)) {
                    eventHandler.handleEvent(event);
                }
            }
        }

    }

    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();

        SubscriberInfo subscriberInfo = subscriberInfoFinder.getSubscriberInfo(subscriberClass);
        SubscriberMethod[] subscriberMethods = subscriberInfo.getSubscriberMethods();
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        //搜索subscriber对象的事件类型
        Class<?> eventType =
                subscriberMethod.eventType;

        EventHandler eventHandler = new EventHandler(subscriber, subscriberMethod);
        CopyOnWriteArrayList<EventHandler> eventHandlers = eventHandlerMap.get(eventType);
        if (eventHandlers == null) {
            eventHandlers = new CopyOnWriteArrayList<>();
            eventHandlerMap.put(eventType, eventHandlers);
        }

        eventHandlers.add(eventHandler);
    }

    public synchronized void unregister(Object subscriber) {
        for(Class<?> eventType:eventHandlerMap.keySet()){
            List<EventHandler> eventHandlers = eventHandlerMap.get(eventType);
            if (eventHandlers != null) {
                int size = eventHandlers.size();
                for (int i = 0; i < size; i++) {
                    EventHandler eventHandler = eventHandlers.get(i);
                    if (eventHandler.subscriber == subscriber) {
                        eventHandlers.remove(i);
                        i--;
                        size--;
                    }
                }
            }
        }
    }

}
