/**
 * SerialPortException.java
 *
 * Copyright 2010-2010 Daniel de Aguiar Kamakura
 */
package com.kamakura.datalogger.exception;

import com.kamakura.communication.i18n.MessageSourceUtil;

/**
 * @author Daniel de Aguiar Kamakura
 * 
 * @since 1.0
 */
public class DataLoggerBusinessException extends RuntimeException {

	private static final long serialVersionUID = 593714320635047471L;

	private Object[] params;

	public DataLoggerBusinessException(String message) {
		super(message);
	}

	public DataLoggerBusinessException(String message, Object[] params) {
		super(message);
		this.params = params;
	}

	@Override
	public String getLocalizedMessage() {
		return MessageSourceUtil.getMessage(this.getMessage(), params);
	}
}
