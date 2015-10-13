package ru.edu.pgtk.weducation.reports;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import ru.edu.pgtk.weducation.ejb.CourseWorkMarksEJB;
import ru.edu.pgtk.weducation.ejb.GroupSemestersEJB;
import ru.edu.pgtk.weducation.ejb.MonthMarksEJB;
import ru.edu.pgtk.weducation.ejb.PracticMarksEJB;
import ru.edu.pgtk.weducation.ejb.PracticsEJB;
import ru.edu.pgtk.weducation.ejb.SemesterMarksEJB;
import ru.edu.pgtk.weducation.ejb.StudyCardsEJB;
import ru.edu.pgtk.weducation.ejb.StudyGroupsEJB;
import ru.edu.pgtk.weducation.ejb.SubjectsEJB;
import ru.edu.pgtk.weducation.entity.CourseWorkMark;
import ru.edu.pgtk.weducation.entity.GroupSemester;
import ru.edu.pgtk.weducation.entity.MonthMark;
import ru.edu.pgtk.weducation.entity.Practic;
import ru.edu.pgtk.weducation.entity.PracticMark;
import ru.edu.pgtk.weducation.entity.School;
import ru.edu.pgtk.weducation.entity.SemesterMark;
import ru.edu.pgtk.weducation.entity.StudyCard;
import ru.edu.pgtk.weducation.entity.StudyGroup;
import ru.edu.pgtk.weducation.entity.Subject;
import static ru.edu.pgtk.weducation.reports.PDFUtils.getParagraph;
import static ru.edu.pgtk.weducation.reports.PDFUtils.getPt;
import static ru.edu.pgtk.weducation.reports.Utils.getMonthString;
import ru.edu.pgtk.weducation.reports.dao.MissingsDAO;
import ru.edu.pgtk.weducation.reports.entity.ReportMissing;

/**
 * EJB компонент для генерации различных отчетов по группе.
 */
@Path("/group")
@Stateless
@RequestScoped
public class GroupReportsEJB {

  @Inject
  private transient SubjectsEJB subjects;
  @Inject
  private transient PracticsEJB practics;
  @Inject
  private transient StudyGroupsEJB groups;
  @Inject
  private transient StudyCardsEJB cards;
  @Inject
  private transient MonthMarksEJB marks;
  @Inject
//  private transient MissingsEJB missings;
  private transient MissingsDAO missings;
  @Inject
  private transient CourseWorkMarksEJB cmarks;
  @Inject
  private transient PracticMarksEJB pmarks;
  @Inject
  private transient SemesterMarksEJB smarks;
  @Inject
  private transient GroupSemestersEJB groupSemesters;
  @Inject
  private School school;
  private String schoolName;
  private boolean error;
  private String errorMessage;
  private BaseFont baseFont;
  private Font bigFont;
  private Font regularFont;
  private Font smallFont;

  @PostConstruct
  private void initBean() {
    try {
      baseFont = BaseFont.createFont("fonts/times.ttf", BaseFont.IDENTITY_H,
        BaseFont.EMBEDDED);
      smallFont = new Font(baseFont, 7);
      regularFont = new Font(baseFont, 10);
      bigFont = new Font(baseFont, 16);
      if ((null == school) || (null == school.getFullName())) {
        errorMessage = "School name or school is null!";
        error = true;
      } else {
        schoolName = school.getFullName().replace("Прокопьевский", "\nПрокопьевский");
      }
    } catch (DocumentException | IOException e) {
      error = true;
      errorMessage = e.getClass().getSimpleName() + " with message " + e.getMessage();
    }
  }

