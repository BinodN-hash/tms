package com.tms.controller.web;

import com.tms.annotation.CustomWebController;
import com.tms.exception.ValidationException;
import com.tms.model.*;
import com.tms.properties.SetUpProperties;
import com.tms.repository.UserRepository;
import com.tms.service.CustomUserDetailsService;
import com.tms.service.EmailService;
import com.tms.util.ApplicationHelper;
import com.tms.util.LoggedUser;
import com.tms.util.WebHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tms.util.Consts.WEB_RESPONSE;

@CustomWebController
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;
    private final UserRepository userRepo;
    private static final SetUpProperties setup = SetUpProperties.getInstance();
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public String users(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("roles", userDetailsService.getAllRoles());
        model.addAttribute("users", userDetailsService.getAllUsers());
        model.addAttribute("user", user);
        return "user-management/users";
    }

    @PostMapping("/create/user")
    public String createUser(User user,
                             @RequestParam String roleId, boolean autoGenerate,
                             RedirectAttributes attributes) throws MessagingException {
        Role role = userDetailsService.getRoleById(Long.parseLong(roleId));

        String randomPassword = null;
        log.info("Auto: " + autoGenerate);
        Optional<User> optionalUser = userDetailsService.findByEmail(user.getEmail());
        if (optionalUser.isPresent()){
            attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseError(setup.getProperty("settings.message.email.exist")));
            return "redirect:/tms/users";
        }
        if (autoGenerate) {
            randomPassword = ApplicationHelper.generateRandomPassword();
            log.info("Random Password: " + randomPassword);
            user.setPassword(randomPassword);
        } else {
            try {
                ApplicationHelper.checkPasswordPolicy(user.getPassword());
            } catch (ValidationException e) {
                attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseError(String.format(e.getMessage())));
                return "redirect:/tms/users";
            }
            user.setPassword(user.getPassword());
        }

        user.setRole(role);

        userDetailsService.saveUser(user);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.user.create.success"), user.getName())));
        return "redirect:/tms/users";

    }

    @GetMapping("/delete/user/{userId}")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes attributes){
        userDetailsService.deleteUser(userId);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.user.deleted.success"))));
        return "redirect:/tms/users";

    }

    @PostMapping("/update/user")
    public String updateUser(User user, RedirectAttributes attributes){
        Role role = userDetailsService.getRoleById(user.getRole().getId());
        user.setCreationDate(LocalDateTime.now());
        user.setRole(role);
        userDetailsService.updateUser(user);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.user.updated.success"))));
        return "redirect:/tms/users";

    }

    @GetMapping("/roles")
    public String roles(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("user", user);
        model.addAttribute("roles", userDetailsService.getAllRoles());
        return "user-management/roles";
    }


    @PostMapping("/create/role")
    public String createRole(Model model, RedirectAttributes attributes, @RequestParam String roleType, @RequestParam String name){
        User user = LoggedUser.getWebUser();
        model.addAttribute("user", user);
        userDetailsService.createRole(roleType, name);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.role.create.success"))));
        return "redirect:/tms/roles";

    }

    @PostMapping("/update/role")
    public String updateRole(Role role, @RequestParam String roleType, RedirectAttributes attributes){
        role.setType(RoleType.valueOf(roleType));
        userDetailsService.updateRole(role);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.role.updated.success"))));
        return "redirect:/tms/roles";

    }

    @GetMapping("/delete/role/{roleId}")
    public String deleteRole(@PathVariable Long roleId, RedirectAttributes attributes){
        userDetailsService.deleteRole(roleId);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.role.deleted.success"))));
        return "redirect:/tms/roles";

    }


    @GetMapping("/permissions")
    public String permissions(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("user", user);
        model.addAttribute("permissions", userDetailsService.getAllPermissions());
        return "user-management/permissions";
    }

    @PostMapping("/create/permission")
    public String createPermission(Model model, RedirectAttributes attributes,
                                   @RequestParam String category, @RequestParam String code, @RequestParam String name
                                   ){
        User user = LoggedUser.getWebUser();
        model.addAttribute("user", user);
        userDetailsService.createPermission(category, code, name);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.permissions.create.success"))));
        return "redirect:/tms/permissions";

    }


    @PostMapping("/update/permission")
    public String updatePermission(Permission permission, @RequestParam String category, RedirectAttributes attributes){
        permission.setCategory(PermissionCategory.valueOf(category));
        userDetailsService.updatePermission(permission);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.permission.updated.success"))));
        return "redirect:/tms/permissions";

    }

    @GetMapping("/delete/permission/{permissionId}")
    public String deletePermission(@PathVariable Long permissionId, RedirectAttributes attributes){
        userDetailsService.deletePermission(permissionId);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.permission.deleted.success"))));
        return "redirect:/tms/permissions";

    }

    @GetMapping("/view/user/profile/{userId}")
    public String viewProfile(Model model, @PathVariable String userId){
        User user = userDetailsService.findUserById(Long.valueOf(userId)).get();
        model.addAttribute("users", user);
        model.addAttribute("permissions", userDetailsService.getAllPermissions());
        return "user-management/profile";

    }

}
