package ru.edu.pgtk.weducation.jsf;

import ru.edu.pgtk.weducation.ejb.GroupSemestersEJB;
import ru.edu.pgtk.weducation.ejb.SemesterMarksEJB;
import ru.edu.pgtk.weducation.ejb.StudyGroupsEJB;
import ru.edu.pgtk.weducation.ejb.SubjectsEJB;
import ru.edu.pgtk.weducation.entity.GroupSemester;
import ru.edu.pgtk.weducation.entity.SemesterMark;
import ru.edu.pgtk.weducation.entity.StudyGroup;
import ru.edu.pgtk.weducation.entity.Subject;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static ru.edu.pgtk.weducation.jsf.Utils.addMessage;

@ViewScoped
@Named("semesterSubjectMarksMB")
public class SemesterSubjectMarksMB implements Serializable {

	long serialVersionUID = 0L;

	@EJB
	private transient StudyGroupsEJB groups;
	@EJB
	private transient SubjectsEJB subjects;
	@EJB
	private transient GroupSemestersEJB semesters;
	@EJB
	private transient SemesterMarksEJB marks;
	private int groupCode;
	private StudyGroup group;
	private int subjectCode;
	private Subject subject;
	private int semesterCode;
	private GroupSemester semester;
	private List<GroupSemester> semesterList;
	private List<Subject> subjectList;
	private List<SemesterMark> markList;

	/**
	 * Функция для построения списка оценок
	 */
	private void makeList() {
		if ((group != null) && (subject != null) && (semester != null)) {
			markList = marks.fetchAll(group, subject, semester.getCourse(), semester.getSemester());
		} else {
			// Если хоть один из параметров отсутствует - очищаем список
			markList = null;
		}
	}

	public void loadGroup() {
		try {
			if (groupCode > 0) {
				group = groups.get(groupCode);
				// Можно загрузить список групп
				semesterList = semesters.fetchAll(group);
			}
		} catch (Exception e) {
			addMessage(e);
		}
	}

	public void save() {
		try {
			if (markList != null) {
				for (SemesterMark m : markList) {
					marks.save(m);
				}
			}
		} catch (Exception e) {
			addMessage(e);
		}
	}

	public void changeSemester(ValueChangeEvent event) {
		try {
			int code = (Integer) event.getNewValue();
			if (code > 0) {
				semester = semesters.get(code);
				// Корректируем список дисциплин для этого семестра
				subjectList = subjects.fetch(group, semester.getCourse(), semester.getSemester());
				makeList();
			} else {
				semester = null;
			}
		} catch (Exception e) {
			semester = null;
			addMessage(e);
		}
	}

	public void changeSubject(ValueChangeEvent event) {
		try {
			int code = (Integer) event.getNewValue();
			if (code > 0) {
				subject = subjects.get(code);
				makeList();
			} else {
				subject = null;
			}
		} catch (Exception e) {
			subject = null;
			addMessage(e);
		}
	}

	public List<GroupSemester> getSemesterList() {
		if (semesterList != null) {
			return semesterList;
		} else {
			return new ArrayList<>();
		}
	}

	public List<Subject> getSubjectList() {
		if (subjectList == null) {
			subjectList = new ArrayList<>();
		}
		return subjectList;
	}

	public List<SemesterMark> getMarkList() {
		if (markList == null) {
			markList = new ArrayList<>();
		}
		return markList;
	}

	public int getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(int groupCode) {
		this.groupCode = groupCode;
	}

	public StudyGroup getGroup() {
		return group;
	}

	public int getSemesterCode() {
		return semesterCode;
	}

	public void setSemesterCode(int semesterCode) {
		this.semesterCode = semesterCode;
	}

	public GroupSemester getSemester() {
		return semester;
	}

	public int getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(int subjectCode) {
		this.subjectCode = subjectCode;
	}

	public Subject getSubject() {
		return subject;
	}
}
