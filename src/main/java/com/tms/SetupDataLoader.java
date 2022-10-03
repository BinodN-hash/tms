package com.tms;

import com.tms.model.*;
import com.tms.repository.PermissionRepository;
import com.tms.repository.RoleRepository;
import com.tms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


//@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // == create initial privileges
        final Permission loginPermission = createPrivilegeIfNotFound("USER_MANAGEMENT","Allow Login","ALLOW_LOGIN");

        // == create initial groups
        final Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(loginPermission);
        
        final Role adminGroup = createGroupIfNotFound(RoleType.SUPER_ADMIN,"Super Admin", adminPermissions);

        // == create initial user
        createUserIfNotFound("admin@tms.com.np", "Administrator", "1234", adminGroup,adminPermissions);
        
        alreadySetup = true;
    }

    @Transactional
    Permission createPrivilegeIfNotFound(final String permissionCategory,final String name,final String code) {
    	Permission permission = permissionRepository.findByName(name);
        if (permission == null) {
            permission = new Permission();
            permission.setName(name);
            permission.setCode(code);
            permission.setCategory(PermissionCategory.valueOf(permissionCategory));
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    @Transactional
    Role createGroupIfNotFound(final RoleType type,final String name, final Set<Permission> permissions) {
        Role role = roleRepo.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setType(type);
        }
        role = roleRepo.save(role);
        return role;
    }
    



    @Transactional
    User createUserIfNotFound(final String email, final String name, final String password, final Role role, final Set<Permission> permissions ) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = null;
        if (!userOptional.isPresent()) {
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role);
        user.setPermissions(permissions);
        user = userRepository.save(user);
        return user;
    }

}
