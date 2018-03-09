package pl.hellopoland.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import pl.hellopoland.model.Sample;

@Stateless
@LocalBean
public class SampleDao {

	@PersistenceContext
	private EntityManager entityManager;
	
	public String save() {
		entityManager.persist(new Sample("Sample"));
		
		return "Sample";
	}
}