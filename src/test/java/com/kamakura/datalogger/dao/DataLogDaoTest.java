package com.kamakura.datalogger.dao;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kamakura.datalogger.model.DataLog;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/*.xml"})
public class DataLogDaoTest {

	@Autowired
	private DataLogDao dataLogDao;
	
	@Test
	public void testReadDataLog() {
		DataLog dataLog = dataLogDao.readDataLog();
		assertNotNull(dataLog);
	}
}
