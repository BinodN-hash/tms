package com.tms.repository;

import com.tms.model.Credentials;
import com.tms.model.User;
import com.tms.model.WrongCredentialAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Calendar;


@Repository
public interface WrongCredentialAttemptRepository extends JpaRepository<WrongCredentialAttempt, Long> {

		@Transactional
		@Modifying
		@Query("delete from WrongCredentialAttempt w where w.user = (:user) and w.credentialType = :credentialType")
		void deleteByUser(@Param("user") User user, @Param("credentialType") Credentials credentialType);
		
		@Transactional
		@Query("select count(*) from WrongCredentialAttempt a where a.date >= :limit and a.user = :user and a.credentialType = :credentialType")
		int getCountByUser(@Param("limit") Calendar limit, @Param("user") User user, @Param("credentialType") Credentials credentialType);
}
