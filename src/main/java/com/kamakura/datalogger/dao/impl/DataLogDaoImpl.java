package com.kamakura.datalogger.dao.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.datalogger.dao.DataLogDao;
import com.kamakura.datalogger.exception.DataLoggerException;
import com.kamakura.datalogger.model.DataLog;

@Repository
public class DataLogDaoImpl implements DataLogDao {

	@Autowired
	private SerialPortCommunicator serialPortCommunicator;
	
	/*
	 * Serial Number: position 1 - size 15
	 * Calibration Temperature: position 2 - size 3.2
	 * Sample Interval: position 3 - size 5
	 * Minimum Temperature: position 4 - size 3.2
	 * Maximum Temperature: position 5 - size 3.2
	 * Initial Read Time: position 6 - size 14 (yyyyMMddhhmiss)
	 * Final Read Time: position 7 - size 14 (yyyyMMddhhmiss)
	 * Samples: position 8 - size variable
	 */
	private static final Pattern dataPattern = Pattern.compile("(\\d{15})(\\d{3}\\.\\d{2})(\\d{5})(\\d{3}\\.\\d{2})(\\d{3}\\.\\d{2})(\\d{14})(\\d{14})(.*)");

	private static final Pattern samplesPattern = Pattern.compile("(-?\\d{3}\\.\\d{2})(.*)");

	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");

	@Override
	public DataLog readDataLog() {
		DataLog dataLog = null;
		
		String data = serialPortCommunicator.read();
		if(data != null) {
			Matcher dataMatcher = dataPattern.matcher(data);
		    
		    if (dataMatcher.find()) {
				dataLog = new DataLog();
		    	dataLog.setSerialNumber(new Long(dataMatcher.group(1))); 
		    	dataLog.setCalibrationTemperature(new BigDecimal(dataMatcher.group(2))); 
		    	dataLog.setSampleInterval(new Integer(dataMatcher.group(3))); 
		    	dataLog.setAlarmMinTemperature(new BigDecimal(dataMatcher.group(4))); 
		    	dataLog.setAlarmMaxTemperature(new BigDecimal(dataMatcher.group(5)));

		    	try {
		    		dataLog.setInitialReadTime(dateFormatter.parse(dataMatcher.group(6)));
		    	} catch(ParseException ex) {
		    		throw new DataLoggerException("error.parsing.initial.date");
		    	}
		    	try {
			    	dataLog.setFinalReadTime(dateFormatter.parse(dataMatcher.group(7))); 
		    	} catch(ParseException ex) {
		    		throw new RuntimeException("error.parsing.final.date");
		    	}
		    	
		    	Calendar calendar = GregorianCalendar.getInstance();
		    	calendar.setTime(dataLog.getInitialReadTime());
		    	
		    	String samples = dataMatcher.group(8);
				BigDecimal sum = new BigDecimal(0);
		    	boolean match = false;
		    	do {
			    	Matcher samplesMatcher = samplesPattern.matcher(samples);
			    	match = samplesMatcher.find();
			    	
			    	if(match) {
			    		BigDecimal temperature = new BigDecimal(samplesMatcher.group(1));
			        	dataLog.getSamples().put(calendar.getTime(), temperature);
						sum = sum.add(temperature);
						if(dataLog.getTopTemperature() == null || dataLog.getTopTemperature().compareTo(temperature) < 0 ) {
							dataLog.setTopTemperature(temperature); 
						}
						if(dataLog.getBottomTemperature() == null || dataLog.getBottomTemperature().compareTo(temperature) > 0 ) {
							dataLog.setBottomTemperature(temperature);
						}
						
						if(dataLog.getAlarmMaxTemperature().compareTo(temperature) < 0 ) {
							dataLog.addTimeAboveMaxTemperature(); 
						}
						if(dataLog.getAlarmMinTemperature().compareTo(temperature) > 0 ) {
							dataLog.addTimeUnderMinTemperature(); 
						}
						
			        	calendar.add(Calendar.MINUTE, dataLog.getSampleInterval());
				    	samples = samplesMatcher.group(2);
			    	}
		    	} while(match);
		    	
		    	if(!dataLog.getSamples().isEmpty()) {
					dataLog.setAverageTemperature(sum.divide(new BigDecimal(dataLog.getSamples().values().size()), 2, RoundingMode.HALF_UP));
	
					BigDecimal variance = new BigDecimal(0);
					for(BigDecimal sample : dataLog.getSamples().values()) {
						variance = variance.add(sample.subtract(dataLog.getAverageTemperature()).pow(2));
					}
					variance = variance.divide(new BigDecimal(dataLog.getSamples().values().size() - 1), 2, RoundingMode.HALF_UP);
					// There is no sqtr in BigDecimal watch for precision issues
					dataLog.setStandardDeviation(new BigDecimal(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP));
		    	}
		    }
		}

		return dataLog;
	}
}
