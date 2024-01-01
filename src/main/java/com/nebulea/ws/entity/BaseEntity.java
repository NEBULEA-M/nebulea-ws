package com.nebulea.ws.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity<T> implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @CreatedDate
  @Column(value = "created_at")
  private Date createdDate;

  @CreatedBy
  @Column(value = "created_by")
  private T createdBy;

  @LastModifiedDate
  @Column(value = "updated_at")
  private Date updatedDate;

  @LastModifiedBy
  @Column(value = "updated_by")
  private T updatedBy;

}