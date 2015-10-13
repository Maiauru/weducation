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
import ru.edu.pgtk.weducation.utils.Utils;

@Entity
@Table(name = "plans")
public class StudyPlan implements Serializable {

  @Id
  @Column(name = "pln_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "pln_name", nullable = false, length = 255)
  private String name;

  @Column(name = "pln_spcname", nullable = false, length = 255)
  private String specialityName;

  @Column(name = "pln_spckey", nullable = false, length = 20)
  private String specialityKey;

  @Column(name = "pln_kvalification", nullable = false, length = 128)
  private String kvalification;

  @Column(name = "pln_specialization", nullable = false, length = 128)
  private String specialization;

  @Column(name = "pln_extramural", nullable = false)
  private boolean extramural;
  
  @Column(name = "pln_beginyear", nullable = false)
  private int beginYear;
  
  @Column(name = "pln_years", nullable = false)
  private int years;
  
  @Column(name = "pln_months", nullable = false)
  private int months;
  
  @ManyToOne
  @JoinColumn(name = "pln_spccode", nullable = false)
  private Speciality speciality;

  @Transient
  private int specialityCode;

  @PostLoad
  private void updateCodes() {
    specialityCode = speciality.getId();
  }

  public int getId() {
    return id;
  }
  
  public String getLength() {
    return Utils.getYearString(years) + " " + Utils.getMonthString(months);
  }

  public String getExtramural() {
    return (extramural ? "заочная" : "очная") + " форма";
  }

  public String getNameForList() {
    String localName = name;
    if (speciality != null) {
      localName = speciality.getName();
    }
    return localName + " (" + beginYear + "-й год, " + getExtramural() + ")";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isExtramural() {
    return extramural;
  }

  public void setExtramural(boolean extramural) {
    this.extramural = extramural;
  }

  public Speciality getSpeciality() {
    return speciality;
  }

  public void setSpeciality(Speciality speciality) {
    this.speciality = speciality;
  }

  public int getSpecialityCode() {
    return specialityCode;
  }

  public void setSpecialityCode(int specialityCode) {
    this.specialityCode = specialityCode;
  }

  public int getYears() {
    return years;
  }

  public void setYears(int years) {
    this.years = years;
  }

  public int getMonths() {
    return months;
  }

  public void setMonths(int months) {
    this.months = months;
  }

  public String getSpecialityName() {
    return specialityName;
  }

  public void setSpecialityName(String specialityName) {
    this.specialityName = specialityName;
  }

  public String getSpecialityKey() {
    return specialityKey;
  }

  public void setSpecialityKey(String specialityKey) {
    this.specialityKey = specialityKey;
  }

  public String getKvalification() {
    return kvalification;
  }

  public void setKvalification(String kvalification) {
    this.kvalification = kvalification;
  }

  public String getSpecialization() {
    return specialization;
  }

  public void setSpecialization(String specialization) {
    this.specialization = specialization;
  }

  public int getBeginYear() {
    return beginYear;
  }

  public void setBeginYear(int beginYear) {
    this.beginYear = beginYear;
  }
}
