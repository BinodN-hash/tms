package com.tms.service;

import com.tms.model.Credentials;
import com.tms.model.User;
import com.tms.model.WrongCredentialAttempt;
import com.tms.repository.WrongCredentialAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;


@Service("wrongAttemptService")
public class WrongAttemptServiceImpl implements WrongAttemptService{

	@Autowired
	private WrongCredentialAttemptRepository wrongCredentialRepository;
	
	@Override
	public void clear(User user, Credentials credentialType) {
		wrongCredentialRepository.deleteByUser(user,credentialType);
		
	}

	@Override
	public int count(Calendar limit, User user, Credentials credentialType) {
		
		return wrongCredentialRepository.getCountByUser(limit, user,credentialType);
	}

	@Override
	public WrongCredentialAttempt record(final User user, final Credentials credentialType) {
		WrongCredentialAttempt attempt = new WrongCredentialAttempt();
		attempt.setDate(Calendar.getInstance());
		attempt.setUser(user);
		attempt.setCredentialType(credentialType);
		return wrongCredentialRepository.save(attempt);
	}
	
	
}
