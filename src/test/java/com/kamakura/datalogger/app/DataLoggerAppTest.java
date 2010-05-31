/*
 * DataLoggerApp.java
 */

package com.kamakura.datalogger.app;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.communication.session.SerialPortSession;
import com.kamakura.datalogger.dao.ConfigurationDao;
import com.kamakura.datalogger.dao.DataLogDao;

/**
 * The main class of the application.
 */
public class DataLoggerAppTest extends DataLoggerApp {
	Logger logger = Logger.getLogger(DataLoggerAppTest.class);

	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override
	protected void startup() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:/spring/*.xml");

		SerialPortCommunicator serialPortCommunicator = new SerialPortCommunicator() {
			private String[] data = {
					"0000567890123450010012.145.378.578.512.101.100.076.280.193.5" + (char)2 + DataLogDao.END_DATA_SIGNAL,
					"12345678901234500100-12.145.378.518.5-05.101.100.046.230.1-10.5" + (char)40 + DataLogDao.END_DATA_SIGNAL,
					"12345678901234501000-12.145.378.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.5" + (char)56 + DataLogDao.END_DATA_SIGNAL
			};
			
			@Override
			public void write(String value) {
			}
			
			@Override
			public void setSerialPortSession(SerialPortSession serialPortSession) {
			}
			
			@Override
			public String read() {
				int i = (int)(Math.random() * 3);
				return data[i];
			}
		};
		
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
