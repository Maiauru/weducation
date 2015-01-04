package ru.edu.pgtk.weducation.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Класс для хранения профилей обучения.
 *
 * @author Воронин Леонид
 */
@Entity
@Table(name = "studyprofiles")
public class StudyProfile implements Serializable {

  @Id
  @Column(name = "stp_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "stp_depcode", nullable = false)
  private Department department;

  @Transient
  private int departmentCode;

  @ManyToOne
  @JoinColumn(name = "stp_spccode", nullable = false)
  private Speciality speciality;

  @Transient
  private int specialityCode;

  @Column(name = "stp_extramural", nullable = false)
  private boolean extramural;

  @PostLoad
  private void updateCodes() {
    specialityCode = speciality.getId();
    departmentCode = department.getId();
  }

  public int getId() {
    return id;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public Speciality getSpeciality() {
    return speciality;
  }

  public void setSpeciality(Speciality speciality) {
    this.speciality = speciality;
  }

  public int getDepartmentCode() {
    return departmentCode;
  }

  public void setDepartmentCode(int departmentCode) {
    this.departmentCode = departmentCode;
  }

  public int getSpecialityCode() {
    return specialityCode;
  }

  public void setSpecialityCode(int specialityCode) {
    this.specialityCode = specialityCode;
  }

  public boolean isExtramural() {
    return extramural;
  }

  public void setExtramural(boolean extramural) {
    this.extramural = extramural;
  }
}
