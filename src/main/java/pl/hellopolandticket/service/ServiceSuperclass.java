package pl.hellopolandticket.service;

import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;

@Interceptors(value = LoggingHandler.class)
public class ServiceSuperclass {

}
