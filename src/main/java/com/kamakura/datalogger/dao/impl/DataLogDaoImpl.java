package com.kamakura.datalogger.dao.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.kamakura.datalogger.util.DataLoggerUtil;

@Repository
public class DataLogDaoImpl implements DataLogDao {

	@Autowired
	private SerialPortCommunicator serialPortCommunicator;
	
	/*
	 * Serial Number: position 1 - size 15
	 * Sample Interval: position 2 - size 5
	 * Minimum Temperature: position 3 - size (-)2.1
	 * Maximum Temperature: position 4 - size (-)2.1
	 * Calibration Temperature: position 5 - size (-)2.1
	 * Samples: position 6 - size variable
	 */
	private static final Pattern dataPattern = Pattern.compile("((\\d{15})(\\d{5})(-?\\d{2}\\.\\d{1})(-?\\d{2}\\.\\d{1})(-?\\d{2}\\.\\d{1})((-?\\d{2}\\.\\d{1})*))(.*)" + END_DATA_SIGNAL);

	private static final Pattern samplesPattern = Pattern.compile("(.*?)(-?\\d{2}\\.\\d{1})$");

	private static final Integer SCALE = 1;

	@Override
	public DataLog readDataLog() {
		DataLog dataLog = null;
		
		serialPortCommunicator.write(START_DATA_SIGNAL);

		String data = serialPortCommunicator.read();
		if(data != null) {
			Matcher dataMatcher = dataPattern.matcher(data);
		    
		    if (dataMatcher.find()) {
		    	String fullData = dataMatcher.group(1);
		    	char dataLrc = dataMatcher.group(9).toCharArray()[0];
		    	
		    	if(dataLrc != DataLoggerUtil.calculateLRC(fullData)) {
		    		throw new DataLoggerException("error.corrupted.data");
		    	}
		    	
				dataLog = new DataLog();
		    	dataLog.setSerialNumber(new Long(dataMatcher.group(2))); 
		    	dataLog.setSampleInterval(new Integer(dataMatcher.group(3))); 
		    	dataLog.setAlarmMinTemperature(new BigDecimal(dataMatcher.group(4))); 
		    	dataLog.setAlarmMaxTemperature(new BigDecimal(dataMatcher.group(5)));
		    	dataLog.setCalibrationTemperature(new BigDecimal(dataMatcher.group(6))); 
	
		    	Calendar finalReadTimeCalendar = GregorianCalendar.getInstance();
		    	
		    	dataLog.setFinalReadTime(finalReadTimeCalendar.getTime()); 
		    	
		    	String samples = dataMatcher.group(7);
				BigDecimal totalTemperatureSum = new BigDecimal(0);
		    	boolean match = false;
		    	do {
			    	Matcher samplesMatcher = samplesPattern.matcher(samples);
			    	match = samplesMatcher.find();
			    	
			    	if(match) {
			    		BigDecimal temperature = new BigDecimal(samplesMatcher.group(2));
			        	dataLog.getSamples().put(finalReadTimeCalendar.getTime(), temperature);
						totalTemperatureSum = totalTemperatureSum.add(temperature);
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
						
				    	dataLog.setInitialReadTime(finalReadTimeCalendar.getTime());
				    	
						finalReadTimeCalendar.add(Calendar.MINUTE, - dataLog.getSampleInterval());
				    	samples = samplesMatcher.group(1);
			    	}
		    	} while(match);
		    	
		    	if(!dataLog.getSamples().isEmpty()) {
					dataLog.setAverageTemperature(totalTemperatureSum.divide(new BigDecimal(dataLog.getSamples().values().size()), SCALE, RoundingMode.HALF_UP));
	
					BigDecimal variance = new BigDecimal(0);
					for(BigDecimal sample : dataLog.getSamples().values()) {
						variance = variance.add(sample.subtract(dataLog.getAverageTemperature()).pow(2));
					}
					if(dataLog.getSamples().values().size() > 1) {
						variance = variance.divide(new BigDecimal(dataLog.getSamples().values().size() - 1), SCALE, RoundingMode.HALF_UP);
					}
					// There is no sqtr in BigDecimal watch for precision issues
					dataLog.setStandardDeviation(new BigDecimal(Math.sqrt(variance.doubleValue())).setScale(SCALE, RoundingMode.HALF_UP));
		    	} else {
		    		throw new DataLoggerException("error.no.samples.returned");
		    	}
		    }
		}

		if(dataLog == null) {
    		throw new DataLoggerException("error.invalid.data");
		}
		return dataLog;
	}
}
