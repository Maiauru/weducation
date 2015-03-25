package ru.edu.pgtk.weducation.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import ru.edu.pgtk.weducation.ejb.CourseWorkMarksEJB;
import ru.edu.pgtk.weducation.ejb.FinalMarksEJB;
import ru.edu.pgtk.weducation.ejb.FinalPracticMarksEJB;
import ru.edu.pgtk.weducation.ejb.GOSMarksEJB;
import ru.edu.pgtk.weducation.ejb.RenamingsEJB;
import ru.edu.pgtk.weducation.entity.CourseWorkMark;
import ru.edu.pgtk.weducation.entity.FinalMark;
import ru.edu.pgtk.weducation.entity.FinalPracticMark;
import ru.edu.pgtk.weducation.entity.GOSMark;
import ru.edu.pgtk.weducation.entity.Person;
import ru.edu.pgtk.weducation.entity.Renaming;
import ru.edu.pgtk.weducation.entity.School;
import ru.edu.pgtk.weducation.entity.Speciality;
import ru.edu.pgtk.weducation.entity.StudyCard;
import static ru.edu.pgtk.weducation.reports.PDFUtils.getParagraph;
import static ru.edu.pgtk.weducation.reports.PDFUtils.getPt;
import static ru.edu.pgtk.weducation.reports.PDFUtils.putText;
import static ru.edu.pgtk.weducation.reports.PDFUtils.wrapElement;
import ru.edu.pgtk.weducation.utils.Utils;
import static ru.edu.pgtk.weducation.utils.Utils.getMonthString;
import static ru.edu.pgtk.weducation.utils.Utils.getYearString;

/**
 * Класс, генерирующий отчет в виде pdf документа. Поскольку планируется
 * генерить относительно простые отчеты, класс использует поток для записи в
 * память. Думаю, это быстрее файлового ввода-вывода.
 *
 * @author Воронин Леонид
 *
 */
@Stateless
public class DiplomeBlanksEJB {

  // поток байт в котором будет "собираться" отчет.
  private final ByteArrayOutputStream stream;
  private BaseFont baseFont;
  private Font regularFont;
  private Font smallFont;
  @EJB
  private FinalMarksEJB finalMarks;
  @EJB
  private FinalPracticMarksEJB practicMarks;
  @EJB
  private GOSMarksEJB gosMarks;
  @EJB
  private CourseWorkMarksEJB courseWorks;
  @EJB
  private RenamingsEJB renamings;

  /**
   * Подготавливает шрифты для использования в документе
   *
   * @param regularSize размер обычного шрифта
   * @param smallSize размер маленького шрифта
   * @throws IOException
   * @throws DocumentException
   */
  private void prepareFonts(final int regularSize, final int smallSize)
          throws IOException, DocumentException {
    baseFont = BaseFont.createFont("fonts/times.ttf", BaseFont.IDENTITY_H,
            BaseFont.EMBEDDED);
    regularFont = new Font(baseFont, regularSize);
    smallFont = new Font(baseFont, smallSize);
  }

