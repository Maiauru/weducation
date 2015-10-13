package ru.edu.pgtk.weducation.ejb;

import java.util.LinkedList;
import java.util.List;
import javax.ejb.Singleton;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 * Класс, для реализации различных популярных списков.
 * @author Воронин Леонид
 */
@Singleton
@Named("utilsEJB")
public class UtilsEJB {
  
  private static final List<SelectItem> items;
  
  static {
    items = new LinkedList<>();
    items.add(new SelectItem(0, "без оценки"));
    items.add(new SelectItem(1, "неудовлетворительно (1)"));
    items.add(new SelectItem(2, "неудовлетворительно (2)"));
    items.add(new SelectItem(3, "удовлетворительно"));
    items.add(new SelectItem(4, "хорошо"));
    items.add(new SelectItem(5, "отлично"));
    items.add(new SelectItem(10, "нет аттестации"));
    items.add(new SelectItem(11, "не изучал"));
    items.add(new SelectItem(12, "освобожден"));
    items.add(new SelectItem(13, "зачтено"));
  }
  
  public String getEnabledClass(final boolean enabled) {
    return enabled ? "enabled" : "disabled";
  }
  
  @Deprecated
  public List<SelectItem> getMarkList() {
    return items;
  }  
}
