package pl.hellopolandticket.app;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Slf4j
@Interceptor
@Logger
public class LoggingHandler {

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss.SSS");
  private static final String NEW_LINE_STRING = "\n\t";

  @AroundInvoke
  public Object logAroundServices(InvocationContext invocationCtx) throws Exception {
    long start = System.currentTimeMillis();

    try {
      String className = invocationCtx.getTarget().getClass().getSimpleName();
      String methodName = invocationCtx.getMethod().getName();
      Object[] arguments = invocationCtx.getParameters();
      Object result = invocationCtx.proceed();
      long elapsedTime = System.currentTimeMillis() - start;

      log.info(
          prepareServiceLogInfo(className, methodName, arguments, start, elapsedTime, result));

      return result;
    } catch (Exception e) {
      long elapsedTime = System.currentTimeMillis() - start;

      log.info(prepareServiceLogInfo(invocationCtx.getMethod().getClass().getSimpleName(),
          invocationCtx.getMethod().getName(), invocationCtx.getParameters(), start, elapsedTime,
          e));

      throw e;
    }
  }

  private String prepareServiceLogInfo(String className, String methodName, Object[] arguments,
      long start, long elapsedTime, Object result) {
    return NEW_LINE_STRING + "Executing: " + className + "." + methodName + "()" +
        NEW_LINE_STRING + "Arguments: " + Arrays.toString(arguments) +
        NEW_LINE_STRING + "Time: " + DATE_FORMATTER.format(new Date(start)) +
        NEW_LINE_STRING + "Execution time: " + elapsedTime + "ms" +
        NEW_LINE_STRING + "Result: " + getValue(result);
  }

  private String getValue(Object result) {
    String returnValue = null;
    if (null != result) {
      if (result.toString().endsWith("@" + Integer.toHexString(result.hashCode()))) {
        returnValue = ReflectionToStringBuilder.toString(result);
      } else {
        returnValue = result.toString();
      }
    }
    return returnValue;
  }
}
