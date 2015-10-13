package ru.edu.pgtk.weducation.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для реализации утилит, используемых только для классов-сущностей.
 * @author Воронин Леонид
 */
class Utils {
  
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
  
  private Utils() {
    throw new IllegalStateException("You should not call this constructor!");
  }
  
  public static String getBooleanString(final boolean val) {
    return val ? "да" : "нет";
  }
  
  public static String getDateString(final Date date) {
    return (date != null) ? sdf.format(date) : null;
  }
}
