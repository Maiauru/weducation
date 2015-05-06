package ru.edu.pgtk.weducation.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import ru.edu.pgtk.weducation.ejb.PersonsEJB;
import ru.edu.pgtk.weducation.entity.Account;
import ru.edu.pgtk.weducation.entity.ForeignLanguage;
import ru.edu.pgtk.weducation.entity.Person;

@ManagedBean(name = "personsMB")
@ViewScoped
public class PersonsMB extends GenericBean<Person> implements Serializable {

  @EJB
  private transient PersonsEJB ejb;
  @ManagedProperty(value = "#{sessionMB.user}")
  private transient Account user;

  private int personCode;
  private String name;
  private boolean filter;

  @PostConstruct
  private void checkAccount() {
    resetState();
    // Если пользователь неавторизован, то выдаем ошибку и запрещаем работу!
    if (null == user) {
      error = true;
    }
  }
  
  public ForeignLanguage[] getLanguages() {
    return ForeignLanguage.values();
  }

  public int getPersonCode() {
    return personCode;
  }

  public void setPersonCode(int personCode) {
    this.personCode = personCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isFilter() {
    return filter;
  }

  public void toggleFilter() {
    if ((null != name) && !name.isEmpty()) {
      filter = !filter;
    }
    if (!filter) {
      name = null;
    }
  }

  public String getFilterButtonLabel() {
    return (filter) ? "Новый поиск" : "Поиск";
  }

  public void loadPerson() {
    try {
      if (personCode > 0) {
        item = ejb.get(personCode);
        details = true;
      }
    } catch (Exception e) {
      addMessage(e);
    }
  }

  public List<Person> getPersonList() {
    if (filter && (null != name)) {
      return ejb.findByName(name);
    } else {
      return new ArrayList<>();
    }
  }

  public void add() {
    item = new Person();
    edit = true;
  }

  public void save() {
    try {
      ejb.save(item);
      edit = false;
      details = true;
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

  public Account getUser() {
    return user;
  }

  public void setUser(Account user) {
    this.user = user;
  }
}
