package pl.hellopolandticket.app;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.annotation.Logger;

@Slf4j
@Interceptor
@Logger
public class LoggingHandler {

  private static final SimpleDateFormat DATE_FORMATTER =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private static final String NEW_LINE_STRING = "\n\t";

    private static final int MAX_LEN = 2000;

    private static final int MAX_ARGS = 20;


    private String safe(Object o) {
        if (o == null) return "null";

        // Kolekcje/mapy – tylko typ + size
        if (o instanceof java.util.Collection<?> c) {
            return o.getClass().getSimpleName() + "(size=" + c.size() + ")";
        }
        if (o instanceof java.util.Map<?, ?> m) {
            return o.getClass().getSimpleName() + "(size=" + m.size() + ")";
        }

        // Tablice – nie Arrays.toString() dla obiektów domenowych
        if (o.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(o);
            return o.getClass().getComponentType().getSimpleName() + "[](len=" + len + ")";
        }

        // Hibernate proxy / persistent collections – nie dotykaj
        String cn = o.getClass().getName();
        if (cn.startsWith("org.hibernate.") || cn.contains("HibernateProxy") || cn.contains("Persistent")) {
            return o.getClass().getSimpleName() + "(hibernate-proxy)";
        }

        // Zwykłe typy – OK
        if (o instanceof Number || o instanceof Boolean || o instanceof CharSequence || o instanceof java.util.Date) {
            return truncate(String.valueOf(o));
        }

        if (o instanceof java.time.temporal.TemporalAccessor) {
            return truncate(String.valueOf(o));
        }

        if (cn.startsWith("jdk.proxy") || cn.startsWith("com.sun.proxy")) {
            return o.getClass().getSimpleName() + "(proxy)";
        }


        // Domyślnie: tylko klasa + hash (bez pól)
        return o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > MAX_LEN ? s.substring(0, MAX_LEN) + "...(truncated)" : s;
    }

    private String safeArgs(Object[] args) {
        if (args == null) return "null";
        int n = Math.min(args.length, MAX_ARGS);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(safe(args[i]));
        }
        if (args.length > n) sb.append(", ... (").append(args.length - n).append(" more)");
        sb.append("]");
        return sb.toString();
    }


    @AroundInvoke
  public Object logAroundServices(InvocationContext invocationCtx) throws Exception {
    long start = System.currentTimeMillis();

    try {
      String className = invocationCtx.getTarget().getClass().getSimpleName();
      String methodName = invocationCtx.getMethod().getName();
      Object[] arguments = invocationCtx.getParameters();
      Object result = invocationCtx.proceed();
      long elapsedTime = System.currentTimeMillis() - start;

      log.debug(
          prepareServiceLogInfo(className, methodName, arguments, start, elapsedTime, result));

      return result;
    } catch (Exception e) {
      long elapsedTime = System.currentTimeMillis() - start;

      log.debug(prepareServiceLogInfo(invocationCtx.getMethod().getClass().getSimpleName(),
          invocationCtx.getMethod().getName(), invocationCtx.getParameters(), start, elapsedTime,
          e));

      throw e;
    }
  }

    private String prepareServiceLogInfo(String className, String methodName, Object[] arguments,
                                         long start, long elapsedTime, Object result) {
        return NEW_LINE_STRING + "Executing: " + className + "." + methodName + "()" + NEW_LINE_STRING
                + "Arguments: " + safeArgs(arguments) + NEW_LINE_STRING
                + "Time: " + DATE_FORMATTER.format(new Date(start)) + NEW_LINE_STRING
                + "Execution time: " + elapsedTime + "ms" + NEW_LINE_STRING
                + "Result: " + safe(result);
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
