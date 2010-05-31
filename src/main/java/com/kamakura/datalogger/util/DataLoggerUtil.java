/**
 * DataLoggerUtil.java
 *
 * Copyright 2010-2010 Daniel de Aguiar Kamakura
 */
package com.kamakura.datalogger.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kamakura.datalogger.dao.DataLogDao;


public class DataLoggerUtil {

    private static final NumberFormat viewTemperatureFormatter = new DecimalFormat("#0.0") {
		private static final long serialVersionUID = -652638873419710285L;
	{
		setMaximumFractionDigits(1);
		setMaximumIntegerDigits(2);
    }};

    private static final NumberFormat viewSerialNumberFormatter = new DecimalFormat("000000000000000") {
		private static final long serialVersionUID = -5871129456638648158L;
	{
    	setMaximumFractionDigits(0);
    	setMaximumIntegerDigits(15);
    }};
    
    private static final NumberFormat viewSampleIntervalFormatter = new DecimalFormat("####0") {
		private static final long serialVersionUID = 7089678915678201506L;
	{
    	setMaximumFractionDigits(0);
    	setMaximumIntegerDigits(5);
    }};

    private static final NumberFormat temperatureFormatter = new DecimalFormat("00.0") {
        private static final long serialVersionUID = 6070138055311487837L;
	{
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    	decimalFormatSymbols.setDecimalSeparator('.');
    	setDecimalFormatSymbols(decimalFormatSymbols);
		setMaximumFractionDigits(1);
		setMaximumIntegerDigits(2);
    }};

    private static final NumberFormat serialNumberFormatter = new DecimalFormat("000000000000000");
    
    private static final NumberFormat sampleIntervalFormatter = new DecimalFormat("00000");

	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmm") {
		private static final long serialVersionUID = -3406846023052830108L;
	{
		setLenient(false);
	}};

	private static final DateFormat viewDateFormatter = new SimpleDateFormat() {
		private static final long serialVersionUID = 9176685253576938462L;
	{
		setLenient(false);
	}};
	
	public static NumberFormat getTemperatureformatter() {
		return temperatureFormatter;
	}

	public static NumberFormat getSerialNumberformatter() {
		return serialNumberFormatter;
	}

	public static NumberFormat getSampleintervalformatter() {
		return sampleIntervalFormatter;
	}

	public static NumberFormat getViewTemperatureformatter() {
		return viewTemperatureFormatter;
	}

	public static NumberFormat getViewSerialNumberformatter() {
		return viewSerialNumberFormatter;
	}

	public static NumberFormat getViewSampleintervalformatter() {
		return viewSampleIntervalFormatter;
	}
	
	public static DateFormat getDateformatter() {
		return dateFormatter;
	}

	public static String formatViewSerialNumber(Long serialNumber) {
		return viewSerialNumberFormatter.format(serialNumber);
	}
	
	public static String formatSerialNumber(Long serialNumber) {
		return serialNumberFormatter.format(serialNumber);
	}
	
	public static String formatDate(Date date) {
		return dateFormatter.format(date);
	}

	public static String formatViewDate(Date date) {
		return viewDateFormatter.format(date);
	}
	
	public static Date parseDate(String date) throws ParseException {
		return dateFormatter.parse(date);
	}
	
	public static String formatSampleInterval(Integer sampleInterval) {
		return sampleIntervalFormatter.format(sampleInterval);
	}
	
	public static String formatTemperature(BigDecimal temperature) {
		return temperatureFormatter.format(temperature.doubleValue());
	}

	public static String formatViewTemperature(BigDecimal temperature) {
		return viewTemperatureFormatter.format(temperature.doubleValue());
	}

	public static BigDecimal parseTemperature(String temperature) throws ParseException {
		return new BigDecimal(temperatureFormatter.parse(temperature).doubleValue());
	}

	public static char calculateLRC(String str) {
		char lrc = 0;
		char[] charArray = str.toCharArray();
		for(char c : charArray) {
			lrc = (char)(lrc ^ c);
		}
		return lrc;
	}

	public static String addLRC(String str) {
		return str + DataLoggerUtil.calculateLRC(str);
	}
	
	public static void main(String[] args) {
		char t = DataLoggerUtil.calculateLRC("12345678901234501000-12.145.378.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.518.5-05.101.100.046.230.1-10.5");

		System.out.println((int)t);
	}
}
