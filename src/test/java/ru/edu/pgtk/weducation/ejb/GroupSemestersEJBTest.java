package ru.edu.pgtk.weducation.ejb;

import java.util.List;
import javax.ejb.embeddable.EJBContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.edu.pgtk.weducation.entity.GroupSemester;
import ru.edu.pgtk.weducation.entity.StudyGroup;
import ru.edu.pgtk.weducation.utils.ContainerProvider;

public class GroupSemestersEJBTest {

  private static ContainerProvider provider;
  private static StudyGroupsEJB groups;
  private static GroupSemestersEJB groupSemesters;

  @BeforeClass
  public static void setUpClass() {
    provider = new ContainerProvider();
    groups = (StudyGroupsEJB) provider.getBean("StudyGroupsEJB");
    groupSemesters = (GroupSemestersEJB) provider.getBean("GroupSemestersEJB");
  }

  @AfterClass
  public static void tearDownClass() {
    try {
      provider.close();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private boolean equals(GroupSemester one, GroupSemester another) {
    if (one.getId() != another.getId()) {
      return false;
    }
    if (one.getGroup().getId() != another.getGroup().getId()) {
      return false;
    }
    if (one.getCourse() != another.getCourse()) {
      return false;
    }
    return (one.getSemester() == another.getSemester());
  }

  /**
   * Тестирует различные способы получения сведений о семестрах группы.
   */
  @Test
  public void testGetOperations() {
    try {
      // Получим группу ПКС-13 (на момент написания теста только у неё были семестры)
      StudyGroup group = groups.get(21);
      assertNotNull(group); // группа существует
      assertTrue(group.getId() > 0); // группа не создана во время вызова get()
      // получим список семестров
      List<GroupSemester> semesters = groupSemesters.fetchAll(group);
      assertNotNull(semesters); // список есть
      assertFalse(semesters.isEmpty()); // список не пуст
      for (GroupSemester gs : semesters) {
        // Проверим правильность выборки по курсу и семестру
        GroupSemester newSemester = groupSemesters.get(group, gs.getCourse(), gs.getSemester());
        assertNotNull(newSemester); // семестр должен быть
        assertTrue(equals(newSemester, gs));
        // Проверим правильность выборки по году и месяцу
        int year = gs.getBeginYear();
        int month = gs.getBeginMonth();
        int date = year * 1000 + month * 10;
        while (date < gs.getEndDate()) {
          newSemester = groupSemesters.getByMonth(group, year, month);
          assertNotNull(newSemester); // семестр должен быть
          assertTrue(equals(newSemester, gs));
          month += 1;
          if (13 == month) {
            year += 1;
            month = 1;
          }
          date = year * 1000 + month * 10;
        }
      }
    } catch (Exception e) {
      fail("Unexpected exception " + e.getClass().getSimpleName() + " with message " + e.getMessage());
    }
  }

// TODO Add save() and delete() tests
}
