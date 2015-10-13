package ru.edu.pgtk.weducation.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import ru.edu.pgtk.weducation.ejb.PersonsEJB;
import ru.edu.pgtk.weducation.entity.ForeignLanguage;
import ru.edu.pgtk.weducation.entity.Person;
import static ru.edu.pgtk.weducation.jsf.Utils.addMessage;

@Named("personsMB")
@ViewScoped
public class PersonsMB extends GenericBean<Person> implements Serializable {

  long serialVersionUID = 0L;

  @Inject
  private transient PersonsEJB ejb;

  private int personCode;
  private String name;
  private boolean filter;

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
    String text = name.trim();
    if ((null != text) && !text.isEmpty()) {
      this.name = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    } else {
      this.name = null;
    }
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
    return filter ? "Новый поиск" : "Поиск";
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

  @Override
  public void newItem() {
    item = new Person();
  }

  @Override
  public void deleteItem() {
    if ((null != item) && delete) {
      ejb.delete(item);
    }
  }

  @Override
  public void saveItem() {
    ejb.save(item);
    details = true;
  }
}
