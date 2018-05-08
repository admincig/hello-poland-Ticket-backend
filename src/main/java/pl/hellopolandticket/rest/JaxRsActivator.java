package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.Role.ROLE_USER;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("v1")
@DeclareRoles({ROLE_USER, ROLE_ADMIN})
public class JaxRsActivator extends Application {

}