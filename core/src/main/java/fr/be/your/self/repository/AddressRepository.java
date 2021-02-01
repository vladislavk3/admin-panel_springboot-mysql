package fr.be.your.self.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Address;

@Repository
public interface AddressRepository extends BaseRepository<Address, Integer> {
	public long countByAddressContainsIgnoreCase(String name);

	public Page<Address> findAllByAddressContainsIgnoreCase(String text, Pageable pageable);
	public Iterable<Address> findAllByAddressContainsIgnoreCase(String text, Sort sort);

}
