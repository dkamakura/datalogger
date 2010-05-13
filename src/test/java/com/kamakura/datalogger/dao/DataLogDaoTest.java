package com.kamakura.datalogger.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
@ContextConfiguration(locations = { "classpath*:/spring/*.xml" })
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
				will(returnValue("12345678901234500100123.12456.34789.562009120614020220091225120000789.56123.12001.01000.00076.22080.12093.50" + (char)3 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("123.12"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("456.34"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("789.56"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:02 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}
		
		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("789.56"), values[0]);
		assertEquals(new BigDecimal("123.12"), values[1]);
		assertEquals(new BigDecimal("001.01"), values[2]);
		assertEquals(new BigDecimal("000.00"), values[3]);
		assertEquals(new BigDecimal("076.22"), values[4]);
		assertEquals(new BigDecimal("080.12"), values[5]);
		assertEquals(new BigDecimal("093.50"), values[6]);
		
		assertEquals(new BigDecimal("789.56"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("000.00"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("166.22"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("278.73"), dataLog.getStandardDeviation());

		assertEquals(new Long(100), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(500), dataLog.getTimeUnderMinTemperature());

		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testReadDataLogNegativeValues() {
		mockery.checking(new Expectations() {
			{
				one(serialPortCommunicator).write(DataLogDao.START_DATA_SIGNAL);
				one(serialPortCommunicator).read();
				will(returnValue("12345678901234500100-123.12-456.34-789.562009120614020220091225120000-789.56-123.12-001.01-000.00-076.22-080.12-093.50" + (char)3 + DataLogDao.END_DATA_SIGNAL));

			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("-123.12"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("-456.34"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("-789.56"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:02 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}

		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("-789.56"), values[0]);
		assertEquals(new BigDecimal("-123.12"), values[1]);
		assertEquals(new BigDecimal("-001.01"), values[2]);
		assertEquals(new BigDecimal("-000.00"), values[3]);
		assertEquals(new BigDecimal("-076.22"), values[4]);
		assertEquals(new BigDecimal("-080.12"), values[5]);
		assertEquals(new BigDecimal("-093.50"), values[6]);
		
		assertEquals(new BigDecimal("000.00"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("-789.56"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("-166.22"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("278.73"), dataLog.getStandardDeviation());

		assertEquals(new Long(600), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(100), dataLog.getTimeUnderMinTemperature());

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

		DataLog dataLog = dataLogDao.readDataLog();

		assertNull("Datalog is not null", dataLog);

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

		DataLog dataLog = dataLogDao.readDataLog();

		assertNull("Datalog is not null", dataLog);

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
			DataLog dataLog = dataLogDao.readDataLog();
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
				will(returnValue("12345678901234500100123.12456.34789.562009120614020220091225120000789.56123.12001.01000.00076.22080.12093.50" + (char)5 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			DataLog dataLog = dataLogDao.readDataLog();
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
				will(returnValue("12345678901234500100123.12456.34789.562009130614020220091225120000789.56123.12001.01000.00076.22080.12093.50" + (char)2 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			DataLog dataLog = dataLogDao.readDataLog();
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
				will(returnValue("12345678901234500100123.12456.34789.562009120614020220091232120000789.56123.12001.01000.00076.22080.12093.50" + (char)5 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			DataLog dataLog = dataLogDao.readDataLog();
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
				will(returnValue("12345678901234500100123.12456.34789.562009120614020220091225120000" + (char)30 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		try {
			DataLog dataLog = dataLogDao.readDataLog();
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
				will(returnValue("12345678901234500100123.12456.34789.562009120614020220091225120000789.56" + (char)5 + DataLogDao.END_DATA_SIGNAL));
			}
		});

		ReflectionTestUtils.setField(dataLogDao, "serialPortCommunicator", serialPortCommunicator);

		DataLog dataLog = dataLogDao.readDataLog();

		assertNotNull("Datalog is null", dataLog);
		assertEquals(new Long("123456789012345"), dataLog.getSerialNumber());
		assertEquals(new Integer("100"), dataLog.getSampleInterval());
		assertEquals(new BigDecimal("123.12"), dataLog.getAlarmMinTemperature());
		assertEquals(new BigDecimal("456.34"), dataLog.getAlarmMaxTemperature());
		assertEquals(new BigDecimal("789.56"), dataLog.getCalibrationTemperature());
		assertEquals("Sun Dec 06 14:02:02 BRST 2009", dataLog.getInitialReadTime().toString());
		assertEquals("Fri Dec 25 12:00:00 BRST 2009", dataLog.getFinalReadTime().toString());

		assertNotNull("Samples is null", dataLog.getSamples());

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dataLog.getInitialReadTime());

		for(Date date : dataLog.getSamples().keySet()) {
			assertEquals(calendar.getTime(), date);
			calendar.add(Calendar.MINUTE, new Integer("100"));
		}

		BigDecimal[] values = dataLog.getSamples().values().toArray(new BigDecimal[0]);
		assertEquals(new BigDecimal("789.56"), values[0]);
		
		assertEquals(new BigDecimal("789.56"), dataLog.getTopTemperature());
		assertEquals(new BigDecimal("789.56"), dataLog.getBottomTemperature());

		assertEquals(new BigDecimal("789.56"), dataLog.getAverageTemperature());
		assertEquals(new BigDecimal("0.00"), dataLog.getStandardDeviation());

		assertEquals(new Long(100), dataLog.getTimeAboveMaxTemperature());
		assertEquals(new Long(0), dataLog.getTimeUnderMinTemperature());

		mockery.assertIsSatisfied();
	}

}
