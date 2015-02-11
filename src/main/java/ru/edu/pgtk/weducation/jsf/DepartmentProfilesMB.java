package ru.edu.pgtk.weducation.jsf;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import ru.edu.pgtk.weducation.ejb.DepartmentProfilesEJB;
import ru.edu.pgtk.weducation.entity.DepartmentProfile;

@ManagedBean(name = "departmentProfilesMB")
@ViewScoped
public class DepartmentProfilesMB extends GenericBean<DepartmentProfile> implements Serializable {

  @EJB
  private DepartmentProfilesEJB ejb;

  public void add() {
    item = new DepartmentProfile();
    edit = true;
  }

  public void save() {
    try {
      ejb.save(item);
      resetState();
    } catch (Exception e) {
      addMessage(e);
    }
  }

  public void confirmDelete() {
    try {
      if ((null != item) && delete) {
        ejb.delete(item);
      }
      resetState();
    } catch (Exception e) {
      addMessage(e);
    }
  }
}
