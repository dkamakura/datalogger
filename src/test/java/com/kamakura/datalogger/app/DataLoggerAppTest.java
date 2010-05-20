/*
 * DataLoggerApp.java
 */

package com.kamakura.datalogger.app;

import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.datalogger.dao.ConfigurationDao;
import com.kamakura.datalogger.dao.DataLogDao;

/**
 * The main class of the application.
 */
public class DataLoggerAppTest extends DataLoggerApp {
	Logger logger = Logger.getLogger(DataLoggerAppTest.class);

	Mockery mockery = new Mockery();

	private SerialPortCommunicator serialPortCommunicator = mockery.mock(SerialPortCommunicator.class);

	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override
	protected void startup() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:/spring/*.xml");
		
		mockery.checking(new Expectations() {{
			one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
			one(serialPortCommunicator).read();
			will(returnValue("1234567890123450010012.145.378.520091206140220091225120078.512.101.100.076.280.193.5" + (char)3 + DataLogDao.END_DATA_SIGNAL));
			one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
			one(serialPortCommunicator).read();
			will(returnValue("12345678901234500100-12.145.378.520091206140220091225120018.5-05.101.100.046.230.1-10.5" + (char)45 + DataLogDao.END_DATA_SIGNAL));
			one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
			one(serialPortCommunicator).read();
			will(returnValue("12345678901234501000-12.145.378.520091206140220091225120018.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.5" + (char)61 + DataLogDao.END_DATA_SIGNAL));
		}});
		
		ConfigurationDao configurationDao = applicationContext.getBean(ConfigurationDao.class);
		DataLogDao dataLogDao = applicationContext.getBean(DataLogDao.class);
		
		ReflectionTestUtils.setField(configurationDao, "serialPortCommunicator", serialPortCommunicator);
		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);
		
		show(applicationContext.getBean(DataLoggerView.class));
	}

	/**
	 * Main method launching the application.
	 */
	public static void main(String[] args) {
		launch(DataLoggerAppTest.class, args);
	}
	
	@Test
	public void testOk() {
		
	}
}
