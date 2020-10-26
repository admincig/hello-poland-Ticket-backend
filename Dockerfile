FROM docker.fream.pl/wildfly:16-adoptopenjdk-11-openj9
RUN touch /opt/local.runtime.properties
COPY target/helloticket.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-Dlocal.runtime.properties=/opt/local.runtime.properties", "-b", "0.0.0.0"]