  /**
   * Готовит таблицу оценок за курсовые проекты
   *
   * @param marks
   * @return
   * @throws DocumentException
   */
  private PdfPTable prepareCourseWorkTable(final List<CourseWorkMark> marks)
          throws DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100.0f);
    table.setWidths(new int[]{12, 2});
    PdfPCell nameCell;
    PdfPCell markCell;
    for (CourseWorkMark mark : marks) {
      nameCell = new PdfPCell(getParagraph(mark.getSubject().getFullName()
              + " (" + mark.getTheme() + ")",
              smallFont, Paragraph.ALIGN_LEFT));
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
//			nameCell.setPaddingRight(getPt(3));
//			nameCell.setLeading(0.5f, 0.6f);
      nameCell.setBorder(PdfPCell.NO_BORDER);
      markCell = new PdfPCell(getParagraph(Utils.getMarkString(mark.getMark()),
              smallFont, Paragraph.ALIGN_CENTER));
      markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
//			markCell.setLeading(0.5f, 0.6f);
      markCell.setBorder(PdfPCell.NO_BORDER);
      table.addCell(nameCell);
      table.addCell(markCell);
    }
    return table;
  }

  private void prepareMarkTables(final List<MarkItem> marks,
          PdfPTable table1, PdfPTable table2) throws DocumentException {

    boolean firstPage = true;
    PdfPCell nameCell;
    PdfPCell hoursCell;
    PdfPCell markCell;
    for (MarkItem mark : marks) {
      nameCell = new PdfPCell(getParagraph(mark.subject,
              smallFont, Paragraph.ALIGN_LEFT));
      nameCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
      nameCell.setPaddingRight(getPt(3));
      nameCell.setLeading(0.5f, 0.7f);
      nameCell.setBorder(PdfPCell.NO_BORDER);
      hoursCell = new PdfPCell(getParagraph(mark.load, smallFont, Paragraph.ALIGN_CENTER));
      hoursCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      hoursCell.setLeading(0.5f, 0.7f);
      hoursCell.setBorder(PdfPCell.NO_BORDER);
      markCell = new PdfPCell(getParagraph(mark.mark, smallFont, Paragraph.ALIGN_CENTER));
      markCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      markCell.setLeading(0.5f, 0.7f);
      markCell.setBorder(PdfPCell.NO_BORDER);
      if (firstPage) {
        table1.addCell(nameCell);
        table1.addCell(hoursCell);
        table1.addCell(markCell);
        if (table1.calculateHeights() > 460) {
          // Опа! высота таблицы зашкаливает. Удаляем последнюю строку
          firstPage = false;
          table1.deleteLastRow();
        }
      }
      if (!firstPage) {
        // Выводим данные на вторую страницу
        table2.addCell(nameCell);
        table2.addCell(hoursCell);
        table2.addCell(markCell);
      }
    }
  }

  /**
   * Готовит таблицу переименований учебного заведения.
   *
   * @param renamingList
   * @return
   * @throws DocumentException
   */
  private PdfPTable prepareRenamingTable(List<Renaming> renamingList)
          throws DocumentException {
    PdfPTable table = new PdfPTable(1);
    table.setWidthPercentage(100.0f);
    PdfPCell cell;
    for (Renaming item : renamingList) {
      cell = new PdfPCell();
      cell.setBorder(PdfPCell.NO_BORDER);
      cell.addElement(getParagraph(
              "Образовательная организация переименована в "
              + Utils.getYear(item.getDate()) + " году;", smallFont,
              Paragraph.ALIGN_LEFT));
      cell.addElement(getParagraph(
              "старое полное наименование образовательной организации: "
              + item.getOldName(), smallFont,
              Paragraph.ALIGN_LEFT));
      table.addCell(cell);
    }
    return table;
  }

  /**
   * Готовит отчет в виде pdf документа.
   *
   * @param card Личная карточка, требуемая для отчета
   * @param isCopy если истина, то будет выведена копия
   * @param isDuplicate если истина, то будет выведен дубликат
   * @return массив байт (содержимое pdf-документа)
   */
  public byte[] getDiplome(final StudyCard card, final boolean isCopy, final boolean isDuplicate) {
    try {
      prepareFonts(10, 6);
      Document document = new Document(PageSize.A4.rotate(), 15f, 15f,
              75f, 15f);
      PdfWriter writer = PdfWriter.getInstance(document, stream);
      document.open();
      document.addTitle("Диплом о среднеспециальном образовании и приложение к нему.");
      document.addAuthor("weducation project");

      // Данные для вывода (возможно лучше тут считать всё с карточки)
      School scl = card.getSchool();
      Speciality spc = card.getSpeciality();
      Person psn = card.getPerson();
      String sclName = scl.getFullName() + "\n" + scl.getPlace();
      // Корректируем надпись с учетом дубликата
      if (isDuplicate) {
        sclName = sclName + "\n\n" + "ДУБЛИКАТ";
      }
      String comissionDate = "от " + Utils.getDateString(card.getComissionDate()) + " года";
      String diplomeDate = Utils.getDateString(card.getDiplomeDate()) + " года";
      String birthDate = Utils.getDateString(psn.getBirthDate()) + " года";
      String spo = "о среднем профессиональном образовании";
      String schoolDirector = scl.getDirector();
      String comissionDirector = card.getComissionDirector();
      String speciality = spc.getFullName();
      String learnLength = getYearString(card.getPlan().getYears()) + " "
              + getMonthString(card.getPlan().getMonths());
      String oldDocument = card.getDocumentName() + ", "
              + Utils.getYear(card.getDocumentDate()) + " год.";
      List<MarkItem> marks = new ArrayList<>();
      // Добавляем итоговые оценки
      int aload = 0;
      int mload = 0;
      // Сначала добавляем дисциплины, не входящие в модули
      for (FinalMark fm : finalMarks.fetchOnlySubjects(card)) {
        marks.add(new MarkItem(fm));
        aload += fm.getAuditoryLoad();
        mload += fm.getMaximumLoad();
      }
      // Потом добавим модули с их содержимым
      for (FinalMark fm : finalMarks.fetchModules(card)) {
        marks.add(new MarkItem(fm));
        marks.add(new MarkItem("в том числе:", "", ""));
        // Дисциплины конкретного модуля
        for (FinalMark sfm : finalMarks.fetchModuleSubjects(card, fm.getModule())) {
          marks.add(new MarkItem(sfm));
          aload += fm.getAuditoryLoad();
          mload += fm.getMaximumLoad();
        }
      }
      marks.add(new MarkItem("ВСЕГО часов теоретического обучения:", mload, 0));
      marks.add(new MarkItem("в том числе аудиторных часов:", aload, 0));
      marks.add(new MarkItem("Практика", practicMarks.getSummaryLoad(card), 0));
      marks.add(new MarkItem("в том числе:", "", ""));
      // Добавляем оценки за практику
      for (FinalPracticMark pm : practicMarks.fetchAll(card)) {
        marks.add(new MarkItem(pm));
      }
      marks.add(new MarkItem("Государственная итоговая аттестация", card.getDiplomeLength(), 0));
      marks.add(new MarkItem("в том числе:", "", ""));
      String title = (card.isGosExam()) ? "Итоговый междисциплинарный государственный экзамен"
              : ("Дипломный проект на тему \"" + card.getDiplomeTheme() + "\"");
      marks.add(new MarkItem(title, "x", Utils.getMarkString(card.getDiplomeMark())));
      for (GOSMark gm : gosMarks.fetchAll(card)) {
        marks.add(new MarkItem(gm));
      }

      // ============================================================
      // Бланк диплома
      // ============================================================
      PdfPTable mainTable = new PdfPTable(2);
      mainTable.setWidthPercentage(100.0f);

      // Первая колонка
      PdfPCell firstTableCell = new PdfPCell();
      firstTableCell.setPaddingLeft(20f);
      firstTableCell.setPaddingRight(10f);
      firstTableCell.setBorder(PdfPCell.NO_BORDER);

      // Отступ от верхнего края страницы
      firstTableCell.addElement(wrapElement(new Phrase(" ", regularFont), getPt(45)));
      // Наименование учебного заведения
      firstTableCell.addElement(wrapElement(getParagraph(sclName, regularFont, Paragraph.ALIGN_CENTER), 188));
      // Квалификация
      firstTableCell.addElement(wrapElement(getParagraph(spc.getKvalification(), regularFont, Paragraph.ALIGN_CENTER), 97));
      // Регистрационный номер
      firstTableCell.addElement(wrapElement(
              getParagraph(card.getRegistrationNumber(), regularFont,
                      Paragraph.ALIGN_CENTER), 40));
      // Дата выдачи
      firstTableCell.addElement(wrapElement(
              getParagraph(diplomeDate, regularFont,
                      Paragraph.ALIGN_CENTER), 40));

      // Добавляем первую колонку в основную таблицу. Страница диплома создана.
      mainTable.addCell(firstTableCell);
      // Вторая колонка
      PdfPCell secondTableCell = new PdfPCell();
      secondTableCell.setBorder(PdfPCell.NO_BORDER);
      // Добавляем данные
      // Отступ от верхнего края страницы
      secondTableCell.addElement(wrapElement(new Phrase(" ", regularFont), getPt(20)));
      // ФИО
      secondTableCell.addElement(wrapElement(getParagraph(psn.getFirstName(), regularFont, Paragraph.ALIGN_CENTER), getPt(5)));
      secondTableCell.addElement(wrapElement(getParagraph(psn.getMiddleName(), regularFont, Paragraph.ALIGN_CENTER), getPt(5)));
      secondTableCell.addElement(wrapElement(getParagraph(psn.getLastName(), regularFont, Paragraph.ALIGN_CENTER), getPt(39)));
      // образовательная программа
      secondTableCell.addElement(wrapElement(getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER), getPt(30)));
      // Дата комиссии
      secondTableCell.addElement(wrapElement(getParagraph(comissionDate, regularFont, Paragraph.ALIGN_CENTER), getPt(21)));
      // Председатель комиссии
      secondTableCell.addElement(wrapElement(getParagraph(comissionDirector, regularFont, Paragraph.ALIGN_RIGHT), getPt(15)));
      // Руководитель организации
      secondTableCell.addElement(wrapElement(getParagraph(schoolDirector, regularFont, Paragraph.ALIGN_RIGHT), getPt(20)));

      mainTable.addCell(secondTableCell);
      // Добавляем подложку в виде картинки, если требуется копия выписки
      if (isCopy) {
        Image img = Image.getInstance(this.getClass().getClassLoader()
                .getResource("images/02.jpg"));
        img.scaleAbsolute(getPt(297), getPt(210));
        img.setAbsolutePosition(0, 0);
        document.add(img);
      }
      document.add(mainTable);
      // Добавляем приложение к диплому
      document.newPage();
      // ============================================================
      // Приложение к диплому
      // ============================================================
      // Страницы 1 и 4 приложения к диплому (сторона 1 листа)
      // Основная таблица из двух столбцов. Так мы реализуем колонки
      mainTable = new PdfPTable(2);
      mainTable.setWidthPercentage(100.0f);

      // Первая колонка (страница 4 приложения к диплому)
      firstTableCell = new PdfPCell();
      firstTableCell.setPaddingLeft(15f);
      firstTableCell.setPaddingRight(10f);
      firstTableCell.setBorder(PdfPCell.NO_BORDER);

      // Добавим таблицу курсовых в колонку
      PdfPTable courseWorkTable = wrapElement(
              prepareCourseWorkTable(courseWorks.fetchAll(card)), 290.0f);
      firstTableCell.addElement(courseWorkTable);

      // Добавим дополнительные сведения в колонку
      PdfPTable renamingTable = wrapElement(
              prepareRenamingTable(renamings.findByDates(card.getBeginDate(), card.getEndDate())), 80.0f);
      renamingTable.setSpacingBefore(20.0f);
      firstTableCell.addElement(renamingTable);

      // Добавим данные о руководителе организации
      firstTableCell.addElement(wrapElement(
              getParagraph(schoolDirector, regularFont,
                      Paragraph.ALIGN_RIGHT), 50.0f));

      // Добавляем первую колонку в основную таблицу. Страница 4 создана.
      mainTable.addCell(firstTableCell);
      // Вторая колонка (страница 1 приложения к диплому)
      secondTableCell = new PdfPCell();
      secondTableCell.setBorder(PdfPCell.NO_BORDER);
      PdfPTable secondTable = new PdfPTable(2);
      secondTable.setWidthPercentage(100.0f);
      secondTable.setWidths(new int[]{3, 6});
      secondTable.setSpacingBefore(20f);
      // Маленькая колонка (под гербом РФ)
      PdfPCell innerCell1 = new PdfPCell();
      innerCell1.setBorder(PdfPCell.NO_BORDER);
      innerCell1.addElement(wrapElement(new Phrase("", smallFont), 100));
      innerCell1.addElement(wrapElement(getParagraph(sclName, regularFont, Paragraph.ALIGN_CENTER), 230));
      innerCell1.addElement(wrapElement(
              getParagraph(spo,
                      regularFont, Paragraph.ALIGN_CENTER), 70));
      innerCell1.addElement(wrapElement(
              getParagraph(card.getRegistrationNumber(), regularFont,
                      Paragraph.ALIGN_CENTER), 50));
      innerCell1.addElement(wrapElement(
              getParagraph(diplomeDate, regularFont,
                      Paragraph.ALIGN_CENTER), 20));
      // Большая колонка (сведения об обладателе диплома и т.п.)
      PdfPCell innerCell2 = new PdfPCell();
      innerCell2.setBorder(PdfPCell.NO_BORDER);
      innerCell2.addElement(wrapElement(new Phrase("", smallFont), 20));
      innerCell2
              .addElement(wrapElement(
                              getParagraph(psn.getFirstName(), regularFont,
                                      Paragraph.ALIGN_CENTER), 50));
      innerCell2.addElement(wrapElement(
              getParagraph(psn.getMiddleName(), regularFont, Paragraph.ALIGN_CENTER),
              50));
      innerCell2.addElement(wrapElement(
              getParagraph(psn.getLastName(), regularFont,
                      Paragraph.ALIGN_CENTER), 50));
      innerCell2.addElement(wrapElement(
              getParagraph(birthDate, regularFont,
                      Paragraph.ALIGN_CENTER), 50));
      innerCell2.addElement(wrapElement(
              getParagraph(oldDocument,
                      regularFont, Paragraph.ALIGN_LEFT), 130));
      innerCell2.addElement(wrapElement(
              getParagraph(learnLength, regularFont,
                      Paragraph.ALIGN_CENTER), 50));
      innerCell2.addElement(wrapElement(
              getParagraph(spc.getKvalification(), regularFont,
                      Paragraph.ALIGN_CENTER), 40));
      innerCell2
              .addElement(wrapElement(
                              getParagraph(speciality, regularFont, Paragraph.ALIGN_CENTER), 30));
      // Добавляем колонки
      secondTable.addCell(innerCell1);
      secondTable.addCell(innerCell2);
      // Добавляем таблицу в колонку 2
      secondTableCell.addElement(secondTable);
      mainTable.addCell(secondTableCell);
      // Добавляем подложку в виде картинки, если требуется копия выписки
      if (isCopy) {
        Image img = Image.getInstance(this.getClass().getClassLoader()
                .getResource("images/03.jpg"));
        img.scaleAbsolute(getPt(297), getPt(210));
        img.setAbsolutePosition(0, 0);
        document.add(img);
      }
      document.add(mainTable);
      // Выведем номера страниц
      PdfContentByte canvas = writer.getDirectContent();
      putText(canvas, regularFont, "4", 54, 16); // Страниц всего
      putText(canvas, regularFont, "4", 27, 4); // Страница 4
      putText(canvas, regularFont, "1", 289, 4); // Страница 1

      // ===========================================================================
      // Сторона 1 приложения к диплому (страницы 1 и 4) сформированы. Приступаем к
      // стороне 2.
      document.newPage();
      mainTable = new PdfPTable(2);
      mainTable.setWidthPercentage(100.0f);
      // Первая колонка (страница 2 приложения к диплому)
      firstTableCell = new PdfPCell();
      firstTableCell.setPaddingLeft(15f);
      firstTableCell.setPaddingRight(15f);
      firstTableCell.setBorder(PdfPCell.NO_BORDER);
      firstTableCell.setMinimumHeight(300);
      PdfPTable firstTable = new PdfPTable(3);
      firstTable.setWidthPercentage(100.0f);
      firstTable.setTotalWidth(getPt(135));
      firstTable.setWidths(new int[]{10, 2, 2});
      firstTable.setSpacingBefore(35);
      // Вторая колонка (страница 1 приложения к диплому)
      secondTableCell = new PdfPCell();
      secondTableCell.setPaddingLeft(17f);
      secondTableCell.setPaddingRight(12f);
      secondTableCell.setBorder(PdfPCell.NO_BORDER);
      secondTableCell.setMinimumHeight(350);
      secondTable = new PdfPTable(3);
      secondTable.setWidthPercentage(100.0f);
      firstTable.setTotalWidth(getPt(135));
      secondTable.setWidths(new int[]{10, 2, 2});
      secondTable.setSpacingBefore(5);

      // Заполняем данными страницы 2 и 3
      prepareMarkTables(marks, firstTable, secondTable);
      firstTableCell.addElement(firstTable);
      secondTableCell.addElement(secondTable);
      mainTable.addCell(firstTableCell);
      mainTable.addCell(secondTableCell);
      // Если требуется копия выписки, то отображаем картинку в качестве подложки
      if (isCopy) {
        Image img = Image.getInstance(this.getClass().getClassLoader()
                .getResource("images/04.jpg"));
        img.scaleAbsolute(getPt(297), getPt(210));
        img.setAbsolutePosition(0, 0);
        document.add(img);
      }
      document.add(mainTable);
      // Выводим номера страниц
      canvas = writer.getDirectContent();
      putText(canvas, regularFont, "2", 27, 4); // Страница 4
      putText(canvas, regularFont, "3", 289, 4); // Страница 1
      // Закрываем документ
      document.close();
    } catch (IOException | DocumentException e) {
      throw new EJBException(e.getMessage());
    }
    return stream.toByteArray();
  }

  /**
   * Конструктор класса.
   *
   */
  public DiplomeBlanksEJB() {
    stream = new ByteArrayOutputStream();
  }
}