  /**
   * Экзаменационная ведомость.
   *
   * @param groupCode код группы
   * @param course курс
   * @param semester семестр
   * @param subjectCode код дисциплины
   * @return экзаменационная ведомость в виде PDF документа
   */
  @GET
  @Path("{groupId: \\d+}/exam/{course: \\d+}/{semester: \\d+}/{subjectId: \\d+}")
  @Produces("application/pdf")
  public Response examReport(@PathParam("groupId") int groupCode, @PathParam("course") int course,
    @PathParam("semester") int semester, @PathParam("subjectId") int subjectCode) {
    try {
      StudyGroup grp = groups.get(groupCode);
      Subject sub = subjects.get(subjectCode);
      GroupSemester gs = groupSemesters.get(grp, course, semester);
      if (null == gs) {
        return Response.noContent().build();
      }
      ResponseBuilder response = Response.ok(exam(sub, gs));
      return response.build();
    } catch (EJBException e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Ведомость сдачи курсовых проектов
   *
   * @param groupCode код группы
   * @param course курс
   * @param semester семестр
   * @param subjectCode код дисциплины
   * @return ведомость сдачи курсовых проектов в виде PDF документа.
   */
  @GET
  @Path("{groupId: \\d+}/cproject/{course: \\d+}/{semester: \\d+}/{subjectId: \\d+}")
  @Produces("application/pdf")
  public Response courseWorkReport(@PathParam("groupId") int groupCode, @PathParam("course") int course,
    @PathParam("semester") int semester, @PathParam("subjectId") int subjectCode) {
    try {
      StudyGroup grp = groups.get(groupCode);
      Subject sub = subjects.get(subjectCode);
      GroupSemester gs = groupSemesters.get(grp, course, semester);
      if (null == gs) {
        return Response.noContent().build();
      }
      return Response.ok(courseWork(sub, gs)).build();
    } catch (EJBException e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Ведомость аттестации за месяц.
   *
   * @param groupCode код группы
   * @param year год
   * @param month месяц
   * @return ведомость аттестации за месяц в виде PDF документа
   */
  @GET
  @Path("{groupId: \\d+}/monthmarks/empty/{year: \\d+}/{month: \\d+}")
  @Produces("application/pdf")
  public Response emptyMonthReport(@PathParam("groupId") int groupCode,
    @PathParam("year") int year, @PathParam("month") int month) {
    try {
      StudyGroup grp = groups.get(groupCode);
      GroupSemester gs = groupSemesters.getByMonth(grp, year, month);
      if (null == gs) {
        return Response.noContent().build();
      }
      ResponseBuilder response = Response.ok(monthMarks(gs, year, month, true));
      return response.build();
    } catch (EJBException e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Ведомость успеваемсти за месяц.
   *
   * @param groupCode код группы
   * @param year год
   * @param month месяц
   * @return ведомость успеваемости в виде PDF документа
   */
  @GET
  @Path("{groupId: \\d+}/monthmarks/filled/{year: \\d+}/{month: \\d+}")
  @Produces("application/pdf")
  public Response filledMonthReport(@PathParam("groupId") int groupCode,
    @PathParam("year") int year, @PathParam("month") int month) {
    try {
      StudyGroup grp = groups.get(groupCode);
      GroupSemester gs = groupSemesters.getByMonth(grp, year, month);
      if (null == gs) {
        return Response.noContent().build();
      }
      return Response.ok(monthMarks(gs, year, month, false)).build();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Зачетная ведомость.
   *
   * @param groupCode код группы
   * @param course курс
   * @param semester семестр
   * @return зачетная ведомость в виде PDF документа.
   */
  @GET
  @Path("{groupId: \\d+}/semestermarks/{course: \\d+}/{semester: \\d+}")
  @Produces("application/pdf")
  public Response semesterMarksReport(@PathParam("groupId") int groupCode,
    @PathParam("course") int course, @PathParam("semester") int semester) {
    try {
      StudyGroup grp = groups.get(groupCode);
      GroupSemester gs = groupSemesters.get(grp, course, semester);
      if (null == gs) {
        Response.noContent().build();
      }
      return Response.ok(semesterMarks(gs, true)).build();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }

  @GET
  @Path("{groupId: \\d+}/consolidated/{course: \\d+}/{semester: \\d+}")
  @Produces("application/pdf")
  public Response consolidatedReport(@PathParam("groupId") int groupCode,
    @PathParam("course") int course, @PathParam("semester") int semester) {
    try {
      StudyGroup grp = groups.get(groupCode);
      GroupSemester gs = groupSemesters.get(grp, course, semester);
      if (null == gs) {
        Response.noContent().build();
      }
      return Response.ok(consolidatedSemesterMarks(gs, false)).build();
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new NotFoundException(e.getMessage());
    }
  }
  
  
  /**
   * Формирует ведомость сдачи курсовых проектов для группы
   *
   * @param subject дисциплина
   * @param gs Запись о курсе и семестре группы
   * @return
   */
  private byte[] courseWork(final Subject subject, final GroupSemester gs) {
    try {
      // создадим новый лист с размерами A4 и отступами слева и справа по 5 мм, а сверху и снизу - по 10
      Report document = PDFReport.create(Report.Orientation.PORTRET, "Ведомость сдачи курсовых проектов");
      document.add(getParagraph(schoolName, regularFont, Paragraph.ALIGN_CENTER));
      document.add(getParagraph("\nВедомость сдачи курсовых проектов", bigFont, Paragraph.ALIGN_CENTER));
      String description = "группы " + gs.getGroup().getName() + " " + gs.getCourse() + "-го курса "
        + (gs.getGroup().isExtramural() ? "заочной" : "очной") + " формы обучения за " + gs.getSemester() + "-й семестр.";
      document.add(getParagraph(description, regularFont, Paragraph.ALIGN_CENTER));
      String speciality = "Специальность: " + gs.getGroup().getPlan().getSpecialityName();
      document.add(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER));
      document.add(getParagraph("Дисциплина: " + subject.getFullName(), regularFont, Paragraph.ALIGN_CENTER));
      // Теперь можно создавать таблицу
      PdfPTable table = new PdfPTable(5);
      table.setWidthPercentage(100.0f);
      table.setSpacingBefore(getPt(5));
      table.setSpacingAfter(getPt(5));
      // Создадим массив целых чисел для указания размерности столбцов
      // Установим размер столбца ФИО в пять раз больше размером!
      table.setWidths(new int[]{1, 6, 14, 4, 4});
      // Добавляем строки таблицы
      PdfPCell numberCell = new PdfPCell(getParagraph("№", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(numberCell);
      PdfPCell nameCell = new PdfPCell(getParagraph("Фамилия имя отчество", regularFont, Paragraph.ALIGN_CENTER));
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(nameCell);
      PdfPCell themeCell = new PdfPCell(getParagraph("Тема курсового проекта", regularFont, Paragraph.ALIGN_CENTER));
      themeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      themeCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(themeCell);
      PdfPCell markCell = new PdfPCell(getParagraph("Оценка", regularFont, Paragraph.ALIGN_CENTER));
      markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(markCell);
      PdfPCell signCell = new PdfPCell(getParagraph("Подпись руководителя", regularFont, Paragraph.ALIGN_CENTER));
      signCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      signCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(signCell);
      int row = 1;
      PdfPCell cell = new PdfPCell();
      for (CourseWorkMark cm : cmarks.fetchAll(gs.getGroup(), subject, gs.getCourse(), gs.getSemester())) {
        StudyCard sc = cm.getCard();
        if (!sc.isRemanded() && sc.isActive()) {
          numberCell = new PdfPCell(getParagraph("" + row++, regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(numberCell);
          nameCell = new PdfPCell(getParagraph(sc.getPerson().getShortName(),
            regularFont, Paragraph.ALIGN_LEFT));
          table.addCell(nameCell);
          themeCell = new PdfPCell(getParagraph(cm.getTheme(), regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(themeCell);
          for (int i = 0; i < 2; i++) {
            table.addCell(cell);
          }
        }
      }
      document.add(table);
      document.add(getParagraph("\nРуководитель ____________________  ________________________________________",
        regularFont, Paragraph.ALIGN_LEFT));
      document.add(getParagraph("                                                     (подпись)"
        + "                                                                               (ФИО)",
        smallFont, Paragraph.ALIGN_LEFT));
      return document.getData();
    } catch (DocumentException e) {
      throw new InternalServerErrorException("DocumentException: " + e.getMessage());
    }
  }

  /**
   * Формирует экзаменационную ведомость для группы студентов.
   *
   * @param subject дисциплина
   * @param gs Семестр группы
   * @return
   */
  private byte[] exam(final Subject subject, final GroupSemester gs) {
    if (error) {
      throw new InternalServerErrorException(errorMessage);
    }
    try {
      Report report = PDFReport.create(Report.Orientation.PORTRET, "Экзаменационная ведомость");
      report.add(getParagraph(schoolName, regularFont, Paragraph.ALIGN_CENTER));
      report.add(getParagraph("\nЭкзаменационная ведомость", bigFont, Paragraph.ALIGN_CENTER));
      report.add(getParagraph("(для семестровых экзаменов)", regularFont, Paragraph.ALIGN_CENTER));
      String description = "группы " + gs.getGroup().getName() + " " + gs.getCourse() + "-го курса "
        + (gs.getGroup().isExtramural() ? "заочной" : "очной") + " формы обучения за " + gs.getSemester() + "-й семестр.";
      report.add(getParagraph(description, regularFont, Paragraph.ALIGN_CENTER));
      String speciality = "Специальность: " + gs.getGroup().getPlan().getSpecialityName();
      report.add(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER));
      report.add(getParagraph("Дисциплина: " + subject.getFullName(), regularFont, Paragraph.ALIGN_CENTER));
      // Теперь можно создавать таблицу
      PdfPTable table = new PdfPTable(7);
      table.setWidthPercentage(100.0f);
      table.setSpacingBefore(getPt(5));
      table.setSpacingAfter(getPt(5));
      // Создадим массив целых чисел для указания размерности столбцов
      // Установим размер столбца ФИО в пять раз больше размером!
      table.setWidths(new int[]{1, 5, 2, 3, 3, 3, 4});
      // Добавляем строки таблицы
      PdfPCell numberCell = new PdfPCell(getParagraph("№", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setRowspan(2);
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(numberCell);
      PdfPCell nameCell = new PdfPCell(getParagraph("Фамилия имя отчество", regularFont, Paragraph.ALIGN_CENTER));
      nameCell.setRowspan(2);
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(nameCell);
      PdfPCell biletCell = new PdfPCell(getParagraph("№ билета", regularFont, Paragraph.ALIGN_CENTER));
      biletCell.setRowspan(2);
      biletCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      biletCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(biletCell);
      PdfPCell markCell = new PdfPCell(getParagraph("Оценка за экзамен", regularFont, Paragraph.ALIGN_CENTER));
      markCell.setColspan(3);
      markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(markCell);
      PdfPCell signCell = new PdfPCell(getParagraph("Подпись экзаменатора", regularFont, Paragraph.ALIGN_CENTER));
      signCell.setRowspan(2);
      signCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      signCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(signCell);
      PdfPCell cell = new PdfPCell(getParagraph("письменно", regularFont, Paragraph.ALIGN_CENTER));
      cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(cell);
      cell = new PdfPCell(getParagraph("устно", regularFont, Paragraph.ALIGN_CENTER));
      cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(cell);
      cell = new PdfPCell(getParagraph("общая", regularFont, Paragraph.ALIGN_CENTER));
      cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(cell);
      int row = 1;
      cell = new PdfPCell();
      for (StudyCard sc : cards.findByGroup(gs.getGroup())) {
        if (!sc.isRemanded() && sc.isActive()) {
          numberCell = new PdfPCell(getParagraph(String.valueOf(row++), regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(numberCell);
          nameCell = new PdfPCell(getParagraph(sc.getPerson().getShortName(),
            regularFont, Paragraph.ALIGN_LEFT));
          table.addCell(nameCell);
          for (int i = 0; i < 5; i++) {
            table.addCell(cell);
          }
        }
      }
      report.add(table);
      report.add(getParagraph("Дата проведения экзамена \"_____\" _________________  ____________г.",
        regularFont, Paragraph.ALIGN_LEFT));
      report.add(getParagraph("\nПисьменного ____________________   начало ____________________   окончание ____________________",
        regularFont, Paragraph.ALIGN_LEFT));
      report.add(getParagraph("\nУстного         ____________________   начало ____________________   окончание ____________________",
        regularFont, Paragraph.ALIGN_LEFT));
      report.add(getParagraph("\nВсего на проведение экзамена: __________________ час ________________ мин",
        regularFont, Paragraph.ALIGN_LEFT));
      report.add(getParagraph("\nЭкзаменатор(ы) ____________________  ________________________________________",
        regularFont, Paragraph.ALIGN_LEFT));
      report.add(getParagraph("                                                     (подпись)"
        + "                                                                               (ФИО)",
        smallFont, Paragraph.ALIGN_LEFT));
      return report.getData();
    } catch (DocumentException e) {
      throw new InternalServerErrorException("DocumentException: " + e.getMessage());
    }
  }

  /**
   * Формирует ведомость успеваемости за месяц для группы.
   *
   * @param gs Определенный семестр группы
   * @param year год, за который формируем ведомость
   * @param month месяц, за который формируем ведомость
   * @param empty ведомость будет без оценок (для заполнения)
   * @return массив байт, представляющий собой PDF сгенерированный документ.
   * @Throws InternalServerErrorException при невозможности определить семестр и
   * курс по указанному году и месяцу, а также если вместо группы null
   */
  private byte[] monthMarks(final GroupSemester gs, final int year, final int month, final boolean empty) {
    try {
      String title = "Ведомость " + (empty ? "аттестации" : "успеваемости");
      Report document = PDFReport.create(Report.Orientation.PORTRET, title);
      document.add(getParagraph(schoolName, regularFont, Paragraph.ALIGN_CENTER));
      document.add(getParagraph(title, bigFont, Paragraph.ALIGN_CENTER));
      String description = "группы " + gs.getGroup().getName() + " " + gs.getCourse() + "-го курса "
        + (gs.getGroup().isExtramural() ? "заочной" : "очной") + " формы обучения за " + getMonthString(month) + " " + year + "-го года.";
      document.add(getParagraph(description, regularFont, Paragraph.ALIGN_CENTER));
      String speciality = "Специальность: " + gs.getGroup().getPlan().getSpecialityName();
      document.add(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER));
      // Настало время узнать список дисциплин
      List<Subject> subjectList = subjects.fetch(gs.getGroup(), gs.getCourse(), gs.getSemester());
      // Теперь можно создавать таблицу
      // Количество полей будет на 5 больше, чем кол-во дисциплин
      PdfPTable table = new PdfPTable(subjectList.size() + 5);
      table.setWidthPercentage(100.0f);
      table.setSpacingBefore(getPt(5));
      table.setSpacingAfter(getPt(5));
      // Создадим массив целых чисел для указания размерности столбцов
      // Установим размер столбца ФИО в пять раз больше размером!
      int[] sizes = new int[subjectList.size() + 5];
      for (int i = 0; i < sizes.length; i++) {
        sizes[i] = (i == 1) ? 5 : 1;
      }
      table.setWidths(sizes);
      // Добавляем строки таблицы
      PdfPCell numberCell = new PdfPCell(getParagraph("№", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setRowspan(2);
      numberCell.setMinimumHeight(getPt(20));
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(numberCell);
      PdfPCell nameCell = new PdfPCell(getParagraph("Фамилия имя отчество", regularFont, Paragraph.ALIGN_CENTER));
      nameCell.setRowspan(2);
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nameCell.setMinimumHeight(getPt(20));
      nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(nameCell);
      for (Subject sb : subjectList) {
        PdfPCell cell = new PdfPCell(getParagraph(sb.getShortName(), smallFont, Paragraph.ALIGN_CENTER));
        cell.setRowspan(2);
        cell.setRotation(90);
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков
      PdfPCell missingCell = new PdfPCell(getParagraph("Пропуски", regularFont, Paragraph.ALIGN_CENTER));
      missingCell.setColspan(3);
      missingCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(missingCell);
      PdfPCell legalCell = new PdfPCell(getParagraph("Уважительных", smallFont, Paragraph.ALIGN_CENTER));
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      table.addCell(legalCell);
      PdfPCell illegalCell = new PdfPCell(getParagraph("Неуважительных", smallFont, Paragraph.ALIGN_CENTER));
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      table.addCell(illegalCell);
      PdfPCell allCell = new PdfPCell(getParagraph("Всего", smallFont, Paragraph.ALIGN_CENTER));
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      table.addCell(allCell);
      int row = 1;
      int legal = 0;
      int illegal = 0;
      for (StudyCard sc : cards.findByGroup(gs.getGroup())) {
        if (!sc.isRemanded() && sc.isActive()) {
          numberCell = new PdfPCell(getParagraph(String.valueOf(row++), regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(numberCell);
          nameCell = new PdfPCell(getParagraph(sc.getPerson().getShortName(),
            regularFont, Paragraph.ALIGN_LEFT));
          table.addCell(nameCell);
          for (Subject sb : subjectList) {
            PdfPCell cell = new PdfPCell();
            if (!empty) {
              try {
                MonthMark mark = marks.get(sc, sb, year, month);
                cell.addElement(getParagraph(String.valueOf(mark.getMark()), regularFont, Paragraph.ALIGN_CENTER));
              } catch (Exception e) {
                // Ничего не делаем, ячейка просто будет пустой
              }
            }
            table.addCell(cell);
          }
          // Кол-во пропусков
          legalCell = new PdfPCell();
          illegalCell = new PdfPCell();
          allCell = new PdfPCell();
          if (!empty) {
            ReportMissing m = missings.getMonthMissings(sc, year, month);
            legalCell.addElement(getParagraph(String.valueOf(m.getLegal()), regularFont, Paragraph.ALIGN_CENTER));
            illegalCell.addElement(getParagraph(String.valueOf(m.getIllegal()), regularFont, Paragraph.ALIGN_CENTER));
            allCell.addElement(getParagraph(String.valueOf(m.getAll()), regularFont, Paragraph.ALIGN_CENTER));
            legal += m.getLegal();
            illegal = m.getIllegal();
          }
          table.addCell(legalCell);
          table.addCell(illegalCell);
          table.addCell(allCell);
        }
      }
      numberCell = new PdfPCell(getParagraph("Подписи преподавателей", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setColspan(2);
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      numberCell.setMinimumHeight(getPt(20));
      table.addCell(numberCell);
      for (int i = 0; i < subjectList.size(); i++) {
        PdfPCell cell = new PdfPCell();
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков итоговое
      legalCell = new PdfPCell();
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      legalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      legalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      illegalCell = new PdfPCell();
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      illegalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      illegalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      allCell = new PdfPCell();
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      allCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      allCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      if (!empty) {
        legalCell.addElement(getParagraph(String.valueOf(legal), regularFont, Paragraph.ALIGN_CENTER));
        illegalCell.addElement(getParagraph(String.valueOf(illegal), regularFont, Paragraph.ALIGN_CENTER));
        allCell.addElement(getParagraph(String.valueOf(legal + illegal), regularFont, Paragraph.ALIGN_CENTER));
      }
      table.addCell(legalCell);
      table.addCell(illegalCell);
      table.addCell(allCell);
      document.add(table);
      document.add(getParagraph("\n                Зав. отделением ________________________________",
        regularFont, Paragraph.ALIGN_LEFT));
      return document.getData();
    } catch (DocumentException e) {
      throw new InternalServerErrorException("DocumentException: " + e.getMessage());
    }
  }

  /**
   * Формирует зачетную ведомость для группы.
   *
   * @param gs определенный семестр группы
   * @param empty ведомость будет без оценок (для заполнения)
   * @return массив байт, представляющий собой PDF сгенерированный документ.
   * @Throws InternalServerErrorException если вместо группы null
   */
  private byte[] semesterMarks(final GroupSemester gs, final boolean empty) {
    try {
      String title = "Зачетная ведомость";
      Report document = PDFReport.create(Report.Orientation.PORTRET, title);
      document.add(getParagraph(schoolName, regularFont, Paragraph.ALIGN_CENTER));
      document.add(getParagraph(title, bigFont, Paragraph.ALIGN_CENTER));
      String description = "группы " + gs.getGroup().getName() + " " + gs.getCourse() + "-го курса "
        + (gs.getGroup().isExtramural() ? "заочной" : "очной") + " формы обучения за " + gs.getSemester() + "-й семестр.";
      document.add(getParagraph(description, regularFont, Paragraph.ALIGN_CENTER));
      String speciality = "Специальность: " + gs.getGroup().getPlan().getSpecialityName();
      document.add(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER));
      // Настало время узнать список дисциплин
      List<Subject> subjectList = subjects.fetch(gs.getGroup(), gs.getCourse(), gs.getSemester());
      // Теперь можно создавать таблицу
      // Количество полей будет на 5 больше, чем кол-во дисциплин
      PdfPTable table = new PdfPTable(subjectList.size() + 5);
      table.setWidthPercentage(100.0f);
      table.setSpacingBefore(getPt(5));
      table.setSpacingAfter(getPt(5));
      // Создадим массив целых чисел для указания размерности столбцов
      // Установим размер столбца ФИО в пять раз больше размером!
      int[] sizes = new int[subjectList.size() + 5];
      for (int i = 0; i < sizes.length; i++) {
        sizes[i] = (i == 1) ? 5 : 1;
      }
      table.setWidths(sizes);
      // Добавляем строки таблицы
      PdfPCell numberCell = new PdfPCell(getParagraph("№", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setRowspan(2);
      numberCell.setMinimumHeight(getPt(20));
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(numberCell);
      PdfPCell nameCell = new PdfPCell(getParagraph("Фамилия имя отчество", regularFont, Paragraph.ALIGN_CENTER));
      nameCell.setRowspan(2);
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nameCell.setMinimumHeight(getPt(20));
      nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(nameCell);
      for (Subject sb : subjectList) {
        PdfPCell cell = new PdfPCell(getParagraph(sb.getShortName(), smallFont, Paragraph.ALIGN_CENTER));
        cell.setRowspan(2);
        cell.setRotation(90);
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков
      PdfPCell missingCell = new PdfPCell(getParagraph("Пропуски", regularFont, Paragraph.ALIGN_CENTER));
      missingCell.setColspan(3);
      missingCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(missingCell);
      PdfPCell legalCell = new PdfPCell(getParagraph("Уважительных", smallFont, Paragraph.ALIGN_CENTER));
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      table.addCell(legalCell);
      PdfPCell illegalCell = new PdfPCell(getParagraph("Неувуважительных", smallFont, Paragraph.ALIGN_CENTER));
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      table.addCell(illegalCell);
      PdfPCell allCell = new PdfPCell(getParagraph("Всего", smallFont, Paragraph.ALIGN_CENTER));
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      table.addCell(allCell);
      int row = 1;
      int legal = 0;
      int illegal = 0;
      for (StudyCard sc : cards.findByGroup(gs.getGroup())) {
        if (!sc.isRemanded() && sc.isActive()) {
          numberCell = new PdfPCell(getParagraph(String.valueOf(row++), regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(numberCell);
          nameCell = new PdfPCell(getParagraph(sc.getPerson().getShortName(),
            regularFont, Paragraph.ALIGN_LEFT));
          table.addCell(nameCell);
          for (Subject sb : subjectList) {
            PdfPCell cell = new PdfPCell();
            if (!empty) {
              try {
                SemesterMark mark = smarks.get(sc, sb, gs.getCourse(), gs.getSemester());
                cell.addElement(getParagraph(String.valueOf(mark.getMark()), regularFont, Paragraph.ALIGN_CENTER));
              } catch (Exception e) {
                // Ничего не делаем, ячейка просто будет пустой
              }
            }
            table.addCell(cell);
          }
          // Кол-во пропусков
          legalCell = new PdfPCell();
          illegalCell = new PdfPCell();
          allCell = new PdfPCell();
          if (!empty) {
            ReportMissing m = missings.getSemesterMissings(sc, gs);
            legalCell.addElement(getParagraph(String.valueOf(m.getLegal()), regularFont, Paragraph.ALIGN_CENTER));
            illegalCell.addElement(getParagraph(String.valueOf(m.getIllegal()), regularFont, Paragraph.ALIGN_CENTER));
            allCell.addElement(getParagraph(String.valueOf(m.getAll()), regularFont, Paragraph.ALIGN_CENTER));
            legal += m.getLegal();
            illegal += m.getIllegal();
          }
          table.addCell(legalCell);
          table.addCell(illegalCell);
          table.addCell(allCell);
        }
      }
      numberCell = new PdfPCell(getParagraph("Подписи преподавателей", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setColspan(2);
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      numberCell.setMinimumHeight(getPt(20));
      table.addCell(numberCell);
      for (int i = 0; i < subjectList.size(); i++) {
        PdfPCell cell = new PdfPCell();
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков итоговое
      legalCell = new PdfPCell();
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      legalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      legalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      illegalCell = new PdfPCell();
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      illegalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      illegalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      allCell = new PdfPCell();
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      allCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      allCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      if (!empty) {
        legalCell.addElement(getParagraph(String.valueOf(legal), regularFont, Paragraph.ALIGN_CENTER));
        illegalCell.addElement(getParagraph(String.valueOf(illegal), regularFont, Paragraph.ALIGN_CENTER));
        allCell.addElement(getParagraph(String.valueOf(legal + illegal), regularFont, Paragraph.ALIGN_CENTER));
      }
      table.addCell(legalCell);
      table.addCell(illegalCell);
      table.addCell(allCell);
      document.add(table);
      document.add(getParagraph("\n                Зав. отделением ________________________________",
        regularFont, Paragraph.ALIGN_LEFT));
      return document.getData();
    } catch (DocumentException e) {
      throw new InternalServerErrorException("DocumentException: " + e.getMessage());
    }
  }

  /**
   * Сводная ведомость.
   *
   * Сводная ведомость представляет собой ведомость, в которой собраны оценки за
   * семестр по дисциплиным, практикам (если таковые имеются), курсовым проектам
   * и(возможно) модулям.
   *
   * @param gs Семестр группы, для которого запрашивается ведомость
   * @param empty должна ли ведомость быть пустой.
   * @return Массим бай, представляющий собой содержимое ведомости.
   */
  private byte[] consolidatedSemesterMarks(final GroupSemester gs, final boolean empty) {
    try {
      String title = "Сводная ведомость";
      Report document = PDFReport.create(Report.Orientation.PORTRET, title);
      document.add(getParagraph(schoolName, regularFont, Paragraph.ALIGN_CENTER));
      document.add(getParagraph(title, bigFont, Paragraph.ALIGN_CENTER));
      String description = "группы " + gs.getGroup().getName() + " " + gs.getCourse() + "-го курса "
        + (gs.getGroup().isExtramural() ? "заочной" : "очной") + " формы обучения за " + gs.getSemester() + "-й семестр.";
      document.add(getParagraph(description, regularFont, Paragraph.ALIGN_CENTER));
      String speciality = "Специальность: " + gs.getGroup().getPlan().getSpecialityName();
      document.add(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER));
      // Настало время сформировать список всего, что будет изучаться
      List<ReportExam> items = new LinkedList<>();
      // Добавляем экзамены
      for (Subject s : subjects.fetchExams(gs.getGroup(), gs.getCourse(), gs.getSemester())) {
        items.add(new ReportExam(s.getId(), ReportExamType.EXAM, s.getShortName()));
      }
      // Добавляем зачеты
      for (Subject s : subjects.fetchZachets(gs.getGroup(), gs.getCourse(), gs.getSemester())) {
        items.add(new ReportExam(s.getId(), ReportExamType.ZACHET, s.getShortName()));
      }
      // Добавляем курсовые
      for (Subject s : subjects.fetchCourseWorks(gs.getGroup(), gs.getCourse(), gs.getSemester())) {
        items.add(new ReportExam(s.getId(), ReportExamType.COURSEWORK, s.getShortName()));
      }
      // Добавляем Практику
      for (Practic p : practics.fetch(gs.getGroup(), gs.getCourse(), gs.getSemester())) {
        items.add(new ReportExam(p.getId(), ReportExamType.PRACTIC, p.getShortName()));
      }
      // Теперь можно создавать таблицу
      // Количество полей будет на 5 больше, чем кол-во дисциплин
      PdfPTable table = new PdfPTable(items.size() + 5);
      table.setWidthPercentage(100.0f);
      table.setSpacingBefore(getPt(5));
      table.setSpacingAfter(getPt(5));
      // Создадим массив целых чисел для указания размерности столбцов
      // Установим размер столбца ФИО в пять раз больше размером!
      int[] sizes = new int[items.size() + 5];
      for (int i = 0; i < sizes.length; i++) {
        sizes[i] = (i == 1) ? 5 : 1;
      }
      table.setWidths(sizes);
      // Добавляем строки таблицы
      PdfPCell numberCell = new PdfPCell(getParagraph("№", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setRowspan(3);
      numberCell.setMinimumHeight(getPt(20));
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(numberCell);
      PdfPCell nameCell = new PdfPCell(getParagraph("Фамилия имя отчество", regularFont, Paragraph.ALIGN_CENTER));
      nameCell.setRowspan(3);
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nameCell.setMinimumHeight(getPt(20));
      nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      table.addCell(nameCell);
      for (ReportExam item : items) {
        PdfPCell cell = new PdfPCell(getParagraph(item.getName(), smallFont, Paragraph.ALIGN_CENTER));
        cell.setRowspan(2);
        cell.setRotation(90);
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков
      PdfPCell missingCell = new PdfPCell(getParagraph("Пропуски", regularFont, Paragraph.ALIGN_CENTER));
      missingCell.setColspan(3);
      missingCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      table.addCell(missingCell);
      PdfPCell legalCell = new PdfPCell(getParagraph("Уважительных", smallFont, Paragraph.ALIGN_CENTER));
      legalCell.setRowspan(2);
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      table.addCell(legalCell);
      PdfPCell illegalCell = new PdfPCell(getParagraph("Неуважительных", smallFont, Paragraph.ALIGN_CENTER));
      illegalCell.setRowspan(2);
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      table.addCell(illegalCell);
      PdfPCell allCell = new PdfPCell(getParagraph("Всего", smallFont, Paragraph.ALIGN_CENTER));
      allCell.setRowspan(2);
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      table.addCell(allCell);
      // Подписи для видов аттестации
      for (ReportExam item : items) {
        PdfPCell cell = new PdfPCell(getParagraph(item.getType().getLabel(), smallFont, Paragraph.ALIGN_CENTER));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);
      }
      int row = 1;
      int legal = 0;
      int illegal = 0;
      for (StudyCard sc : cards.findByGroup(gs.getGroup())) {
        if (!sc.isRemanded() && sc.isActive()) {
          numberCell = new PdfPCell(getParagraph(String.valueOf(row++), regularFont, Paragraph.ALIGN_CENTER));
          table.addCell(numberCell);
          nameCell = new PdfPCell(getParagraph(sc.getPerson().getShortName(),
            regularFont, Paragraph.ALIGN_LEFT));
          table.addCell(nameCell);
          for (ReportExam item : items) {
            PdfPCell cell = new PdfPCell();
            if (!empty) {
              try {
                ReportExamType type = item.getType();
                if ((type == ReportExamType.EXAM) || (type == ReportExamType.ZACHET)) {
                // Экзамены и зачеты
                  SemesterMark mark = smarks.get(sc, subjects.get(item.getId()), gs.getCourse(), gs.getSemester());
                  cell.addElement(getParagraph(String.valueOf(mark.getMark()), regularFont, Paragraph.ALIGN_CENTER));
                } else if (type == ReportExamType.COURSEWORK) {
                // Курсовые
                  CourseWorkMark mark = cmarks.get(sc, subjects.get(item.getId()), gs.getCourse(), gs.getSemester());
                  cell.addElement(getParagraph(String.valueOf(mark.getMark()), regularFont, Paragraph.ALIGN_CENTER));
                } else if (type == ReportExamType.PRACTIC) {
                // Практика
                  PracticMark mark = pmarks.get(sc, practics.get(item.getId()));
                  cell.addElement(getParagraph(String.valueOf(mark.getMark()), regularFont, Paragraph.ALIGN_CENTER));
                }
              } catch (Exception e) {
                // Ничего не делаем, ячейка просто будет пустой
              }
            }
            table.addCell(cell);
          }
          // Кол-во пропусков
          legalCell = new PdfPCell();
          illegalCell = new PdfPCell();
          allCell = new PdfPCell();
          if (!empty) {
            ReportMissing m = missings.getSemesterMissings(sc, gs);
            legalCell.addElement(getParagraph(String.valueOf(m.getLegal()), regularFont, Paragraph.ALIGN_CENTER));
            illegalCell.addElement(getParagraph(String.valueOf(m.getIllegal()), regularFont, Paragraph.ALIGN_CENTER));
            allCell.addElement(getParagraph(String.valueOf(m.getAll()), regularFont, Paragraph.ALIGN_CENTER));
            legal += m.getLegal();
            illegal += m.getIllegal();
          }
          table.addCell(legalCell);
          table.addCell(illegalCell);
          table.addCell(allCell);
        }
      }
      numberCell = new PdfPCell(getParagraph("Подписи преподавателей", regularFont, Paragraph.ALIGN_CENTER));
      numberCell.setColspan(2);
      numberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      numberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      numberCell.setMinimumHeight(getPt(20));
      table.addCell(numberCell);
      for (int i = 0; i < items.size(); i++) {
        PdfPCell cell = new PdfPCell();
        cell.setMinimumHeight(getPt(20));
        table.addCell(cell);
      }
      // Кол-во пропусков итоговое
      legalCell = new PdfPCell();
      legalCell.setMinimumHeight(getPt(20));
      legalCell.setRotation(90);
      legalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      legalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      illegalCell = new PdfPCell();
      illegalCell.setMinimumHeight(getPt(20));
      illegalCell.setRotation(90);
      illegalCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      illegalCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      allCell = new PdfPCell();
      allCell.setMinimumHeight(getPt(20));
      allCell.setRotation(90);
      allCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      allCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
      if (!empty) {
        legalCell.addElement(getParagraph(String.valueOf(legal), regularFont, Paragraph.ALIGN_CENTER));
        illegalCell.addElement(getParagraph(String.valueOf(illegal), regularFont, Paragraph.ALIGN_CENTER));
        allCell.addElement(getParagraph(String.valueOf(legal + illegal), regularFont, Paragraph.ALIGN_CENTER));
      }
      table.addCell(legalCell);
      table.addCell(illegalCell);
      table.addCell(allCell);
      document.add(table);
      document.add(getParagraph("\n                Зав. отделением ________________________________",
        regularFont, Paragraph.ALIGN_LEFT));
      return document.getData();
    } catch (DocumentException e) {
      throw new InternalServerErrorException("DocumentException: " + e.getMessage());
    }
  }
}
