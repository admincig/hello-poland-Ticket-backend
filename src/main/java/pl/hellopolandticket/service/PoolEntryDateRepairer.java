package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import java.lang.System.Logger.Level;
import javax.annotation.PostConstruct;
import javax.annotation.security.RunAs;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Singleton
@RunAs(value = ROLE_ADMIN)
public class PoolEntryDateRepairer extends ServiceSuperclass {

  @Inject
  private TicketPoolDefinitionService tpdService;

  private Long maxId;

  private Long lastId = 0l;

  private final int batch = 100;

  @PostConstruct
  public void init() {
    try {
      maxId = em.createQuery("select id from TicketPoolDefinition order by id desc", Long.class)
          .setMaxResults(1).getSingleResult();
    } catch (NoResultException e) {
      maxId = 0l;
      logger.log(Level.WARNING, "No TPDs in database");
    }
  }

  @Schedule(hour = "*", minute = "*/5", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  @Lock(LockType.WRITE)
  public void run() {
    logger.log(Level.INFO, "PoolEntryDateRepairer start");

    if (lastId < maxId) {
      logger.log(Level.INFO,
          "Start processing " + batch + " TicketPoolDefinitions. First id > " + lastId);
      var tpds = em
          .createQuery("from TicketPoolDefinition where id > :lastId order by id asc",
              TicketPoolDefinition.class)
          .setParameter("lastId", lastId).setMaxResults(batch).getResultList();
      tpdService.repairEntryDates(tpds);
      lastId = tpds.get(tpds.size() - 1).getId();
      logger.log(Level.INFO,
          "Stop processing " + batch + " TicketPoolDefinitions. Last id = " + lastId);

    }
    logger.log(Level.INFO, "PoolEntryDateRepairer end");
  }

}
