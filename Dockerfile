FROM quay.io/wildfly/wildfly:34.0.0.Final-jdk21
RUN touch /opt/jboss/wildfly/standalone/deployments/local.runtime.properties
COPY configure-elytron.cli /opt/jboss/wildfly/bin/configure-elytron.cli
RUN wildfly/bin/jboss-cli.sh --file=wildfly/bin/configure-elytron.cli

COPY target/helloticket.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-Dlocal.runtime.properties=/opt/jboss/wildfly/standalone/deployments/local.runtime.properties", "-b", "0.0.0.0"]
