package com.tms.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@Table(name = "wrong_credential_attempts")
public class WrongCredentialAttempt{


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar date;

	@Enumerated(EnumType.STRING)
	private Credentials credentialType;
}
