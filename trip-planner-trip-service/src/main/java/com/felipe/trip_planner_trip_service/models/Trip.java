package com.felipe.trip_planner_trip_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trips")
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String destination;

  @Column(name = "owner_email", nullable = false)
  private String ownerEmail;

  @Column(name = "owner_name", nullable = false)
  private String ownerName;

  @Column(name = "is_confirmed", nullable = false)
  private boolean isConfirmed = true;

  @Column(name = "starts_at", nullable = false)
  private LocalDate startsAt;

  @Column(name = "ends_at", nullable = false)
  private LocalDate endsAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public Trip() {}

  public UUID getId() {
    return this.id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getDestination() {
    return this.destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getOwnerEmail() {
    return this.ownerEmail;
  }

  public void setOwnerEmail(String ownerEmail) {
    this.ownerEmail = ownerEmail;
  }

  public String getOwnerName() {
    return this.ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public boolean isConfirmed() {
    return this.isConfirmed;
  }

  public void setIsConfirmed(boolean isConfirmed) {
    this.isConfirmed = isConfirmed;
  }

  public LocalDate getStartsAt() {
    return this.startsAt;
  }

  public void setStartsAt(LocalDate startsAt) {
    this.startsAt = startsAt;
  }

  public LocalDate getEndsAt() {
    return this.endsAt;
  }

  public void setEndsAt(LocalDate endsAt) {
    this.endsAt = endsAt;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
