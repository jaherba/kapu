package com.kapu.kapuproject.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType name;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private LocalTime start;

    @Column(nullable = false)
    private LocalTime end;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private User employee;

    @Column(nullable = false)
    private LocalDate date;  

    @Column(nullable = false)
    private boolean open = false; 

    
    public Shift() {}

    
    public Shift(ShiftType name, Integer duration, LocalTime start, LocalTime end, User employee, LocalDate date) {
        this.name = name;
        this.duration = duration;
        this.start = start;
        this.end = end;
        this.employee = employee;
        this.date = date;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ShiftType getName() { return name; }
    public void setName(ShiftType name) { this.name = name; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public LocalTime getStart() { return start; }
    public void setStart(LocalTime start) { this.start = start; }

    public LocalTime getEnd() { return end; }
    public void setEnd(LocalTime end) { this.end = end; }

    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
}
