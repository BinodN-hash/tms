package com.tms.controller.web;

import com.tms.annotation.CustomWebController;
import com.tms.model.Ticket;
import com.tms.model.User;
import com.tms.properties.SetUpProperties;
import com.tms.service.CustomUserDetailsService;
import com.tms.service.TicketService;
import com.tms.util.LoggedUser;
import com.tms.util.WebHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.tms.util.Consts.WEB_RESPONSE;

@CustomWebController
@Slf4j
public class TicketManagement {

    private static final SetUpProperties setup = SetUpProperties.getInstance();

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/ticket/create")
    public String createView(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("users", user);
        model.addAttribute("userList", userDetailsService.getAllUsers());
        return "ticket-management/create";

    }

    @PostMapping("/create/ticket")
    public String create(Model model,String summary, String description,
                         String issuedApp, String priority,  String assignee, RedirectAttributes attributes){
        User user = LoggedUser.getWebUser();
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("users", user);
        ticketService.create(summary, description, issuedApp, priority, assignee);
        attributes.addFlashAttribute(WEB_RESPONSE, WebHelper.responseSuccess(String.format(setup.getProperty("settings.message.ticket.create.success"))));
        return "redirect:/tms/ticket/create";

    }

    @GetMapping("/tickets")
    public String openTickets(Model model){
        User user = LoggedUser.getWebUser();
        model.addAttribute("users", user);
        model.addAttribute("tickets", ticketService.getAllOpenTickets());
        return "ticket-management/tickets";

    }
}
