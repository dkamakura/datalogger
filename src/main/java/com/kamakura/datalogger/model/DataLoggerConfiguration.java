package com.kamakura.datalogger.model;

import java.math.BigDecimal;

public class DataLoggerConfiguration {

	private Long serialNumber;
	
	private BigDecimal calibrationTemperature;
	
	private BigDecimal alarmMinTemperature;
	
	private BigDecimal alarmMaxTemperature;

	private Integer sampleInterval;

	public Long getSerialNumber() {
    	return serialNumber;
    }

	public DataLoggerConfiguration setSerialNumber(Long serialNumber) {
    	this.serialNumber = serialNumber;
    	return this;
    }

	public BigDecimal getCalibrationTemperature() {
    	return calibrationTemperature;
    }

	public DataLoggerConfiguration setCalibrationTemperature(BigDecimal calibrationTemperature) {
    	this.calibrationTemperature = calibrationTemperature;
    	return this;
    }

	public BigDecimal getAlarmMinTemperature() {
    	return alarmMinTemperature;
    }

	public DataLoggerConfiguration setAlarmMinTemperature(BigDecimal alarmMinTemperature) {
    	this.alarmMinTemperature = alarmMinTemperature;
    	return this;
    }

	public BigDecimal getAlarmMaxTemperature() {
    	return alarmMaxTemperature;
    }

	public DataLoggerConfiguration setAlarmMaxTemperature(BigDecimal alarmMaxTemperature) {
    	this.alarmMaxTemperature = alarmMaxTemperature;
    	return this;
    }

	public Integer getSampleInterval() {
    	return sampleInterval;
    }

	public DataLoggerConfiguration setSampleInterval(Integer sampleInterval) {
    	this.sampleInterval = sampleInterval;
    	return this;
    }
}
