package fr.be.your.self.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.User;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.UserRepository;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.StringUtils;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, Integer> implements UserService {

	@Autowired
	private UserRepository repository;

	@Override
	protected BaseRepository<User, Integer> getRepository() {
		return this.repository;
	}

	@Override
	public String getDefaultSort() {
		return "email|asc";
	}

	@Override
	public boolean existsEmail(String email) {
		return this.repository.existsByEmail(email);
	}

	@Override
	protected Iterable<User> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text) ? this.repository.findAll(sort)
				: this.repository
						.findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(text,
								text, text, sort);
	}

	@Override
	public Iterable<User> findAll() {
		return this.repository.findAll();
	}

	@Override
	protected Page<User> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) ? this.repository.findAll(pageable)
				: this.repository
						.findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(text,
								text, text, pageable);
	}

	public Page<User> getPaginatedUsers(Pageable pageable) {
		return this.repository.findAll(pageable);
	}

	@Override
	public PageableResponse<User> searchByName(String firstNameOrLastName, Pageable pageable, Sort sort) {
		final PageableResponse<User> result = new PageableResponse<>();
		result.setTotalItems(0);
		result.setTotalPages(0);
		result.setPageIndex(0);
		result.setPageSize(-1);
		
		if (pageable == null) {
			final Iterable<User> domains = StringUtils.isNullOrSpace(firstNameOrLastName) 
					? this.repository.findAll(sort == null ? Sort.unsorted() : sort)
					: this.repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
							firstNameOrLastName, firstNameOrLastName, sort == null ? Sort.unsorted() : sort);

			if (domains == null) {
				result.setItems(Collections.emptyList());
			} else {
				final List<User> users = new ArrayList<>();
				domains.forEach(users::add);
				
				result.setItems(users);
				result.setTotalItems(users.size());
				result.setTotalPages(1);
				result.setPageIndex(1);
			}
			
			return result;
		}
		
		final Page<User> pageDomain = StringUtils.isNullOrSpace(firstNameOrLastName) 
				? this.repository.findAll(pageable)
				: this.repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(firstNameOrLastName, firstNameOrLastName, pageable);
		if (pageDomain == null) {
			result.setItems(Collections.emptyList());
			return result;
		}
		
		final int pageIndex = pageable.getPageNumber() + 1;
		final int pageSize = pageable.getPageSize();
		
		result.setItems(pageDomain.getContent());
		result.setTotalItems(pageDomain.getTotalElements());
		result.setTotalPages(pageDomain.getTotalPages());
		result.setPageIndex(pageIndex);
		result.setPageSize(pageSize);
		
		return result;
	}

	@Override
	public PageableResponse<User> searchProfessionalByName(String firstNameOrLastName, Pageable pageable, Sort sort) {
		final PageableResponse<User> result = new PageableResponse<>();
		result.setTotalItems(0);
		result.setTotalPages(0);
		result.setPageIndex(0);
		result.setPageSize(-1);
		
		if (pageable == null) {
			final Sort userSort = sort == null ? Sort.unsorted() : sort;
			final Iterable<User> domains = StringUtils.isNullOrSpace(firstNameOrLastName) 
					? this.repository.findAllByUserTypeAndStatus(UserType.PROFESSIONAL.getValue(), 
							UserStatus.ACTIVE.getValue(), userSort)
					: this.repository.findAllByUserTypeAndStatusAndFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
							UserType.PROFESSIONAL.getValue(), UserStatus.ACTIVE.getValue(), 
							firstNameOrLastName, firstNameOrLastName, userSort);

			if (domains == null) {
				result.setItems(Collections.emptyList());
			} else {
				final List<User> users = new ArrayList<>();
				domains.forEach(users::add);
				
				result.setItems(users);
				result.setTotalItems(users.size());
				result.setTotalPages(1);
				result.setPageIndex(1);
			}
			
			return result;
		}
		
		final Page<User> pageDomain = StringUtils.isNullOrSpace(firstNameOrLastName) 
				? this.repository.findAllByUserTypeAndStatus(UserType.PROFESSIONAL.getValue(), 
						UserStatus.ACTIVE.getValue(), pageable)
				: this.repository.findAllByUserTypeAndStatusAndFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
						UserType.PROFESSIONAL.getValue(), UserStatus.ACTIVE.getValue(),
						firstNameOrLastName, firstNameOrLastName, pageable);
		if (pageDomain == null) {
			result.setItems(Collections.emptyList());
			return result;
		}
		
		final int pageIndex = pageable.getPageNumber() + 1;
		final int pageSize = pageable.getPageSize();
		
		result.setItems(pageDomain.getContent());
		result.setTotalItems(pageDomain.getTotalElements());
		result.setTotalPages(pageDomain.getTotalPages());
		result.setPageIndex(pageIndex);
		result.setPageSize(pageSize);
		
		return result;
	}

	@Override
	public List<User> getActivateProfessionals(Collection<Integer> ids, Sort sort) {
		final Iterable<User> domains = this.repository.findAllByIdInAndUserTypeAndStatus(ids, UserType.PROFESSIONAL.getValue(), 
				UserStatus.ACTIVE.getValue(), sort == null ? Sort.unsorted() : sort);
		
		if (domains == null) {
			return Collections.emptyList();
		}
		
		final List<User> users = new ArrayList<>();
		domains.forEach(users::add);
		
		return users;
	}

	@Override
	@Transactional(readOnly = true)
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}

		return this.repository.countByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(
				text, text, text);
	}

	@Override
	public User saveOrUpdate(User user) {
		return this.repository.save(user);
	}

	@Override
	public <S extends User> Iterable<S> saveAll(Iterable<S> entities) {
		return this.repository.saveAll(entities);
	}

	@Override
	public User getByActivateCode(String activateCode) {
		return this.repository.findByActivateCode(activateCode);
	}

	@Override
	public User getByResetPasswordCode(String resetPasswordCode) {
		return this.repository.findByResetPasswordCode(resetPasswordCode);
	}

	@Override
	@Transactional
	public boolean activateUser(Integer userId) {
		return this.repository.updateStatus(userId, UserStatus.ACTIVE.getValue()) > 0;
	}

	@Override
	@Transactional
	public boolean updatePassword(Integer userId, String password) {
		return this.repository.updatePassword(userId, password) > 0;
	}

	@Override
	public User getByEmail(String email) {
		return this.repository.findByEmail(email);
	}

	@Override
	public Page<User> findAllByUserType(String userType, Pageable pageable) {
		return this.repository.findAllByUserType(userType, pageable);
	}

	@Override
	public Page<User> findAllByStatus(int status, Pageable pageable) {
		return this.repository.findAllByStatus(status, pageable);

	}

	@Override
	public Iterable<User> findAllById(List<Integer> ids) {
		return this.repository.findAllById(ids);
	}

	@Override
	public Page<User> findAllByEmailOrFirstNameOrLastName(String email, String firstName, String lastName,
			Pageable pageable) {
		return this.repository
				.findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(email,
						firstName, lastName, pageable);
	}

	@Override
	public PageableResponse<User> pageableSearch(String text, String filterRole, Integer filterStatus,
			List<Integer> filterSubscriptionTypesIds, PageRequest pageable, Sort sort) {
		if ( (filterSubscriptionTypesIds == null || filterSubscriptionTypesIds.isEmpty()) 
			 && (filterStatus == null) 
			 &&	 StringUtils.isNullOrEmpty(filterRole) ) {
					return this.pageableSearch(text, pageable, sort);
		}
		
		if (pageable == null) {
			final List<User> items = this.search(text, filterRole, filterStatus, filterSubscriptionTypesIds, sort);
			
			final PageableResponse<User> result = new PageableResponse<>();
			result.setItems(items);
			result.setTotalItems(items.size());
			result.setTotalPages(1);
			result.setPageIndex(1);
			result.setPageSize(-1);
			
			return result;
		}
		
		Page<User> pageDomain;

		if (filterSubscriptionTypesIds != null && !filterSubscriptionTypesIds.isEmpty()) {
			pageDomain = this.findAllBySubscriptionType(filterSubscriptionTypesIds, pageable);
		} else 	if (!StringUtils.isNullOrEmpty(filterRole)) {
			pageDomain = this.findAllByUserType(filterRole, pageable);
		} else if (filterStatus != null) {
			pageDomain = this.findAllByStatus(filterStatus, pageable);
		} else {
			pageDomain = this.getPaginatedObjects(pageable);
		}
		
		
		if (pageDomain == null) {
			return null;
		}
		
		final int pageIndex = pageable.getPageNumber() + 1;
		final int pageSize = pageable.getPageSize();
		
		final PageableResponse<User> result = new PageableResponse<>();
		result.setItems(pageDomain.getContent());
		result.setTotalItems(pageDomain.getTotalElements());
		result.setTotalPages(pageDomain.getTotalPages());
		result.setPageIndex(pageIndex);
		result.setPageSize(pageSize);
		
		return result;
		
	}
	
	@Override
	public Page<User> findAllBySubscriptionType(List<Integer> subscriptionTypesIds, Pageable pageable) {
		return this.repository.findAllBySubscriptionType(subscriptionTypesIds, pageable);
	}

	@Override
	public Iterable<User> findAllBySubscriptionType(List<Integer> subscriptionTypesIds) {
		return this.repository.findAllBySubscriptionType(subscriptionTypesIds);
	}
	
	private List<User> search(String text, String filterRole, Integer filterStatus,
			List<Integer> filterSubscriptionTypesIds, Sort sort) {
		Iterable<User> domains;

		final Sort domainSort = sort == null ? Sort.unsorted() : sort;
		
		if (filterSubscriptionTypesIds != null && !filterSubscriptionTypesIds.isEmpty()) {
			domains = this.findAllBySubscriptionType(filterSubscriptionTypesIds);
		} else 	if (!StringUtils.isNullOrEmpty(filterRole)) {
			domains = this.findAllByUserType(filterRole);
		} else if (filterStatus != null) {
			domains = this.findAllByStatus(filterStatus);
		} 	else {
			domains = this.findAll();
		}
		
		
		if (domains == null) {
			return Collections.emptyList();
		}
		
		final List<User> result = new ArrayList<>();
		domains.forEach(result::add);
		
		return result;
	}
	

	@Override
	public Iterable<User> findAllByUserType(String userType) {
		return this.repository.findAllByUserType(userType);
	}
	
	@Override
	public Iterable<User> findAllByStatus(int status) {
		return this.repository.findAllByStatus(status);
	}

	@Override
	public PageableResponse<User> pageableSearchPro(String search, PageRequest pageable, Sort sort) {
		Page<User> page;
		
		if (pageable == null) {//Not paging
			Iterable<User> users;

			if (StringUtils.isNullOrSpace(search))	{
				users  =  this.repository.findAllByUserType(UserType.PROFESSIONAL.getValue());
			} else {
				users =  this.repository.searchByUserTypeAndEmailOrFirstNameOrLastName(UserType.PROFESSIONAL.getValue(), search);
			}
			
			List<User> usersList = new ArrayList<>();
		
			if (users != null) {
				users.forEach(usersList::add);
			}	
			final PageableResponse<User> result = createPageableResponse(usersList);
			
			return result;
		}
		
		if (StringUtils.isNullOrSpace(search))	{
			page  =  this.repository.findAllByUserType(UserType.PROFESSIONAL.getValue(), pageable);
		} else {
			page =  this.repository.searchByUserTypeAndEmailOrFirstNameOrLastName(UserType.PROFESSIONAL.getValue(), search, pageable);

		}
		return new PageableResponse<>(page);

	}

	private PageableResponse<User> createPageableResponse(List<User> usersList) {
		final PageableResponse<User> result = new PageableResponse<>();
		result.setItems(usersList);
		result.setTotalItems(usersList.size());
		result.setTotalPages(1);
		result.setPageIndex(1);
		result.setPageSize(-1);
		return result;
	}
	
}
