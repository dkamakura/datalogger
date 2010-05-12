package com.kamakura.datalogger.dao;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.datalogger.exception.DataLoggerException;
import com.kamakura.datalogger.model.DataLoggerConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/*.xml"})
public class ConfigurationDaoTest {

	@Autowired
	private ConfigurationDao configurationDao;

	Mockery mockery = new Mockery();

	private SerialPortCommunicator serialPortCommunicator = mockery.mock(SerialPortCommunicator.class);

	@Test
	public void testWriteConfiguration() {
		
		mockery.checking(new Expectations() {{
            one(serialPortCommunicator).write(ConfigurationDao.START_CONFIGURE_SIGNAL + "12345678901234510000123.12456.34789.56" + (char)25 + ConfigurationDao.END_CONFIGURE_SIGNAL);
            one(serialPortCommunicator).read();
            will(returnValue(ConfigurationDao.ACK_CONFIGURE_SIGNAL));
        }});
		
		ReflectionTestUtils.setField(configurationDao, "serialPortCommunicator", serialPortCommunicator);
		
		DataLoggerConfiguration dataLoggerConfiguration = new DataLoggerConfiguration();
		dataLoggerConfiguration.setSerialNumber(new Long("123456789012345"))
			.setSampleInterval(10000)
			.setAlarmMinTemperature(new BigDecimal("123.12"))
			.setAlarmMaxTemperature(new BigDecimal("456.34"))
			.setCalibrationTemperature(new BigDecimal("789.56"));
		configurationDao.writeConfiguration(dataLoggerConfiguration);
		
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testWriteConfigurationNoAck() {
		
		mockery.checking(new Expectations() {{
            one(serialPortCommunicator).write(ConfigurationDao.START_CONFIGURE_SIGNAL + "12345678901234510000123.12456.34789.56" + (char)25 + ConfigurationDao.END_CONFIGURE_SIGNAL);
            one(serialPortCommunicator).read();
        }});
		
		ReflectionTestUtils.setField(configurationDao, "serialPortCommunicator", serialPortCommunicator);
		
		DataLoggerConfiguration dataLoggerConfiguration = new DataLoggerConfiguration();
		dataLoggerConfiguration.setSerialNumber(new Long("123456789012345"))
			.setSampleInterval(10000)
			.setAlarmMinTemperature(new BigDecimal("123.12"))
			.setAlarmMaxTemperature(new BigDecimal("456.34"))
			.setCalibrationTemperature(new BigDecimal("789.56"));
		
		try {
			configurationDao.writeConfiguration(dataLoggerConfiguration);
		} catch (DataLoggerException ex) {
			assertEquals("error.ack.configure.signal.not.found", ex.getMessage());
			assertEquals("Sinal de Confirmação de Configuração não encontrado.", ex.getLocalizedMessage());
		}
		mockery.assertIsSatisfied();
	}
	
}
