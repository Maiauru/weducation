package ru.edu.pgtk.weducation.reports.jsf;

import javax.ejb.Stateless;
import javax.inject.Named;
import ru.edu.pgtk.weducation.entity.GroupSemester;

/**
 * Класс для выдачи ссылок на ресурсы.
 *
 * Данный класс будет выдавать ссылки на ведомости и т.п. Планируется всю логику
 * по вычислению ссылок вынести в отдельный класс для централизованного
 * изменения в дальнейшем.
 *
 * @author Воронин Леонид
 */
@Stateless
@Named("linksMB")
public class LinksMB {
  
  private static final String prefix = "reports/group/";

  /**
   * Возвращает ссылку на сводную ведомость.
   *
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getConsolidatedMarksLink(final GroupSemester gs) {
    return prefix + gs.getGroup().getId() + "/consolidated/" + gs.getCourse()
      + "/" + gs.getSemester();
  }

  /**
   * Возвращает ссылку на зачетную ведомость.
   *
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getSemesterMarksLink(final GroupSemester gs) {
    return prefix + gs.getGroup().getId() + "/semestermarks/" + gs.getCourse()
      + "/" + gs.getSemester();
  }

  /**
   * Возвращает ссылку на ведомость сдачи курсовых проектов.
   *
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @param subjectCode код дисциплины по которой предусмотрен курсовой проект
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getCourseWorkMarksLink(final GroupSemester gs, final int subjectCode) {
    return prefix + gs.getGroup().getId() + "/cproject/" + gs.getCourse()
      + "/" + gs.getSemester() + "/" + subjectCode;
  }

  /**
   * Возвращает ссылку на экзаменационную ведомость.
   *
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @param subjectCode код дисциплины по которой предусмотрен экзамен
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getExamMarksLink(final GroupSemester gs, final int subjectCode) {
    return prefix + gs.getGroup().getId() + "/exam/" + gs.getCourse()
      + "/" + gs.getSemester() + "/" + subjectCode;
  }

  /**
   * Возвращает ссылку на ведомость аттестации за месяц.
   * 
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @param date Закодированные год и месяц в виде {@code year * 100 + month}
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getEmptyMonthMarks(final GroupSemester gs, final int date) {
    return prefix + gs.getGroup().getId() + "/monthmarks/empty/"
      + date / 100 + "/" + date % 100;
  }
  
  /**
   * Возвращает ссылку на ведомость успеваемости за месяц.
   * 
   * @param gs семестр группы, в виде объекта {@code GroupSemester}
   * @param date Закодированные год и месяц в виде {@code year * 100 + month}
   * @return ссылка на REST сервис генерации ведомости в виде строки
   */
  public String getFilledMonthMarks(final GroupSemester gs, final int date) {
    return prefix + gs.getGroup().getId() + "/monthmarks/filled/"
      + date / 100 + "/" + date % 100;    
  }
}
