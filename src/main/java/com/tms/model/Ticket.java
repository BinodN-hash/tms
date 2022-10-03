package com.tms.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bytebuddy.asm.Advice;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String tId;

    private String summary;
    private String description;
    private String reporter;
    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    @CreationTimestamp
    private LocalDateTime created;
    @Column(nullable = true)
    private String assignee;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    private ApplicationCategory issuedApplication;



}
