package com.wiggins.retrofit.http;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class RxUtils {

    public static void unSubscriptionIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public static CompositeSubscription getCompositeSubscription(CompositeSubscription subscription) {
        if (subscription == null || subscription.isUnsubscribed()) {
            return new CompositeSubscription();
        }
        return subscription;
    }
}
