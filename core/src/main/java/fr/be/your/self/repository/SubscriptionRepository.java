package fr.be.your.self.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Subscription;

@Repository
public interface SubscriptionRepository extends BaseRepository<Subscription, Integer>   {
	@Query(
	    	"SELECT s FROM Subscription s WHERE  lower(s.user.firstName) LIKE lower(concat('%', :text,'%')) "
	    	+ " or lower(s.user.lastName) LIKE lower(concat('%', :text,'%')) or lower(s.user.email) LIKE lower(concat('%', :text,'%'))  or lower(s.subtype.name) LIKE lower(concat('%', :text,'%'))  "
	    )
	Page<Subscription> searchByFirstNameOrLastNameOrEmail(@Param("text") String text, Pageable pageable);
	
	Boolean existsByBusinessCodeId(Integer codeId);
	
	@Query("SELECT NEW org.apache.commons.lang3.tuple.ImmutablePair(a.businessCode.id, COUNT(a.id)) FROM Subscription a WHERE a.businessCode.id IN :codeIds GROUP BY a.businessCode.id")
	List<ImmutablePair<Integer, Long>> countByBusinessCodes(@Param("codeIds") Collection<Integer> codeIds);
	
	@Modifying
	@Query("UPDATE Subscription SET validStartDate = :validStartDate, validEndDate = :validEndDate, subscriptionStartDate = :subscriptionStartDate, subscriptionEndDate = :subscriptionEndDate, price = :subscriptionPrice, status = :status WHERE businessCode.id = :codeId")
	int updateFromBusinessCode(@Param("codeId") Integer codeId, 
			@Param("validStartDate") Date validStartDate,
			@Param("validEndDate") Date validEndDate,
			@Param("subscriptionStartDate") Date subscriptionStartDate,
			@Param("subscriptionEndDate") Date subscriptionEndDate,
			@Param("subscriptionPrice") BigDecimal subscriptionPrice,
			@Param("status") boolean status);
}
