package ru.edu.pgtk.weducation.reports;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Утилитарный класс только для отчетов.
 * @author Воронин Леонид.
 */
class Utils {
  private static final String[] months = {"январь", "февраль", "март", "апрель", 
    "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
  
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
  
  private Utils() {
    throw new IllegalStateException("This constructor should not be called!");
  }
  
  /**
   * Возвращает название месяца по его числу.
   * @param month месяц в виде числа (от 1 до 12)
   * @return Строковое название месяца (например, январь для 1)
   */
  public static String getMonthString(final int month) {
    try {
      return months[month-1];
    } catch (Exception e) {
      throw new IllegalArgumentException("Wrong month value " + month);
    }
  }
  
  public static String getDateString(final Date date) {
    return (null == date) ? "Date is NULL!" : sdf.format(date);
  }
  
}
