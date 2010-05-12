package com.kamakura.datalogger.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class DataLog {

	private Long serialNumber;
	
	private BigDecimal calibrationTemperature;

	private BigDecimal alarmMinTemperature;
	
	private BigDecimal alarmMaxTemperature;
	
	private Map<Date, BigDecimal> samples = new TreeMap<Date, BigDecimal>();
	
	private BigDecimal topTemperature;
	
	private BigDecimal bottomTemperature;
	
	private Date initialReadTime;
	
	private Date finalReadTime;
	
	private BigDecimal averageTemperature;

	private BigDecimal standardDeviation;
	
	private Long timeUnderMinTemperature = new Long(0);
	
	private Long timeAboveMaxTemperature = new Long(0);
	
	private Integer sampleInterval;
	
	public Long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public BigDecimal getCalibrationTemperature() {
    	return calibrationTemperature;
    }

	public void setCalibrationTemperature(BigDecimal calibrationTemperature) {
    	this.calibrationTemperature = calibrationTemperature;
    }

	public BigDecimal getAlarmMinTemperature() {
		return alarmMinTemperature;
	}

	public void setAlarmMinTemperature(BigDecimal alarmMinTemperature) {
		this.alarmMinTemperature = alarmMinTemperature;
	}

	public BigDecimal getAlarmMaxTemperature() {
		return alarmMaxTemperature;
	}

	public void setAlarmMaxTemperature(BigDecimal alarmMaxTemperature) {
		this.alarmMaxTemperature = alarmMaxTemperature;
	}

	public Map<Date, BigDecimal> getSamples() {
		return samples;
	}

	public void setSamples(Map<Date, BigDecimal> samples) {
		this.samples = samples;
	}

	public BigDecimal getTopTemperature() {
		return topTemperature;
	}

	public void setTopTemperature(BigDecimal topTemperature) {
		this.topTemperature = topTemperature;
	}

	public BigDecimal getBottomTemperature() {
		return bottomTemperature;
	}

	public void setBottomTemperature(BigDecimal bottomTemperature) {
		this.bottomTemperature = bottomTemperature;
	}

	public Date getInitialReadTime() {
		return initialReadTime;
	}

	public void setInitialReadTime(Date initialReadTime) {
		this.initialReadTime = initialReadTime;
	}

	public Date getFinalReadTime() {
		return finalReadTime;
	}

	public void setFinalReadTime(Date finalReadTime) {
		this.finalReadTime = finalReadTime;
	}

	public BigDecimal getAverageTemperature() {
		return averageTemperature;
	}

	public void setAverageTemperature(BigDecimal averageTemperature) {
		this.averageTemperature = averageTemperature;
	}

	public BigDecimal getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(BigDecimal standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public Long getTimeUnderMinTemperature() {
		return timeUnderMinTemperature;
	}

	public void setTimeUnderMinTemperature(Long timeUnderMinTemperature) {
		this.timeUnderMinTemperature = timeUnderMinTemperature;
	}

	public Long getTimeAboveMaxTemperature() {
		return timeAboveMaxTemperature;
	}

	public void setTimeAboveMaxTemperature(Long timeAboveMaxTemperature) {
		this.timeAboveMaxTemperature = timeAboveMaxTemperature;
	}

	public Integer getSampleInterval() {
		return sampleInterval;
	}

	public void setSampleInterval(Integer sampleInterval) {
		this.sampleInterval = sampleInterval;
	}
	
	public void addTimeUnderMinTemperature() {
		this.timeUnderMinTemperature += this.sampleInterval;
	}
	
	public void addTimeAboveMaxTemperature() {
		this.timeAboveMaxTemperature += this.sampleInterval;
	}
	
}
