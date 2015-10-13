package ru.edu.pgtk.weducation.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс практики. Помимо дисциплин, в модуль может также входить и практика.
 *
 * @author Воронин Леонид
 */
public class XMLPractice {

  private String index = "";             // индекс (ЕН, ОП)
  private String name = "";              // Наименование практики
  private XMLModule module = null;          // родительский элемент (модуль, которому принадлежит практика)
  private List<XMLPracticeLoad> load = new ArrayList<>(); // список нагрузки по семестрам

  /**
   * Конструктор без параметров.
   */
  public XMLPractice() {
    super();
  }

  /**
   * Конструктор с параметрами.
   *
   * @param module Модуль для которого создается практика
   * @param index индекс практики (Всякие там ПР.02 и т.п.)
   * @param name наименование практики
   */
  public XMLPractice(XMLModule module, String index, String name) {
    super();
    this.module = module;
    this.index = index;
    this.name = name;
  }
  
  /**
   * Возвращает полное наименование практики.
   * @return полное наименование в виде строки.
   */
  public String getFullName() {
    return index + " " + name;
  }
  
  @Override
  public String toString() {
    return "Practice [index=" + index + ", name=" + name + ", load=" + load + "]";
  }

  public void addLoad(XMLPracticeLoad item) {
    load.add(item);
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public XMLModule getModule() {
    return module;
  }

  public void setModule(XMLModule module) {
    this.module = module;
  }

  public List<XMLPracticeLoad> getLoad() {
    return load;
  }

  public void setLoad(List<XMLPracticeLoad> load) {
    this.load = load;
  }
}
