package com.tms.service;


import com.tms.model.Credentials;
import com.tms.model.User;
import com.tms.model.WrongCredentialAttempt;

import java.util.Calendar;

public interface WrongAttemptService {

	public void clear(final User user, final Credentials credentialType);
	public int count(final Calendar limit, final User user, final Credentials credentialTYpe);
	public WrongCredentialAttempt record(final User user, final Credentials credentialType);
}
