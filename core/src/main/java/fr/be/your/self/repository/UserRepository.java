package fr.be.your.self.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.User;

@Repository
public interface UserRepository extends BaseRepository<User, Integer> {
    
	Boolean existsByEmail(String email);
    
    User findByEmail(String email);
    
    User findBySocialLogin(String socialLogin);
    
    User findByActivateCode(String activateCode);
    
    User findByResetPasswordCode(String resetPasswordCode);
    
    long countByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String email, String firstName, String lastName);
    
    Iterable<User> findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String email, String firstName, String lastName);
    
    Iterable<User> findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String email, String firstName, String lastName, Sort sort);
    
    Page<User> findAllByEmailContainsIgnoreCaseOrFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String email, String firstName, String lastName, Pageable pageable);
    
    Page<User> findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String firstName, String lastName, Pageable pageable);
    Iterable<User> findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String firstName, String lastName, Sort sort);
    
    Page<User> findAllByUserTypeAndStatusAndFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String userType, int status, 
    		String firstName, String lastName, Pageable pageable);
    Iterable<User> findAllByUserTypeAndStatusAndFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String userType, int status, 
    		String firstName, String lastName, Sort sort);
    
    
    Page<User> findAllByUserTypeAndStatus(String userType, int status, Pageable pageable);
    Iterable<User> findAllByUserTypeAndStatus(String userType, int status, Sort sort);
    
    Page<User> findAllByUserType(String userType, Pageable pageable);
    Iterable<User> findAllByUserType(String userType, Sort sort);
    Iterable<User> findAllByUserType(String userType);

    
    Page<User> findAllByStatus(int status, Pageable pageable);
    Iterable<User> findAllByStatus(int status, Sort sort);
    Iterable<User> findAllByStatus(int status);

    
    Iterable<User> findAllByIdInAndUserTypeAndStatus(Iterable<Integer> ids, String userType, int status, Sort sort);
    
    @Modifying
    @Query("UPDATE User SET status = ?2, activateCode = NULL, activateTimeout = 0 WHERE id = ?1")
    int updateStatus(Integer id, Integer status);
    
    @Modifying
    @Query("UPDATE User SET password = ?2, resetPasswordCode = NULL, resetPasswordTimeout = 0 WHERE id = ?1")
    int updatePassword(Integer id, String password);
    
    @Query(
        	value = "SELECT u FROM User u, Subscription s, SubscriptionType st WHERE u.id = s.user AND s.subtype=st.id  AND st.id IN :subtypeIds"
        )
    Page<User> findAllBySubscriptionType(@Param("subtypeIds") Collection<Integer> subtypeIds, 
    		Pageable pageable);
   
    @Query(
        	value = "SELECT u FROM User u, Subscription s, SubscriptionType st WHERE u.id = s.user AND s.subtype=st.id  AND st.id IN :subtypeIds"
        )
    Iterable<User> findAllBySubscriptionType(@Param("subtypeIds") Collection<Integer> subtypeIds);
    
    @Query(
        	value = "SELECT u FROM User u WHERE lower(u.userType) LIKE lower(concat('%', :userType,'%')) AND ( (lower(u.email) LIKE lower(concat('%', :text,'%'))) OR  (lower(u.firstName) LIKE lower(concat('%', :text,'%'))) OR  (lower(u.lastName) LIKE lower(concat('%', :text,'%'))) )"
    )
    Page<User> searchByUserTypeAndEmailOrFirstNameOrLastName(@Param("userType") String userType, @Param("text") String text, Pageable pageable);
    
    @Query(
        	value = "SELECT u FROM User u WHERE lower(u.userType) LIKE lower(concat('%', :userType,'%')) AND ( (lower(u.email) LIKE lower(concat('%', :text,'%'))) OR  (lower(u.firstName) LIKE lower(concat('%', :text,'%'))) OR  (lower(u.lastName) LIKE lower(concat('%', :text,'%'))) )"
    )
    Iterable<User> searchByUserTypeAndEmailOrFirstNameOrLastName(@Param("userType") String userType, @Param("text") String text);
}
