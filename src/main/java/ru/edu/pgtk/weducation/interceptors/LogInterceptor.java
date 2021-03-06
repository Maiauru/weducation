package ru.edu.pgtk.weducation.interceptors;

import java.io.Serializable;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Класс-перехватчик, реализующий журналирование. Это тестовая реализация класса
 * с целью проверки механизма работы перехватчиков. Планируется использовать
 * данный класс для всех интересующих "клиентов" журналирования.
 *
 * @author Воронин Леонид
 */
@Interceptor
@WithLog
public class LogInterceptor implements Serializable {

  long serialVersionUID = 20150806L;
  
  @AroundInvoke
  private Object logMethod(InvocationContext context) throws Exception {
    System.out.println("Method " + context.getMethod().getName()
        + " of class " + context.getTarget().getClass().getName() + " was called.");
    return context.proceed();
  }
}
