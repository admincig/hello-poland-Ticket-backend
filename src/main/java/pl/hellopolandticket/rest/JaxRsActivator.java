package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("v1")
@DeclareRoles({ROLE_USER, ROLE_ADMIN, ROLE_EXTERNAL_USER})
public class JaxRsActivator extends Application {

}