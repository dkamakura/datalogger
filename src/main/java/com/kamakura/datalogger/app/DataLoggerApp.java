/*
 * DataLoggerApp.java
 */

package com.kamakura.datalogger.app;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The main class of the application.
 */
public class DataLoggerApp extends SingleFrameApplication {
	Logger logger = Logger.getLogger(DataLoggerApp.class);

	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override
	protected void startup() {
		Locale.setDefault(new Locale("en"));
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:/spring/*.xml");
		show(applicationContext.getBean(DataLoggerView.class));
	}

	/**
	 * This method is to initialize the specified window by injecting resources.
	 * Windows shown in our application come fully initialized from the GUI
	 * builder, so this additional configuration is not needed.
	 */
	@Override
	protected void configureWindow(java.awt.Window root) {
	}

	/**
	 * A convenient static getter for the application instance.
	 * 
	 * @return the instance of DataLoggerApp
	 */
	public static DataLoggerApp getApplication() {
		return Application.getInstance(DataLoggerApp.class);
	}

	/**
	 * Main method launching the application.
	 */
	public static void main(String[] args) {
		launch(DataLoggerApp.class, args);
	}
}
