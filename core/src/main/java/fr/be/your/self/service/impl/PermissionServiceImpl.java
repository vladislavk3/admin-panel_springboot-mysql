package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.Permission;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.PermissionRepository;
import fr.be.your.self.service.PermissionService;

@Service
public class PermissionServiceImpl extends BaseServiceImpl<Permission, Integer> implements PermissionService {
	
	@Autowired
	private PermissionRepository repository;
	
	@Override
	protected BaseRepository<Permission, Integer> getRepository() {
		return this.repository;
	}

	@Override
	public String getDefaultSort() {
		return null;
	}
	
	@Override
	public Iterable<Permission> getPermissionByUserId(Integer userId) {
		return this.repository.findAllByUserId(userId);
	}

	@Override
	public long count(String text) {
		return this.repository.count();
	}

	@Override
	public Iterable<Permission> findAll() {
		return this.repository.findAll();
	}

	@Override
	public Page<Permission> getPaginatedObjects(Pageable pageable) {
		return null;
	}

	@Override
	public <S extends Permission> Iterable<S> saveAll(Iterable<S> entities) {
		return this.repository.saveAll(entities);
	}

	@Override
	protected Iterable<Permission> getList(String text, Sort sort) {
		return null;
	}

	@Override
	protected Page<Permission> getListByPage(String text, Pageable pageable) {
		return null;
	}
	
	@Override
	public void saveOrUpdate(Permission permission) {
		this.repository.save(permission); 
	}
}
