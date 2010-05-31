package com.kamakura.datalogger.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.datalogger.dao.ConfigurationDao;
import com.kamakura.datalogger.exception.DataLoggerException;
import com.kamakura.datalogger.model.DataLoggerConfiguration;
import com.kamakura.datalogger.util.DataLoggerUtil;

@Repository
public class ConfigurationDaoImpl implements ConfigurationDao {

	@Autowired
	private SerialPortCommunicator serialPortCommunicator;

	@Override
	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration) {

		String message = START_CONFIGURE_SIGNAL + this.configurationToData(dataLoggerConfiguration) + END_CONFIGURE_SIGNAL;
		serialPortCommunicator.write(message);

		String ack = serialPortCommunicator.read();
		if (!ACK_CONFIGURE_SIGNAL.equals(ack)) {
			throw new DataLoggerException("error.ack.configure.signal.not.found");
		}
	}

	private String configurationToData(DataLoggerConfiguration dataLoggerConfiguration) {
		StringBuilder message = new StringBuilder();
		message.append(DataLoggerUtil.formatSerialNumber(dataLoggerConfiguration.getSerialNumber()));
		message.append(DataLoggerUtil.formatSampleInterval(dataLoggerConfiguration.getSampleInterval()));
		message.append(DataLoggerUtil.formatTemperature(dataLoggerConfiguration.getAlarmMinTemperature()));
		message.append(DataLoggerUtil.formatTemperature(dataLoggerConfiguration.getAlarmMaxTemperature()));
		message.append(DataLoggerUtil.formatTemperature(dataLoggerConfiguration.getCalibrationTemperature()));
		String messageLRC = DataLoggerUtil.addLRC(message.toString()); 
		return messageLRC;
	}
}
