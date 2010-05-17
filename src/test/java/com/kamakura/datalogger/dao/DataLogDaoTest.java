package com.kamakura.datalogger.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import com.kamakura.datalogger.model.DataLog;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/communication-spring-cfg.xml", "classpath*:/spring/test-datalogger-spring-cfg.xml"})
public class DataLogDaoTest {

	@Autowired
	private DataLogDao dataLogDao;

	Mockery mockery = new Mockery();

	private SerialPortCommunicator serialPortCommunicator = mockery.mock(SerialPortCommunicator.class);

	@Test
	public void testReadDataLog() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.520091206140220091225120078.512.101.100.076.280.193.5" + (char)3 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("12.1"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("45.3"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("78.5"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:00 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}
		
		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("78.5"), values[0]);
		assertEquals(new BigDecimal("12.1"), values[1]);
		assertEquals(new BigDecimal("01.1"), values[2]);
		assertEquals(new BigDecimal("00.0"), values[3]);
		assertEquals(new BigDecimal("76.2"), values[4]);
		assertEquals(new BigDecimal("80.1"), values[5]);
		assertEquals(new BigDecimal("93.5"), values[6]);
		
		assertEquals(new BigDecimal("93.5"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("00.0"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("48.8"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("42.1"), dataLog.getStandardDeviation());

		assertEquals(new Long(400), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(200), dataLog.getTimeUnderMinTemperature());

		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testReadDataLogNegativeValues() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("12345678901234500100-12.1-45.3-78.5200912061402200912251200-78.5-12.1-01.1-00.0-76.2-80.1-93.5" + (char)3 + DataLogDao.END_DATA_SIGNAL));

			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("-12.1"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("-45.3"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("-78.5"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:00 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}

		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("-78.5"), values[0]);
		assertEquals(new BigDecimal("-12.1"), values[1]);
		assertEquals(new BigDecimal("-01.1"), values[2]);
		assertEquals(new BigDecimal("-00.0"), values[3]);
		assertEquals(new BigDecimal("-76.2"), values[4]);
		assertEquals(new BigDecimal("-80.1"), values[5]);
		assertEquals(new BigDecimal("-93.5"), values[6]);
		
		assertEquals(new BigDecimal("00.0"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("-93.5"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("-48.8"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("42.1"), dataLog.getStandardDeviation());

		assertEquals(new Long(300), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(400), dataLog.getTimeUnderMinTemperature());

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogNullData() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue(null));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.invalid.data", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogEmptyData() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue(""));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.invalid.data", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogInvalidData() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("invalid data"));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.invalid.data", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testReadDataLogCorruptedData() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.520091206140220091225120078.512.101.100.076.280.193.5" + (char)5 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.corrupted.data", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogInvalidInitialDate() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.520091306140220091225120078.512.101.100.076.280.193.5" + (char)2 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.parsing.initial.date", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogInvalidFinalDate() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.520091206140220091232120078.512.101.100.076.280.193.5" + (char)5 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.parsing.final.date", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogNoSamples() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.5200912061402200912251200" + (char)16 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			dataLogDao.readDataLog();
		} catch(DataLoggerException ex) {
			assertEquals("error.no.samples.returned", ex.getMessage());
		}

		mockery.assertIsSatisfied();
	}

	@Test
	public void testReadDataLogSingleSample() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("1234567890123450010012.145.378.520091206140220091225120078.5" + (char)4 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("12.1"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("45.3"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("78.5"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:00 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}

		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("78.5"), values[0]);
		
		assertEquals(new BigDecimal("78.5"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("78.5"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("78.5"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("0.0"), dataLog.getStandardDeviation());

		assertEquals(new Long(100), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(0), dataLog.getTimeUnderMinTemperature());

		mockery.assertIsSatisfied();
	}

}
