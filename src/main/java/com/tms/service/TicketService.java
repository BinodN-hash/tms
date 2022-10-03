package com.tms.service;

import com.amazonaws.services.opsworks.model.App;
import com.tms.model.*;
import com.tms.repository.TicketRepository;
import com.tms.util.GenerateRandomNumber;
import com.tms.util.LoggedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepo;

    public void create(String summary, String description,
                        String issuedApp, String priority, String assignee){
        User user = LoggedUser.getWebUser();
        Ticket ticket = new Ticket();
        ticket.setSummary(summary);
        ticket.setReporter(user.getName());
        ticket.setDescription(description);
        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(Priority.valueOf(priority));
        ticket.setIssuedApplication(ApplicationCategory.valueOf(issuedApp));
        ticket.setTId(GenerateRandomNumber.generateNum());
        ticketRepo.save(ticket);

    }

    public List<Ticket> getAllOpenTickets() {
        return ticketRepo.findAll();
    }

}
