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
	 * Initial Read Time: position 6 - size 12 (yyyyMMddhhmi)
	 * Final Read Time: position 7 - size 12 (yyyyMMddhhmi)
	 * Samples: position 8 - size variable
	 */
	private static final Pattern dataPattern = Pattern.compile("((\\d{15})(\\d{5})(-?\\d{2}\\.\\d{1})(-?\\d{2}\\.\\d{1})(-?\\d{2}\\.\\d{1})(\\d{12})(\\d{12})((-?\\d{2}\\.\\d{1})*))(.*)" + END_DATA_SIGNAL);

	private static final Pattern samplesPattern = Pattern.compile("(-?\\d{2}\\.\\d{1})(.*)");

	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm") {{
		setLenient(false);
	}};
	
	private static final Integer SCALE = 1;

	@Override
	public DataLog readDataLog() {
		DataLog dataLog = null;
		
		serialPortCommunicator.write(START_DATA_SIGNAL);

		String data = serialPortCommunicator.read();
		if(data != null && !data.equals("")) {
			Matcher dataMatcher = dataPattern.matcher(data);
		    
		    if (dataMatcher.find()) {
		    	String fullData = dataMatcher.group(1);
		    	char dataLrc = dataMatcher.group(11).toCharArray()[0];
		    	
		    	if(dataLrc != DataLoggerUtil.calculateLRC(fullData)) {
		    		throw new DataLoggerException("error.corrupted.data");
		    	}
		    	
				dataLog = new DataLog();
		    	dataLog.setSerialNumber(new Long(dataMatcher.group(2))); 
		    	dataLog.setSampleInterval(new Integer(dataMatcher.group(3))); 
		    	dataLog.setAlarmMinTemperature(new BigDecimal(dataMatcher.group(4))); 
		    	dataLog.setAlarmMaxTemperature(new BigDecimal(dataMatcher.group(5)));
		    	dataLog.setCalibrationTemperature(new BigDecimal(dataMatcher.group(6))); 

		    	try {
		    		dataLog.setInitialReadTime(dateFormatter.parse(dataMatcher.group(7)));
		    	} catch(ParseException ex) {
		    		throw new DataLoggerException("error.parsing.initial.date");
		    	}
		    	try {
			    	dataLog.setFinalReadTime(dateFormatter.parse(dataMatcher.group(8))); 
		    	} catch(ParseException ex) {
		    		throw new DataLoggerException("error.parsing.final.date");
		    	}
		    	
		    	Calendar calendar = GregorianCalendar.getInstance();
		    	calendar.setTime(dataLog.getInitialReadTime());
		    	
		    	String samples = dataMatcher.group(9);
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
					dataLog.setAverageTemperature(sum.divide(new BigDecimal(dataLog.getSamples().values().size()), SCALE, RoundingMode.HALF_UP));
	
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
		    } else {
	    		throw new DataLoggerException("error.invalid.data");
		    }
		}

		return dataLog;
	}
}
