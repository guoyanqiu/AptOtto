package com.yanqiu.otto.bus;

import com.yanqiu.otto.bus.subscribe.SubscriberInfo;

/**
 * Created by mac on 18/6/26.
 */

public interface SubscriberInfoFinder {
    SubscriberInfo getSubscriberInfo(Class<?> subscriberClass);
}
