package com.feedss.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.feedss.portal.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

}
