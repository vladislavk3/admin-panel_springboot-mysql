package fr.be.your.self.common;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import fr.be.your.self.model.Subscription;
import fr.be.your.self.model.User;

public class Utils {
	public static Subscription findSubscriptionUser(User user) {
		Subscription subscription = new Subscription();
		
		//Find the active subscription
		Optional<Subscription> subscriptionOptional = user.getSubscriptions().stream().filter(x -> x.isStatus()).findAny();
		
		if (subscriptionOptional.isPresent()) {
			subscription =  subscriptionOptional.get();
		} else { //Otherwise, find the last inactive subscription
			List<Subscription> subscriptions = user.getSubscriptions();
			if (subscriptions != null && subscriptions.size() > 0) {
				Subscription subsription = subscriptions.get(0);
				Date date = subsription.getSubscriptionEndDate();
				for (Subscription sub : subscriptions) {
					if (sub.getSubscriptionEndDate() == null) {
						continue;
					}
					if (sub.getSubscriptionEndDate().after(date)) {
						subsription = sub;
						date = sub.getSubscriptionEndDate();
					}
					
				}
				subscription = subsription;
			}
		}
		
		return subscription;
	}
}
