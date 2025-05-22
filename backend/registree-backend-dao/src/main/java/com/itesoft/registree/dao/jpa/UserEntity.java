package com.itesoft.registree.dao.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

import org.hibernate.annotations.ColumnTransformer;

@Entity(name = "reg_user")
public class UserEntity {
  @Id
  @SequenceGenerator(name = "reg_user_id_seq", sequenceName = "reg_user_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reg_user_id_seq")
  private Long id;

  @Column(nullable = false)
  private String username;

  @ColumnTransformer(write = "crypt(?::text, gen_salt('md5'))")
  private String password;

  private String firstName;

  private String lastName;

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }
}
