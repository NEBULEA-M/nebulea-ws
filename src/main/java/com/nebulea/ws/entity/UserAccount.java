package com.nebulea.ws.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(value = "user_account")
public class UserAccount extends BaseEntity<String> {

  @Id
  @Column(value = "user_id")
  private Long userId;

  @Column(value = "sso")
  private String sso;

  @Column(value = "user_name")
  private String userName;

  @Column(value = "email")
  private String email;

  @Column(value = "default_language")
  private String defaultLanguage;

  @Column(value = "lasted_login_time")
  private Date lastedLogInTime;
}
