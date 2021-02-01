package fr.be.your.self.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Permission;

@Repository
public interface PermissionRepository extends BaseRepository<Permission, Integer> {
	
	public Iterable<Permission> findAllByUserId(Integer userId);
	
	public Optional<Permission> findByUserIdAndFunctionalityId(Integer userId, Integer functionalityId);
	
}
