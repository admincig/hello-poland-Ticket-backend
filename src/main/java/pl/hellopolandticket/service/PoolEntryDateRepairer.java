package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import java.lang.System.Logger.Level;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Singleton
@RunAs(value = ROLE_ADMIN)
public class PoolEntryDateRepairer extends ServiceSuperclass {

  @Inject
  private TicketPoolDefinitionService tpdService;

  private Long maxId;

  private Long lastId = 0l;

  private final int batch = 100;

    private boolean isDisabled() {
        // 1) -Dpool.entry.date.repairer.enabled=true/false
        String v = System.getProperty("pool.entry.date.repairer.enabled");
        if (v != null) return !Boolean.parseBoolean(v);

        // 2) ENV: POOL_ENTRY_DATE_REPAIRER_ENABLED=true/false
        v = System.getenv("POOL_ENTRY_DATE_REPAIRER_ENABLED");
        if (v != null) return !Boolean.parseBoolean(v);

        // domyślnie WYŁĄCZONE (hotfix)
        return true;
    }

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

  @Schedule(hour = "5", minute = "15", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  @Lock(LockType.WRITE)
  public void run() {
      if (isDisabled()) {
          logger.log(Level.INFO, "PoolEntryDateRepairer DISABLED");
          return;
      }

      logger.log(Level.TRACE, "PoolEntryDateRepairer start");

      if (lastId >= maxId) {
          logger.log(Level.INFO, "PoolEntryDateRepairer finished (lastId=" + lastId + ", maxId=" + maxId + ")");
          return;
      }

      logger.log(Level.INFO, "Start processing " + batch + " TicketPoolDefinitions. First id > " + lastId);

      var tpds = em.createQuery(
                      "from TicketPoolDefinition where id > :lastId order by id asc",
                      TicketPoolDefinition.class)
              .setParameter("lastId", lastId)
              .setMaxResults(batch)
              .getResultList();

      if (tpds.isEmpty()) {
          logger.log(Level.INFO, "No TicketPoolDefinitions found for lastId=" + lastId + " (maxId=" + maxId + ")");
          return;
      }

      tpdService.repairEntryDates(tpds);
      lastId = tpds.get(tpds.size() - 1).getId();

      logger.log(Level.INFO, "Stop processing " + tpds.size() + " TicketPoolDefinitions. Last id = " + lastId);
      logger.log(Level.TRACE, "PoolEntryDateRepairer end");
  }

}
