package com.tms.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "permissions")
public class Permission{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(name = "code", columnDefinition = "varchar(50)",unique=true, nullable = false)
	private String code;
	
	@Column(name = "name", columnDefinition = "varchar(50)", nullable = false)
	private String name;
	
	@Transient
	private boolean active;
	
	@Column(name = "category")
	@Enumerated(EnumType.STRING)
	private PermissionCategory category;
	
}
