package com.kamakura.datalogger.dao.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kamakura.communication.SerialPortCommunicator;
import com.kamakura.datalogger.dao.ConfigurationDao;
import com.kamakura.datalogger.exception.DataLoggerException;
import com.kamakura.datalogger.model.DataLoggerConfiguration;
import com.kamakura.datalogger.util.DataLoggerUtil;

@Repository
public class ConfigurationDaoImpl implements ConfigurationDao {

    private static final NumberFormat temperatureFormatter = new DecimalFormat("000.00") {
        private static final long serialVersionUID = 6070138055311487837L;
	{
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    	decimalFormatSymbols.setDecimalSeparator('.');
    	setDecimalFormatSymbols(decimalFormatSymbols);
    }};

    private static final NumberFormat serialNumberFormatter = new DecimalFormat("000000000000000");

    private static final NumberFormat sampleIntervalFormatter = new DecimalFormat("00000");

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
		message.append(serialNumberFormatter.format(dataLoggerConfiguration.getSerialNumber()));
		message.append(sampleIntervalFormatter.format(dataLoggerConfiguration.getSampleInterval()));
		message.append(temperatureFormatter.format(dataLoggerConfiguration.getAlarmMinTemperature().doubleValue()));
		message.append(temperatureFormatter.format(dataLoggerConfiguration.getAlarmMaxTemperature().doubleValue()));
		message.append(temperatureFormatter.format(dataLoggerConfiguration.getCalibrationTemperature().doubleValue()));
		String messageLRC = DataLoggerUtil.addLRC(message.toString()); 
		return messageLRC;
	}
}
