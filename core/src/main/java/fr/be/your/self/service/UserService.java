package fr.be.your.self.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.User;

public interface UserService extends BaseService<User, Integer> {
	
	public boolean existsEmail(String email);
	
	public User getByEmail(String email);
	
	public User saveOrUpdate(User user);
	
	public User getByActivateCode(String activateCode);
	
	public User getByResetPasswordCode(String resetPasswordCode);
	
	public PageableResponse<User> searchByName(String firstNameOrLastName, Pageable pageable, Sort sort);
	
	public PageableResponse<User> searchProfessionalByName(String firstNameOrLastName, Pageable pageable, Sort sort);
	
	public List<User> getActivateProfessionals(Collection<Integer> ids, Sort sort);
	
	Page<User> findAllBySubscriptionType(List<Integer> subtypeIds, Pageable pageable);
	Page<User> findAllByUserType(String userType, Pageable pageable);
    Page<User> findAllByStatus(int status, Pageable pageable);
    Page<User> findAllByEmailOrFirstNameOrLastName(String email, String firstName, String lastName, Pageable pageable);

	Iterable<User> findAllById(List<Integer> ids);
	Iterable<User> findAllByUserType(String filterRole);
	Iterable<User> findAllByStatus(int status);
	Iterable<User> findAllBySubscriptionType(List<Integer> subscriptionTypesIds);

	public boolean activateUser(Integer userId);

	public boolean updatePassword(Integer userId, String password);
	
	public PageableResponse<User> pageableSearch(String text, String filterRole, Integer filterStatus,
			List<Integer> filterSubscriptionTypesIds, PageRequest pageable, Sort sort);

	public  PageableResponse<User> pageableSearchPro(String search, PageRequest pageable, Sort sort);


}
