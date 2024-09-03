package com.felipe.trip_planner_trip_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invites")
public class Invite {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID code;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "user_email", nullable = false)
  private String userEmail;

  @Column(name = "is_valid", nullable = false)
  private boolean isValid = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "trip_id", nullable = false)
  private Trip trip;

  public Invite() {}

  public UUID getCode() {
    return this.code;
  }

  public void setCode(UUID code) {
    this.code = code;
  }

  public UUID getUserId() {
    return this.userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getUserEmail() {
    return this.userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public boolean isValid() {
    return this.isValid;
  }

  public void setIsValid(boolean isValid) {
    this.isValid = isValid;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Trip getTrip() {
    return this.trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }
}
