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
 * Класс дисциплины или междисциплинарного курса
 *
 */
@Entity
@Table(name = "subjects")
public class Subject implements Serializable {

  @Id
  @Column(name = "sub_pcode")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "sub_fullname", nullable = false)
  private String fullName;

  @Column(name = "sub_shortname")
  private String shortName;

  @ManyToOne
  @JoinColumn(name = "sub_plncode", nullable = false)
  private StudyPlan plan;

  @Transient
  private int planCode;

  @ManyToOne
  @JoinColumn(name = "sub_modcode")
  private StudyModule module;

  @Transient
  private int moduleCode;

  @PostLoad
  private void updateCodes() {
    planCode = plan.getId();
    if (module != null) {
      moduleCode = module.getId();
    }
  }

  public int getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public StudyPlan getPlan() {
    return plan;
  }

  public void setPlan(StudyPlan plan) {
    this.plan = plan;
  }

  public StudyModule getModule() {
    return module;
  }

  public void setModule(StudyModule module) {
    this.module = module;
  }

  public int getPlanCode() {
    return planCode;
  }

  public void setPlanCode(int planCode) {
    this.planCode = planCode;
  }

  public int getModuleCode() {
    return moduleCode;
  }

  public void setModuleCode(int moduleCode) {
    this.moduleCode = moduleCode;
  }
}
