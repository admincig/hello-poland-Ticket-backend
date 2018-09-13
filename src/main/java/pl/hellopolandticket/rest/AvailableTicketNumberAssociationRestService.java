package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import java.util.Date;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.annotation.DateTimeFormat;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.service.AvailableTicketNumberAssociationService;
import pl.hellopolandticket.service.SightEventService;

@Path("/available-ticket-number-associations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AvailableTicketNumberAssociationRestService {

  @Inject
  private AvailableTicketNumberAssociationService service;

  @Inject
  private SightEventService seService;

  @Inject
  private PartnerDao partnerDao;

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response checkAvailabilityOfTickets(@QueryParam("sightEventId") Long id,
      @QueryParam("date") @DateTimeFormat Date date) {
    return Response.ok(service.checkAvailabilityOfTickets(id, date)).build();
    // return Response.ok(getMock()).build();
  }

  // private List<AvailableTicketNumberAssociationDTO> getMock() {
  // var partner = partnerDao.findByName("Zoo");
  //
  // var sightEvent = seService.findSightEventById(seService.findForPartner("Zoo").get(0).id);
  //
  // var tpdCic = getTicketPoolDefinition("cyclicalPool", -1, true, sightEvent);
  // var tdCic1 = getTicketDefinition("Ulgowy", 1500, partner, List.of(tpdCic));
  // var tdCic2 = getTicketDefinition("Normalny", 2000, partner, List.of(tpdCic));
  //
  // var tpdNonCic = getTicketPoolDefinition("nonCyclicalPool", 500, false, sightEvent);
  // var tdNonCic1 = getTicketDefinition("VIP", 10000, partner, List.of(tpdCic));
  // var tdNonCic2 = getTicketDefinition("Normalny", 3000, partner, List.of(tpdCic));
  //
  // var bo1 = ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(
  // AvailableTicketNumberAssociation.builder().ticketDefinition(tdCic1)
  // .ticketPoolDefinition(tpdCic).availableTicketsNumber(-1).build());
  //
  // var bo2 = ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(
  // AvailableTicketNumberAssociation.builder().ticketDefinition(tdCic2)
  // .ticketPoolDefinition(tpdCic).availableTicketsNumber(-1).build());
  //
  // var bo3 = ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(
  // AvailableTicketNumberAssociation.builder().ticketDefinition(tdNonCic1)
  // .ticketPoolDefinition(tpdNonCic).availableTicketsNumber(100).build());
  //
  // var bo4 = ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(
  // AvailableTicketNumberAssociation.builder().ticketDefinition(tdNonCic2)
  // .ticketPoolDefinition(tpdNonCic).availableTicketsNumber(389).build());
  //
  // return List.of(bo1, bo2, bo3, bo4);
  // }
  //
  // private TicketPoolDefinition getTicketPoolDefinition(String name, Integer
  // availableTicketsNumber,
  // boolean isCyclic, SightEvent sightEvent) {
  //
  // var tpd = TicketPoolDefinition.builder().sightEvent(sightEvent).name(name).isCyclic(isCyclic)
  // .availableTicketsNumber(availableTicketsNumber).deleted(false);
  //
  // Date startDate = new Date();
  // startDate.setSeconds(0);
  // startDate.setMinutes(0);
  // startDate.setHours(0);
  //
  // Date endDate = new Date();
  // endDate.setHours(23);
  // endDate.setMinutes(59);
  // endDate.setSeconds(59);
  //
  // if (isCyclic) {
  // var frequencyData = new FrequencyData();
  // frequencyData.setFrequencyType(FrequencyType.DAILY);
  // frequencyData.setFrequency(1);
  // frequencyData.setStartDate(startDate);
  // Calendar cal = Calendar.getInstance();
  // cal.set(Calendar.DAY_OF_YEAR, 365);
  // frequencyData.setEndDate(cal.getTime());
  // tpd.frequencyData(frequencyData).startDate(startDate).endDate(endDate);
  // }
  // if (!isCyclic) {
  // startDate.setMonth(startDate.getMonth() + 1);
  // endDate.setMonth(endDate.getMonth() + 1);
  // tpd.startDate(startDate).endDate(endDate);
  // }
  // return tpd.build();
  // }
  //
  // private TicketDefinition getTicketDefinition(String name, int price, Partner partner,
  // List<TicketPoolDefinition> ticketPoolDefinitions) {
  // var td = TicketDefinition.builder().name(name).price(price).partner(partner)
  // .ticketPoolDefinitions(ticketPoolDefinitions);
  // return td.build();
  // }

}
