package ru.edu.pgtk.weducation.jsf;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import ru.edu.pgtk.weducation.ejb.PracticsEJB;
import ru.edu.pgtk.weducation.ejb.SpecialitiesEJB;
import ru.edu.pgtk.weducation.ejb.StudyModulesEJB;
import ru.edu.pgtk.weducation.ejb.StudyPlansEJB;
import ru.edu.pgtk.weducation.ejb.SubjectLoadEJB;
import ru.edu.pgtk.weducation.ejb.SubjectsEJB;
import ru.edu.pgtk.weducation.entity.ExamForm;
import ru.edu.pgtk.weducation.entity.Practic;
import ru.edu.pgtk.weducation.entity.Speciality;
import ru.edu.pgtk.weducation.entity.StudyModule;
import ru.edu.pgtk.weducation.entity.StudyPlan;
import ru.edu.pgtk.weducation.entity.Subject;
import ru.edu.pgtk.weducation.entity.SubjectLoad;
import static ru.edu.pgtk.weducation.jsf.Utils.addMessage;
import ru.edu.pgtk.weducation.utils.PlanParser;
import static ru.edu.pgtk.weducation.utils.Utils.getShortName;
import ru.edu.pgtk.weducation.utils.XMLModule;
import ru.edu.pgtk.weducation.utils.XMLPractice;
import ru.edu.pgtk.weducation.utils.XMLPracticeLoad;
import ru.edu.pgtk.weducation.utils.XMLSubject;
import ru.edu.pgtk.weducation.utils.XMLSubjectLoad;

/**
 * Класс для реализации импорта планов.
 *
 * @author Воронин Леонид
 */
@Named("importPlanMB")
@ViewScoped
public class ImportPlanMB implements Serializable {

  private final long serialVersionUID = 0L;

  private transient Part file;
  private boolean uploaded;
  private transient PlanParser parser;
  @Inject
  private transient SpecialitiesEJB specialities;
  @Inject
  private transient StudyPlansEJB plans;
  @Inject
  private transient StudyModulesEJB modules;
  @Inject
  private transient PracticsEJB practics;
  @Inject
  private transient SubjectsEJB subjects;
  @Inject
  private transient SubjectLoadEJB load;
  private int specialityCode;
  private Speciality speciality;
  private List<StudyPlan> existingPlans;

  /**
   * Анализирует учебные планы на предмет совпадения некоторых полей.
   *
   * Анализ выполняется по следующим полям: форма обучения, год начала действия плана.
   *
   * @param a Первый учебный план
   * @param b Второй учебный план
   * @return истина, если анализируемые поля у двух планов совпадают. Иначе - ложь.
   */
  private boolean match(final StudyPlan a, final StudyPlan b) {
    if (a.isExtramural() != b.isExtramural()) {
      return false;
    }
    // TODO добавить еще поля для сравнения
    return a.getBeginYear() == b.getBeginYear();
  }

  /**
   * Обрабатывае событие при смене специальности.
   *
   * @param event событие.
   */
  public void changeSpeciality(final ValueChangeEvent event) {
    try {
      int code = (Integer) event.getNewValue();
      if (code > 0) {
        speciality = specialities.get(code);
        existingPlans = plans.findBySpeciality(speciality);
      } else {
        speciality = null;
        existingPlans = Collections.EMPTY_LIST;
      }
    } catch (Exception e) {
      speciality = null;
      existingPlans = Collections.EMPTY_LIST;
      addMessage(e);
    }
  }

  /**
   * Загружает на сервер данные учебного плана.
   */
  public void upload() {
    uploaded = false;
    try {
      parser = new PlanParser(file.getInputStream());
      if ((null != parser) && (parser.isCorrect())) {
        if (null == speciality) {
          addMessage("Нет данных о специальности для импорта учебного плана!");
        } else {
          // Получаем учебный план
          StudyPlan sp = parser.getStudyPlan();
          // Проверим перед импортом список планов по выбранной специальности.
          boolean exist = false;
          for (StudyPlan p : existingPlans) {
            if (match(p, sp)) {
              exist = true;
              break;
            }
          }
          // Если совпадений не обнарудено, проверим перед импортом совпадения в общем списке планов
          if (!exist) {
            List<StudyPlan> samePlans = plans.findLike(sp);
            if (!samePlans.isEmpty()) {
              exist = true;
              for (StudyPlan p: samePlans) {
                addMessage("Уже имеется учебный план \"" + p.getNameForList() + "\" с наименованием специальности \"" +
                  p.getSpecialityName() + "\" и аналогичными другими параметрами в специальности " + 
                  p.getSpeciality().getName() + ". Импорт невозможен!");
              }
            }
          }
          if (!exist) {
            sp.setSpeciality(speciality);
            plans.save(sp);
            // Импорт модулей
            for (XMLModule mod : parser.getModules()) {
              StudyModule sm = null;
              if (mod.getType() == 2) {
                sm = new StudyModule();
                sm.setName(mod.getName());
                sm.setPlan(sp);
                if (mod.getKvExams() > 0) {
                  sm.setExamForm(ExamForm.KVALIFEXAM);
                }
                modules.save(sm);
              }
              // Импорт дисциплин
              for (XMLSubject xs : mod.getSubjects()) {
                Subject s = new Subject();
                s.setFullName(xs.getName());
                if (xs.getName().length() <= 30) {
                  s.setShortName(xs.getName());
                } else {
                  s.setShortName(getShortName(xs.getName()));
                }
                s.setPlan(sp);
                s.setModule(sm);
                subjects.save(s);
                // Нагрузка
                for (XMLSubjectLoad xsl : xs.getLoad()) {
                  SubjectLoad sl = new SubjectLoad();
                  sl.setSubject(s);
                  sl.setMaximumLoad(xsl.getMaxLoad());
                  sl.setAuditoryLoad(xsl.getAudLoad());
                  sl.setCourseProjectLoad(xsl.getCprLoad());
                  sl.setSemester(xsl.getSemester());
                  sl.setCourse((sl.getSemester() + 1) / 2);
                  sl.setExamForm(xsl.getExamType());
                  load.save(sl);
                }
              }
              // Импорт практик
              for (XMLPractice xp : mod.getPractices()) {
                // Для каждой практики смотрим нагрузки
                for (XMLPracticeLoad xpl : xp.getLoad()) {
                  // Формируем практику для новой базы
                  Practic p = new Practic();
                  p.setPlan(sp);
                  p.setModule(sm);
                  p.setFullName(xp.getName());
                  p.setLength(xpl.getWeeks());
                  p.setShortName(getShortName(p.getFullName()));
                  p.setSemester(xpl.getSemester());
                  p.setCourse((p.getSemester() + 1) / 2);
                  practics.save(p);
                }
              }
            }
            uploaded = true;
          } else {
            addMessage("По нашим данным, учебный план для такой же формы обучения с таким же годом начала действия уже есть!\n"
              + "Боюсь, вы не сможете импортировать этот план.");
          }
        }
      }
    } catch (Exception e) {
      addMessage(e);
    }
  }

  public Part getFile() {
    return file;
  }

  public void setFile(Part file) {
    this.file = file;
  }

  public boolean isUploaded() {
    return uploaded;
  }

  public int getSpecialityCode() {
    return specialityCode;
  }

  public void setSpecialityCode(int specialityCode) {
    this.specialityCode = specialityCode;
  }

  public Speciality getSpeciality() {
    return speciality;
  }

  public List<StudyPlan> getExistingPlans() {
    return existingPlans;
  }
}
