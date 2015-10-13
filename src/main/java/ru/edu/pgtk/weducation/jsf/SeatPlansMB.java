package ru.edu.pgtk.weducation.jsf;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import ru.edu.pgtk.weducation.ejb.SeatsEJB;
import ru.edu.pgtk.weducation.entity.Seat;
import static ru.edu.pgtk.weducation.jsf.Utils.addMessage;

@ViewScoped
@Named("seatPlansMB")
public class SeatPlansMB implements Serializable {

  long serialVersionUID = 0L;

  @Inject
  private transient SeatsEJB ejb;
  private List<Seat> seats;
  private int year = 0;
  private boolean extramural;

  private void prepareList() {
    if (year > 0) {
      seats = ejb.fetch(year, extramural);
    } else {
      seats = null;
    }
  }
  
  public String getFormString() {
    return (extramural ? "заочная" : "очная") + " форма обучения";
  }

  public Map<String, Integer> getYears() {
    Map<String, Integer> result = new TreeMap<>();
    Calendar now = new GregorianCalendar();
    int y = now.get(Calendar.YEAR);
    for (int i = y - 1; i <= y + 1; i++) {
      result.put(i + "-й год", i);
    }
    return result;
  }

  public void save() {
    try {
      for (Seat s : seats) {
        ejb.save(s);
      }
      addMessage("Данные успешно сохранены");
    } catch (Exception e) {
      addMessage(e);
    }
  }

  public void changeYear(ValueChangeEvent event) {
    try {
      year = (Integer) event.getNewValue();
      prepareList();
    } catch (Exception e) {
      seats = null;
      addMessage(e);
    }
  }

  public void changeForm(ValueChangeEvent event) {
    try {
      extramural = (Boolean) event.getNewValue();
      prepareList();
    } catch (Exception e) {
      seats = null;
      addMessage(e);
    }
  }
  
  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public List<Seat> getSeats() {
    return seats;
  }

  public boolean isExtramural() {
    return extramural;
  }

  public void setExtramural(boolean extramural) {
    this.extramural = extramural;
  }
}
