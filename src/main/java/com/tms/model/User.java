package com.tms.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "users", indexes = {@Index(name = "ix_email", columnList = "email")})
public class User{



	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", columnDefinition = "varchar(50)")
	private String name;
	
	@Column(name = "email", columnDefinition = "varchar(50)")
	private String email;

	@Enumerated(EnumType.STRING)
	private Status status = Status.ACTIVE;
	
	private String password;
	

	@JsonIgnore
	@Column(name = "password_blocked_until")
	private Calendar passwordBlockedUntil;
	
	@JsonIgnore
	@Column(name =  "access_token")
	private String accessToken;
	
	@JsonIgnore
	@Column(name =  "refresh_token")
	private String refreshToken;

	@CreationTimestamp
	@Column(name = "creation_date")
	private LocalDateTime creationDate;

	@Enumerated(EnumType.STRING)
	private Department department;
	@Enumerated(EnumType.STRING)
	private Designation designation;
	
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;


	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "users_permissions", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"))
	private Set<Permission> permissions = new HashSet<Permission>();
	
	public void addPermissions(List<Permission> permissions) {
		this.permissions.addAll(permissions);
	}
	
	public void removePermissions(Set<Permission> permissions) {
		this.permissions.removeAll(permissions);
	}
	
	public boolean isSuperAdmin() {
		return role.getType().SUPER_ADMIN.equals(RoleType.SUPER_ADMIN);
	}
	
	@JsonIgnore
	@Lob
	@Column(name = "private_key")
	private String privateKey;
	
}
