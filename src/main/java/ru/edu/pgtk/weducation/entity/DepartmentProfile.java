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
@Table(name = "departmentprofiles")
public class DepartmentProfile implements Serializable {

  @Id
  @Column(name = "dpr_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "dpr_depcode", nullable = false)
  private Department department;

  @Transient
  private int departmentCode;

  @ManyToOne
  @JoinColumn(name = "dpr_spccode", nullable = false)
  private Speciality speciality;

  @Transient
  private int specialityCode;

  @Column(name = "dpr_extramural", nullable = false)
  private boolean extramural;

  @PostLoad
  private void updateCodes() {
    specialityCode = speciality.getId();
    departmentCode = department.getId();
  }

  public String getExtramuralString() {
    return extramural ? "заочная" : "очная";
  }

  public String getNameForList() {
    return department.getName() + " (" + speciality.getName()
            + ", " + getExtramuralString() + " форма)";
  }

  public int getId() {
    return id;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
    if (department != null) {
      departmentCode = department.getId();
    } else {
      departmentCode = 0;
    }
  }

  public Speciality getSpeciality() {
    return speciality;
  }

  public void setSpeciality(Speciality speciality) {
    this.speciality = speciality;
    if (speciality != null) {
      specialityCode = speciality.getId();
    } else {
      specialityCode = 0;
    }
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
