package de.vogella.jpa.eclipselink.main;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;


public class BookingCancelSchedulerTest {

  private static final String PERSISTENCE_UNIT_NAME = "JEE6Demo-Persistence";
  private EntityManagerFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    EntityManager em = factory.createEntityManager();

  }

  @Test
  public void checkAvailablePeople() {

    // now lets check the database and see if the created entries are there
    // create a fresh, new EntityManager
    EntityManager em = factory.createEntityManager();

    // Perform a simple query for all the Message entities
    Query q = em.createQuery("select m from Person m");

    // We should have 40 Persons in the database
    assertTrue(q.getResultList().size() == 40);

    em.close();
  }

  @Test
  public void checkFamily() {
    EntityManager em = factory.createEntityManager();
    // Go through each of the entities and print out each of their
    // messages, as well as the date on which it was created
    Query q = em.createQuery("select f from Family f");

    // We should have one family with 40 persons
    assertTrue(q.getResultList().size() == 1);
//    assertTrue(((Family) q.getSingleResult()).getMembers().size() == 40);
    em.close();
  }

  @Test(expected = javax.persistence.NoResultException.class)
  public void deletePerson() {
    EntityManager em = factory.createEntityManager();
    // Begin a new local transaction so that we can persist a new entity
    em.getTransaction().begin();
    Query q = em
        .createQuery("SELECT p FROM Person p WHERE p.firstName = :firstName AND p.lastName = :lastName");
    q.setParameter("firstName", "Jim_1");
    q.setParameter("lastName", "Knopf_!");
//    Person user = (Person) q.getSingleResult();
//    em.remove(user);
//    em.getTransaction().commit();
//    Person person = (Person) q.getSingleResult();
    // Begin a new local transaction so that we can persist a new entity

    em.close();
  }
}