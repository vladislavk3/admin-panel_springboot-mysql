package fr.be.your.self.service;

import fr.be.your.self.model.SubscriptionType;

public interface SubscriptionTypeService extends BaseService<SubscriptionType, Integer>  {

	SubscriptionType saveOrUpdate(SubscriptionType subtype);
	Iterable<SubscriptionType> findAllByNameContainsIgnoreCase(String text);
	Boolean existsByName(String name);
	SubscriptionType findByName(String name);

}
