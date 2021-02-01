package fr.be.your.self.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.be.your.self.model.Subscription;

public interface SubscriptionService extends BaseService<Subscription, Integer>  {

	Page<Subscription> getListByPage(Pageable pageable);

	Iterable<Subscription> getList();

}
