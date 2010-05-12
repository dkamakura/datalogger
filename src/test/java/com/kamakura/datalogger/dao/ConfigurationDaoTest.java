package com.kamakura.datalogger.dao;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kamakura.datalogger.model.DataLoggerConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/*.xml"})
public class ConfigurationDaoTest {

	@Autowired
	private ConfigurationDao configurationDao;
	
	@Test
	public void testWriteConfiguration() {
		DataLoggerConfiguration dataLoggerConfiguration = new DataLoggerConfiguration();
		dataLoggerConfiguration.setSerialNumber(new Long("123456789012345"))
			.setSampleInterval(10000)
			.setCalibrationTemperature(new BigDecimal("123.12"))
			.setAlarmMinTemperature(new BigDecimal("456.34"))
			.setAlarmMaxTemperature(new BigDecimal("789.56"));
		configurationDao.writeConfiguration(dataLoggerConfiguration);
	}
}
