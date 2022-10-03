package com.tms.service;

import com.tms.exception.ApplicationException;
import com.tms.model.*;
import com.tms.properties.SetUpProperties;
import com.tms.repository.PermissionRepository;
import com.tms.repository.RoleRepository;
import com.tms.repository.UserRepository;
import com.tms.util.ApplicationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static SetUpProperties setup = SetUpProperties.getInstance();

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PermissionRepository permissionRepo;

	@Autowired
	private EntityManager em;

	@Override
	public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByEmail(username).map(u -> new CustomUserDetails(u))
				.orElseThrow(() -> new UsernameNotFoundException(setup.getProperty("settings.message.login.invalid")));


	}

	public CustomUserDetails loadRestUserByUsername(String username) {
		Optional<User> optionalUser = userRepo.findByEmail(username);
		if (!optionalUser.isPresent()) {
			ApplicationHelper.sendError(setup.getProperty("settings.message.login.invalid"), UNAUTHORIZED.value());
			throw new ApplicationException(setup.getProperty("settings.message.login.invalid"));
		}
		return new CustomUserDetails(optionalUser.get());
	}

	public User updateUser(User user) {
		return userRepo.save(user);
	}


	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepo.save(user);
	}

	public User saveRestUser(User user) {
		if (!Objects.isNull(user.getPassword()))
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(createRoleIfNotFound("USER"));
		return userRepo.save(user);
	}

	public void updateAccessToken(String accessToken, String email) {
		userRepo.updateUserAccessToken(accessToken, email);
	}

	public void updateRefreshToken(String refreshToken, String email) {
		userRepo.updateUserRefreshToken(refreshToken, email);
	}
	
	public Optional<User> findUserByUsername(String username) {

		return userRepo.findByEmail(username);
	}

	public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
		return passwordEncoder.matches(oldPassword, user.getPassword());
	}

	public void changeUserPassword(final User user, final String password) {
		user.setPassword(passwordEncoder.encode(password));
		userRepo.save(user);
	}

	public void changeUserStatus(final User user, final String status) {
		user.setStatus(Status.valueOf(status));
		userRepo.save(user);
	}


	private Role createRoleIfNotFound(final String name) {
		Role role = roleRepo.findByName(name);
		if (role == null) {
			role = new Role();
			role.setName(name);
		}
		role = roleRepo.save(role);
		return role;
	}




	public void updateRole(Role Role) {
		roleRepo.save(Role);
	}

	public void removeRole(Long id) {
		Role Role = new Role();
		Role.setId(id);
		roleRepo.delete(Role);
	}

	public List<Role> getAllRoles() {
		return roleRepo.findAll();
	}

	public List<User> getAllUsers() {
		return userRepo.findAll();
	}

	public Role getRoleById(Long id) {
		return roleRepo.findById(id).get();
	}

	public void createPermission(String category, String code, String name) {
		Permission p = new Permission();
		p.setCategory(PermissionCategory.valueOf(category));
		p.setCode(code);
		p.setName(name);
		permissionRepo.save(p);
	}

	public void updatePermission(Permission permission) {
		permissionRepo.save(permission);
	}

	public void removePermission(Long id) {
		Permission p = new Permission();
		p.setId(id);
		permissionRepo.delete(p);
	}

	public List<Permission> getAllPermissions() {
		return permissionRepo.findAll();
	}

	public Permission getPermissionById(Long id) {
		return permissionRepo.findById(id).get();
	}


	public Optional<User> findUserById(Long id) {

		return userRepo.findById(id);
	}

	public Optional<User> findByEmail(String email) {
		return userRepo.findByEmail(email);
	}

	public void createRole(String type, String name){
		Role role = new Role();
		role.setName(name);
		role.setType(RoleType.valueOf(type));
		roleRepo.save(role);
	}

	public void deleteUser(Long id) {
		User user = new User();
		user.setId(id);
		userRepo.delete(user);
	}

	public void deleteRole(Long id) {
		Role role = new Role();
		role.setId(id);
		roleRepo.delete(role);
	}

	public void deletePermission(Long id) {
		Permission p = new Permission();
		p.setId(id);
		permissionRepo.delete(p);
	}

}
