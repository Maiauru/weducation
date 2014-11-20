package ru.edu.pgtk.weducation.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Класс законного представителя
 *
 * @author Воронин Леонид
 */
@Entity
@Table(name = "delegates")
public class Delegate implements Serializable {

  @Id
  @Column(name = "dlg_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "dlg_fullname", nullable = false, length = 128)
  private String fullName;

  @Column(name = "dlg_hphone", nullable = false, length = 20)
  private String homePhone;

  @Column(name = "dlg_mphone", nullable = false, length = 20)
  private String mobilePhone;

  @Column(name = "dlg_wphone", nullable = false, length = 20)
  private String workPhone;

  @Column(name = "dlg_job", nullable = false, length = 255)
  private String job;

  @Column(name = "dlg_description", length = 255)
  private String description;
  
  @ManyToOne
  @JoinColumn(name = "dlg_psncode", nullable = false)
  private Person person;

  public int getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getHomePhone() {
    return homePhone;
  }

  public void setHomePhone(String homePhone) {
    this.homePhone = homePhone;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public String getWorkPhone() {
    return workPhone;
  }

  public void setWorkPhone(String workPhone) {
    this.workPhone = workPhone;
  }

  public String getJob() {
    return job;
  }

  public void setJob(String job) {
    this.job = job;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }
}
