package fr.be.your.self.repository;

import org.springframework.stereotype.Repository;
import fr.be.your.self.model.Price;

@Repository
public interface PriceRepository  extends BaseRepository<Price, Integer> {

	long countByLabelContainsIgnoreCase(String text);

}
