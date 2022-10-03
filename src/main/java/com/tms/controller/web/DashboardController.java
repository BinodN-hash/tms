package com.tms.controller.web;

import com.tms.annotation.CustomWebController;
import com.tms.model.User;
import com.tms.util.LoggedUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@CustomWebController
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("users", user);
        return "dashboard";

    }
}
