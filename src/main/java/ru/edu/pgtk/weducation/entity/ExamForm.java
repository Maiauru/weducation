package ru.edu.pgtk.weducation.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Класс форм итоговой аттестации.
 *
 * @author Воронин Леонид
 */
@Entity
@Table(name = "examforms")
public class ExamForm implements Serializable {

  @Id
  @Column(name = "exf_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  
}
