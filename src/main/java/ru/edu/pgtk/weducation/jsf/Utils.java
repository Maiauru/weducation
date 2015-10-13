package ru.edu.pgtk.weducation.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;

/**
 * Утилиты, предназначенные для облегчения работы с JSF.
 *
 * @author Воронин Леонид
 */
class Utils {

  /**
   * Приватный конструктор, чтобы запретить создание экземпляров класса.
   */
  private Utils() {
    throw new AssertionError("This constractor should not been called!");
  }

  /**
   * Возвращает экземпляр FacesContext и используется для других методов.
   *
   * @return FacesContext.getCurrentInstance()
   */
  private static FacesContext getFacesContext() {
    return FacesContext.getCurrentInstance();
  }

  /**
   * Проверяет, есть ли что-нибудь полезное в сообщении исключения.
   * 
   * Используется при формировании сообщения об ошибке на основе сообщения исключения
   * 
   * @param text текст сообщения исключения
   * @return истина, если текст бесполезен (null, пустое значение либо строка "null")
   */
  private static boolean isUseless(String text) {
    return (null == text) || (text.isEmpty()) || text.contentEquals("null") || 
      text.contentEquals("Transaction aborted") || text.contentEquals("Transaction marked for rollback");
  }

  /**
   * Добавляет сообщение об ошибке по исключению.
   *
   * @param e Исключение, из которого будет формироваться информация об ошибке.
   */
  public static void addMessage(final Exception e) {
    String message = e.getMessage();
    if (isUseless(message)) {
      Throwable cause = e.getCause();
      while (cause != null) {
        if (isUseless(cause.getMessage())) {
          cause = cause.getCause();
        } else {
          message = cause.getMessage();
          break;
        }
      }
      // Если сообщение все еще бесполезно, то
      if (isUseless(message)) {
        message = "При выполнении операции случилось что-то страшное. Обратитесь к разработчику.";
      }
    }
    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Ошибка! " + message, "Error"));
  }

  /**
   * Добавляет сообщение об ошибке.
   *
   * @param message сообщение об ошибке.
   */
  public static void addMessage(final String message) {
    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, "Error"));
  }

  public static ExternalContext getExternalContext() {
    return getFacesContext().getExternalContext();
  }

  /**
   * Устанавливает cookie с указанными параметрами.
   *
   * @param key ключ для доступа к cookie
   * @param value значение, доступное по ключу.
   */
  public static void setCookie(final String key, final String value) {
    getExternalContext().addResponseCookie(key, value, null);
  }

  /**
   * Получает целочисленное значение из cookie.
   *
   * @param name наименование cookie
   * @return целое типа Long
   */
  public static long getLongFromCookie(final String name) {
    long result = 0;
    Cookie cookie = (Cookie) Utils.getExternalContext().getRequestCookieMap().get(name);
    if (null != cookie) {
      try {
        result = Long.parseLong(cookie.getValue());
      } catch (NumberFormatException e) {
        // Ничего не делаем. Пусть вернется 0
      }
    }
    return result;
  }

  /**
   * Получает строку из coolie.
   *
   * @param name наименование cookie
   * @return строка.
   */
  public static String getStringFromCookie(final String name) {
    String result = null;
    Cookie cookie = (Cookie) Utils.getExternalContext().getRequestCookieMap().get(name);
    if (null != cookie) {
      try {
        result = cookie.getValue();
      } catch (NumberFormatException e) {
        addMessage(e);
      }
    }
    return result;
  }
}
