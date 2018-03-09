package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import pl.hellopolandticket.dao.SampleDao;

@Stateless
@LocalBean
public class SampleService {

	@Inject
	private SampleDao sampleDao;
	
	public String save() {
		return sampleDao.save();
	}
}