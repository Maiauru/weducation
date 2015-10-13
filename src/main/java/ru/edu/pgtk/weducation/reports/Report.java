package ru.edu.pgtk.weducation.reports;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

/**
 * Интерфейс для работы с различными отчетами.
 *
 * @author Воронин Леонид
 */
public interface Report {

  public enum Orientation {
    PORTRET,
    LANDSCAPE;
  }

  /**
   * Метод для добавления различных элементов в отчет.
   *
   * Позволяет добавлять в отчет различные таблицы, параграфы и т.п.
   *
   * @param element элемент отчета
   * @throws DocumentException
   */
  void add(Element element) throws DocumentException;

  /**
   * Возвращает содержимое PDF документа в виде массива байт.
   *
   * @return массив байт
   */
  byte[] getData();
}
