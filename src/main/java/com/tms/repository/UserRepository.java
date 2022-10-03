package com.tms.repository;

import com.tms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	
	@Query("select u from User u join fetch u.permissions join fetch u.role where u.email = ?1")
	Optional<User> findByEmail(String email);


	@Query("select u from User u join fetch u.role where u.id = ?1")
	Optional<User> findById(Long id);
	
	@Modifying
	@Query("update User u set u.accessToken = :accessToken where u.email = :email")
	void updateUserAccessToken(String accessToken,String email);
	
	@Modifying
	@Query("update User u set u.refreshToken = :refreshToken where u.email = :email")
	void updateUserRefreshToken(String refreshToken,String email);


}
