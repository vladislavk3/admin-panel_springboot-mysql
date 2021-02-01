package fr.be.your.self.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.BusinessCode;

@Repository
public interface BusinessCodeRepository extends BaseRepository<BusinessCode, Integer> {
	
	Boolean existsByNameIgnoreCase(String name);
	
	long countByNameContainsIgnoreCase(String name);
	Iterable<BusinessCode> findAllByNameContainsIgnoreCase(String name, Sort sort);
    Page<BusinessCode> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
    
	//long countByTypeIn(Iterable<Integer> types);
    //Iterable<BusinessCode> findAllByTypeIn(Iterable<Integer> types, Sort sort);
    //Page<BusinessCode> findAllByTypeIn(Iterable<Integer> types, Pageable pageable);
    
    //long countByNameContainsIgnoreCaseAndTypeIn(String name, Iterable<Integer> types);
	//Iterable<BusinessCode> findAllByNameContainsIgnoreCaseAndTypeIn(String name, Iterable<Integer> types, Sort sort);
    //Page<BusinessCode> findAllByNameContainsIgnoreCaseAndTypeIn(String name, Iterable<Integer> types, Pageable pageable);
    
    //long countByNameContainsIgnoreCaseAndBeneficiaryContainsIgnoreCaseAndTypeIn(String name, String beneficiary, Iterable<Integer> types);
	//Iterable<BusinessCode> findAllByNameContainsIgnoreCaseAndBeneficiaryContainsIgnoreCaseAndTypeIn(String name, String beneficiary, Iterable<Integer> types, Sort sort);
    //Page<BusinessCode> findAllByNameContainsIgnoreCaseAndBeneficiaryContainsIgnoreCaseAndTypeIn(String name, String beneficiary, Iterable<Integer> types, Pageable pageable);
    
	@Query("SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%')))")
    long count(@Param("name") String name, @Param("beneficiary") String beneficiary);
	
	@Query(
    	value = "SELECT a FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%')))",
    	countQuery = "SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%')))" 
    )
	Iterable<BusinessCode> findAll(@Param("name") String name, @Param("beneficiary") String beneficiary, Sort sort);
	
	@Query(
    	value = "SELECT a FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%')))",
    	countQuery = "SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%')))" 
    )
    Page<BusinessCode> findAll(@Param("name") String name, @Param("beneficiary") String beneficiary, Pageable pageable);

	@Query("SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%'))) AND a.type IN :types")
    long count(@Param("name") String name, @Param("beneficiary") String beneficiary, @Param("types") Collection<Integer> types);
	
    @Query(
    	value = "SELECT a FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%'))) AND a.type IN :types",
    	countQuery = "SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%'))) AND a.type IN :types" 
    )
    Iterable<BusinessCode> findAll(@Param("name") String name, @Param("beneficiary") String beneficiary,
    		@Param("types") Collection<Integer> types, Sort sort);
    
    @Query(
    	value = "SELECT a FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%'))) AND a.type IN :types",
    	countQuery = "SELECT COUNT(a) FROM BusinessCode a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%'))) AND (:beneficiary IS NULL OR :beneficiary = '' OR lower(a.beneficiary) LIKE lower(concat('%', :beneficiary,'%'))) AND a.type IN :types" 
    )
    Page<BusinessCode> findAll(@Param("name") String name, @Param("beneficiary") String beneficiary,
    		@Param("types") Collection<Integer> types, Pageable pageable);
    
    @Modifying
    @Query("UPDATE BusinessCode SET status = ?2 WHERE name = ?1")
    int updateStatus(String name, Integer status);

    Iterable<BusinessCode> findAllByNameContainsIgnoreCase(String name);
}
