/*
 * DataLoggerView.java
 */

package com.kamakura.datalogger.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.kamakura.communication.config.SerialPortConfiguration;
import com.kamakura.datalogger.business.DataLoggerBusiness;
import com.kamakura.datalogger.exception.DataLoggerException;
import com.kamakura.datalogger.model.DataLog;
import com.kamakura.datalogger.model.DataLoggerConfiguration;
import com.kamakura.datalogger.util.ChartBuilder;

/**
 * The application's main frame.
 */
@Controller
public class DataLoggerView extends FrameView {

    private ResourceMap resourceMap; 
    private javax.swing.JLabel averageTemperatureLabel;
    private javax.swing.JTextField averageTemperatureTextField;
    private javax.swing.JButton configButton;
    private javax.swing.JPanel configPanel;
    private javax.swing.JLabel configSerialNumberLabel;
    private javax.swing.JFormattedTextField configSerialNumberTextField;
    private javax.swing.JLabel configSampleIntervalLabel;
    private javax.swing.JFormattedTextField configSampleIntervalTextField;
    private javax.swing.JLabel configAlarmMaxTemperatureLabel;
    private javax.swing.JFormattedTextField configAlarmMaxTemperatureTextField;
    private javax.swing.JLabel configAlarmMinTemperatureLabel;
    private javax.swing.JFormattedTextField configAlarmMinTemperatureTextField;
    private javax.swing.JLabel configCalibrationTemperatureLabel;
    private javax.swing.JFormattedTextField configCalibrationTemperatureTextField;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JSplitPane dataSplitPane;
    private javax.swing.JPanel dataTabPanel;
    private javax.swing.JLabel finalReadTimeLabel;
    private javax.swing.JTextField finalReadTimeTextField;
    private javax.swing.JTabbedPane functionsTabbedPane;
    private javax.swing.JScrollPane chartTabPanel;
    private org.jfree.chart.ChartPanel chartPanel;
    private javax.swing.JScrollPane chartScrollPane;
    private javax.swing.JLabel initialReadTimeLabel;
    private javax.swing.JTextField initialReadTimeTextField;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel alarmMaxTemperatureLabel;
    private javax.swing.JTextField alarmMaxTemperatureTextField;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel alarmMinTemperatureLabel;
    private javax.swing.JTextField alarmMinTemperatureTextField;
    private javax.swing.JComboBox portComboBox;
    private javax.swing.JLabel applicationMessageLabel;
    private javax.swing.JPanel portPanel;
    private javax.swing.JButton printButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton readDataButton;
    private javax.swing.JLabel selectPortLabel;
    private javax.swing.JLabel serialNumberLabel;
    private javax.swing.JTextField serialNumberTextField;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel standardDeviationLabel;
    private javax.swing.JTextField standardDeviationTextField;
    private javax.swing.JLabel timeAboveMaxTemperatureLabel;
    private javax.swing.JTextField timeAboveMaxTemperatureTextField;
    private javax.swing.JLabel timeUnderMinTemperatureLabel;
    private javax.swing.JTextField timeUnderMinTemperatureTextField;

    private Timer messageTimer;
    private Timer busyIconTimer;
    private Icon idleIcon;
    private Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    
    private Timer applicationMessageTimer;

    private JDialog aboutBox;
    
    @Autowired
    private DataLoggerBusiness dataLoggerBusiness;
    
    @Autowired
    private SerialPortConfiguration serialPortConfiguration;

    @Autowired
    private ChartBuilder chartBuilder;

    private static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm") {
        private static final long serialVersionUID = -1065659425735047652L;
	{
    	setLenient(false);
    }};

    private static final NumberFormat temperatureFormatter = new DecimalFormat("#0.0") {
        private static final long serialVersionUID = 6336985835001301033L;
	{
    	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    	decimalFormatSymbols.setDecimalSeparator('.');
    	setDecimalFormatSymbols(decimalFormatSymbols);
		setMaximumFractionDigits(1);
		setMaximumIntegerDigits(2);
    	
    }};
    

    private static NumberFormat serialNumberFormatter = new DecimalFormat("##############0") {
		private static final long serialVersionUID = 1568292922173869276L;
	{
        setMaximumFractionDigits(0);
        setMaximumIntegerDigits(15);
    }};

    private static NumberFormat sampleIntervalFormatter = new DecimalFormat("####0") {
		private static final long serialVersionUID = 7788525257956762L;
	{
		setMaximumFractionDigits(0);
		setMaximumIntegerDigits(5);
    }};
    
    public DataLoggerView() {
    	super(Application.getInstance());
    }

    @PostConstruct
    public void startup() {
        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        
        int applicationMessageTimeout = resourceMap.getInteger("applicationMessageLabel.messageTimeout");
        applicationMessageTimer = new Timer(applicationMessageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applicationMessageLabel.setText("");
            }
        });
        applicationMessageTimer.setRepeats(false);

    }
    
    private void showMessage(String message) {
    	applicationMessageLabel.setText(message);
    	applicationMessageTimer.start();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        portPanel = new javax.swing.JPanel();
        selectPortLabel = new javax.swing.JLabel();
        portComboBox = new javax.swing.JComboBox();
        applicationMessageLabel = new javax.swing.JLabel();
        functionsTabbedPane = new javax.swing.JTabbedPane();
        dataPanel = new javax.swing.JPanel();
        dataSplitPane = new javax.swing.JSplitPane();
        dataTabPanel = new javax.swing.JPanel();
        readDataButton = new javax.swing.JButton();
        serialNumberLabel = new javax.swing.JLabel();
        alarmMinTemperatureLabel = new javax.swing.JLabel();
        alarmMaxTemperatureLabel = new javax.swing.JLabel();
        initialReadTimeLabel = new javax.swing.JLabel();
        serialNumberTextField = new javax.swing.JTextField();
        alarmMinTemperatureTextField = new javax.swing.JTextField();
        alarmMaxTemperatureTextField = new javax.swing.JTextField();
        initialReadTimeTextField = new javax.swing.JTextField();
        printButton = new javax.swing.JButton();
        finalReadTimeLabel = new javax.swing.JLabel();
        averageTemperatureLabel = new javax.swing.JLabel();
        standardDeviationLabel = new javax.swing.JLabel();
        timeUnderMinTemperatureLabel = new javax.swing.JLabel();
        timeAboveMaxTemperatureLabel = new javax.swing.JLabel();
        finalReadTimeTextField = new javax.swing.JTextField();
        averageTemperatureTextField = new javax.swing.JTextField();
        standardDeviationTextField = new javax.swing.JTextField();
        timeUnderMinTemperatureTextField = new javax.swing.JTextField();
        timeAboveMaxTemperatureTextField = new javax.swing.JTextField();
        chartTabPanel = new javax.swing.JScrollPane();
        configPanel = new javax.swing.JPanel();
        configSerialNumberLabel = new javax.swing.JLabel();
        configSampleIntervalLabel = new javax.swing.JLabel();
        configAlarmMinTemperatureLabel = new javax.swing.JLabel();
        configAlarmMaxTemperatureLabel = new javax.swing.JLabel();
        configCalibrationTemperatureLabel = new javax.swing.JLabel();
        configSerialNumberTextField = new javax.swing.JFormattedTextField(serialNumberFormatter);
        configSampleIntervalTextField = new javax.swing.JFormattedTextField(sampleIntervalFormatter);
        configAlarmMinTemperatureTextField = new javax.swing.JFormattedTextField(temperatureFormatter);
        configAlarmMaxTemperatureTextField = new javax.swing.JFormattedTextField(temperatureFormatter);
        configCalibrationTemperatureTextField = new javax.swing.JFormattedTextField(temperatureFormatter);
        configButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.kamakura.datalogger.app.DataLoggerApp.class).getContext().getActionMap(DataLoggerView.class, this);

        mainPanel.setName("mainPanel");

        portPanel.setBorder(null);
        portPanel.setName("portPanel");

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.kamakura.datalogger.app.DataLoggerApp.class).getContext().getResourceMap(DataLoggerView.class);
        selectPortLabel.setText(resourceMap.getString("selectPortLabel.text"));
        selectPortLabel.setName("selectPortLabel");

        List<String> availableSerialPorts = serialPortConfiguration.listAvailableSerialPorts();
        if(availableSerialPorts.isEmpty()) {
        	availableSerialPorts.add(resourceMap.getString("selectPort.no.serialport.available.text"));
        }
        
        portComboBox.setModel(new javax.swing.DefaultComboBoxModel(availableSerialPorts.toArray(new String[0])));
        portComboBox.setAction(actionMap.get("configurePort"));
        portComboBox.setName("portComboBox");

        applicationMessageLabel.setName("applicationMessageLabel");
        applicationMessageLabel.setFont(new Font(applicationMessageLabel.getFont().getName(), Font.BOLD, applicationMessageLabel.getFont().getSize() + 3));
        applicationMessageLabel.setForeground(Color.red);
        applicationMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout portPanelLayout = new javax.swing.GroupLayout(portPanel);
        portPanel.setLayout(portPanelLayout);
        portPanelLayout.setHorizontalGroup(
            portPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(portPanelLayout.createSequentialGroup()
                .addComponent(selectPortLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applicationMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE))
        );
        portPanelLayout.setVerticalGroup(
            portPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(portPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(portPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectPortLabel)
                    .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applicationMessageLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        functionsTabbedPane.setName("functionsTabbedPane");

        dataPanel.setName("dataPanel");

        dataSplitPane.setEnabled(false);
        dataSplitPane.setName("dataSplitPane");

        dataTabPanel.setName("dataTabPanel");

        readDataButton.setText(resourceMap.getString("readDataButton.text"));
        readDataButton.setName("readDataButton");
        readDataButton.setAction(actionMap.get("readData"));


        serialNumberLabel.setText(resourceMap.getString("serialNumberDataLabel.text"));
        serialNumberLabel.setName("serialNumberDataLabel");

        alarmMinTemperatureLabel.setText(resourceMap.getString("minTemperatureLabel.text"));
        alarmMinTemperatureLabel.setName("minTemperatureLabel");

        alarmMaxTemperatureLabel.setText(resourceMap.getString("maxTemperatureLabel.text"));
        alarmMaxTemperatureLabel.setName("maxTemperatureLabel");

        initialReadTimeLabel.setText(resourceMap.getString("initialTimeLabel.text"));
        initialReadTimeLabel.setName("initialTimeLabel");

        serialNumberTextField.setEditable(false);
        serialNumberTextField.setText(resourceMap.getString("serialNumberDataTextField.text"));
        serialNumberTextField.setName("serialNumberDataTextField");

        alarmMinTemperatureTextField.setEditable(false);
        alarmMinTemperatureTextField.setText(resourceMap.getString("minTemperatureTextField.text"));
        alarmMinTemperatureTextField.setName("minTemperatureTextField");

        alarmMaxTemperatureTextField.setEditable(false);
        alarmMaxTemperatureTextField.setText(resourceMap.getString("maxTemperatureTextField.text"));
        alarmMaxTemperatureTextField.setName("maxTemperatureTextField");

        initialReadTimeTextField.setEditable(false);
        initialReadTimeTextField.setText(resourceMap.getString("initialTimeTextField.text"));
        initialReadTimeTextField.setName("initialTimeTextField");

        printButton.setText(resourceMap.getString("printButton.text"));
        printButton.setName("printButton");
        printButton.setAction(actionMap.get("printData"));

        finalReadTimeLabel.setText(resourceMap.getString("finalTimeLabel.text"));
        finalReadTimeLabel.setName("finalTimeLabel");

        averageTemperatureLabel.setText(resourceMap.getString("averageTemperatureLabel.text"));
        averageTemperatureLabel.setName("averageTemperatureLabel");

        standardDeviationLabel.setText(resourceMap.getString("stdDeviationLabel.text"));
        standardDeviationLabel.setName("stdDeviationLabel");

        timeUnderMinTemperatureLabel.setText(resourceMap.getString("timeUnderMinTemperatureLabel.text"));
        timeUnderMinTemperatureLabel.setName("timeUnderMinTemperatureLabel");

        timeAboveMaxTemperatureLabel.setText(resourceMap.getString("timeAboveMaxTemperatureLabel.text"));
        timeAboveMaxTemperatureLabel.setName("timeAboveMaxTemperatureLabel");

        finalReadTimeTextField.setEditable(false);
        finalReadTimeTextField.setText(resourceMap.getString("finalTimeTextField.text"));
        finalReadTimeTextField.setName("finalTimeTextField");

        averageTemperatureTextField.setEditable(false);
        averageTemperatureTextField.setText(resourceMap.getString("averageTemperatureTextField.text"));
        averageTemperatureTextField.setName("averageTemperatureTextField");

        standardDeviationTextField.setEditable(false);
        standardDeviationTextField.setText(resourceMap.getString("stdDeviationTextField.text"));
        standardDeviationTextField.setName("stdDeviationTextField");

        timeUnderMinTemperatureTextField.setEditable(false);
        timeUnderMinTemperatureTextField.setText(resourceMap.getString("timeUnderMinTemperatureTextField.text"));
        timeUnderMinTemperatureTextField.setName("timeUnderMinTemperatureTextField");

        timeAboveMaxTemperatureTextField.setEditable(false);
        timeAboveMaxTemperatureTextField.setText(resourceMap.getString("timeAboveMaxTemperatureTextField.text"));
        timeAboveMaxTemperatureTextField.setName("timeAboveMaxTemperatureTextField");

        javax.swing.GroupLayout dataTabPanelLayout = new javax.swing.GroupLayout(dataTabPanel);
        dataTabPanel.setLayout(dataTabPanelLayout);
        dataTabPanelLayout.setHorizontalGroup(
            dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(readDataButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printButton))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(serialNumberLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serialNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(alarmMinTemperatureLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alarmMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(alarmMaxTemperatureLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alarmMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(initialReadTimeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initialReadTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(finalReadTimeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(finalReadTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(averageTemperatureLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(averageTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addComponent(standardDeviationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(standardDeviationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dataTabPanelLayout.createSequentialGroup()
                        .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeUnderMinTemperatureLabel)
                            .addComponent(timeAboveMaxTemperatureLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeAboveMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeUnderMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dataTabPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {averageTemperatureLabel, finalReadTimeLabel, initialReadTimeLabel, alarmMaxTemperatureLabel, alarmMinTemperatureLabel, serialNumberLabel, standardDeviationLabel, timeAboveMaxTemperatureLabel, timeUnderMinTemperatureLabel});

        dataTabPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {averageTemperatureTextField, finalReadTimeTextField, initialReadTimeTextField, alarmMaxTemperatureTextField, alarmMinTemperatureTextField, serialNumberTextField, standardDeviationTextField, timeAboveMaxTemperatureTextField, timeUnderMinTemperatureTextField});

        dataTabPanelLayout.setVerticalGroup(
            dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataTabPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(readDataButton)
                    .addComponent(printButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serialNumberLabel)
                    .addComponent(serialNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alarmMinTemperatureLabel)
                    .addComponent(alarmMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alarmMaxTemperatureLabel)
                    .addComponent(alarmMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialReadTimeLabel)
                    .addComponent(initialReadTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finalReadTimeLabel)
                    .addComponent(finalReadTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(averageTemperatureLabel)
                    .addComponent(averageTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(standardDeviationLabel)
                    .addComponent(standardDeviationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeUnderMinTemperatureLabel)
                    .addComponent(timeUnderMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeAboveMaxTemperatureLabel)
                    .addComponent(timeAboveMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        dataSplitPane.setLeftComponent(dataTabPanel);

        chartTabPanel.setName("chartTabPanel");
        chartTabPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        chartTabPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        dataSplitPane.setRightComponent(chartTabPanel);

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 777, Short.MAX_VALUE)
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dataSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
        );

        functionsTabbedPane.addTab(resourceMap.getString("dataPanel.TabConstraints.tabTitle"), dataPanel);

        configPanel.setName("configPanel");

        configSerialNumberLabel.setText(resourceMap.getString("serialNumberConfigLabel.text"));
        configSerialNumberLabel.setName("serialNumberConfigLabel");

        configSampleIntervalLabel.setText(resourceMap.getString("sampleIntervalLabel.text"));
        configSampleIntervalLabel.setName("sampleIntervalLabel");

        configAlarmMinTemperatureLabel.setText(resourceMap.getString("alarmMinTemperatureLabel.text"));
        configAlarmMinTemperatureLabel.setName("alarmMinTemperatureLabel");

        configAlarmMaxTemperatureLabel.setText(resourceMap.getString("alarmMaxTemperatureLabel.text"));
        configAlarmMaxTemperatureLabel.setName("alarmMaxTemperatureLabel");

        configCalibrationTemperatureLabel.setText(resourceMap.getString("calibrationTemperatureLabel.text"));
        configCalibrationTemperatureLabel.setName("calibrationTemperatureLabel");

        configSerialNumberTextField.setText(resourceMap.getString("serialNumberConfigTextField.text"));
        configSerialNumberTextField.setName("serialNumberConfigTextField");

        configSampleIntervalTextField.setText(resourceMap.getString("sampleIntervalTextField.text"));
        configSampleIntervalTextField.setName("sampleIntervalTextField");

        configAlarmMinTemperatureTextField.setText(resourceMap.getString("alarmMinTemperatureTextField.text"));
        configAlarmMinTemperatureTextField.setName("alarmMinTemperatureTextField");

        configAlarmMaxTemperatureTextField.setText(resourceMap.getString("alarmMaxTemperatureTextField.text"));
        configAlarmMaxTemperatureTextField.setName("alarmMaxTemperatureTextField");

        configCalibrationTemperatureTextField.setText(resourceMap.getString("calibrationTemperatureTextField.text"));
        configCalibrationTemperatureTextField.setName("calibrationTemperatureTextField");

        configButton.setText(resourceMap.getString("configButton.text"));
        configButton.setName("configButton");
        configButton.setAction(actionMap.get("configureDataLogger"));

        
        javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configButton)
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configSerialNumberLabel)
                            .addComponent(configCalibrationTemperatureLabel)
                            .addComponent(configSampleIntervalLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(configCalibrationTemperatureTextField)
                            .addComponent(configSerialNumberTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(configSampleIntervalTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(configPanelLayout.createSequentialGroup()
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configAlarmMinTemperatureLabel)
                            .addComponent(configAlarmMaxTemperatureLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configAlarmMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(configAlarmMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(472, Short.MAX_VALUE))
        );

        configPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {configAlarmMaxTemperatureLabel, configAlarmMinTemperatureLabel, configSampleIntervalLabel, configSerialNumberLabel});

        configPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {configAlarmMaxTemperatureTextField, configAlarmMinTemperatureTextField, configSampleIntervalTextField, configSerialNumberTextField});

        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configSerialNumberLabel)
                    .addComponent(configSerialNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configCalibrationTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configCalibrationTemperatureLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configSampleIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configSampleIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configAlarmMinTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configAlarmMinTemperatureLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configAlarmMaxTemperatureTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configAlarmMaxTemperatureLabel))
                .addGap(34, 34, 34)
                .addComponent(configButton)
                .addContainerGap(147, Short.MAX_VALUE))
        );
        
        functionsTabbedPane.addTab(resourceMap.getString("configPanel.TabConstraints.tabTitle"), configPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(functionsTabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE)
                    .addComponent(portPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(portPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(functionsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuBar.setName("menuBar");

        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");
        fileMenu.setMnemonic(resourceMap.getString("fileMenu.mnemonic").charAt(0));

        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text"));
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText"));
        exitMenuItem.setName("exitMenuItem");
        exitMenuItem.setAccelerator(null);
        exitMenuItem.setMnemonic(resourceMap.getString("exitMenuItem.mnemonic").charAt(0));
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon"));

        
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");
        helpMenu.setMnemonic(resourceMap.getString("helpMenu.mnemonic").charAt(0));

        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text"));
        aboutMenuItem.setName("aboutMenuItem");
        aboutMenuItem.setMnemonic(resourceMap.getString("aboutMenuItem.mnemonic").charAt(0));
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel");

        statusPanelSeparator.setName("statusPanelSeparator");

        statusMessageLabel.setName("statusMessageLabel");

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel");

        progressBar.setName("progressBar");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 623, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }
    
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = DataLoggerApp.getApplication().getMainFrame();
            aboutBox = new DataLoggerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        DataLoggerApp.getApplication().show(aboutBox);
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> readData() {
        return new ReadDataTask(getApplication());
    }

    @Action
    public void configurePort() {
    	serialPortConfiguration.setPortName(this.portComboBox.getSelectedItem().toString());
    }
    
    private class ReadDataTask extends org.jdesktop.application.Task<Object, Void> {
    	ReadDataTask(org.jdesktop.application.Application app) {
            super(app);
        }
        @Override protected Object doInBackground() {
        	this.clearData();
            return dataLoggerBusiness.readDataLog();
        }
        @Override protected void succeeded(Object result) {
        	DataLog dataLog = (DataLog)result;
        	this.showData(dataLog);
        	chartPanel = chartBuilder.buildChart(dataLog);
        	
        	chartScrollPane = new JScrollPane(chartPanel);
        	chartScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        	chartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        	
        	dataSplitPane.setRightComponent(chartScrollPane);
        }
        @Override protected void failed(Throwable cause) {
        	showMessage(cause.getLocalizedMessage());
        }

        private void clearData() {
        	serialNumberTextField.setText("");
        	alarmMinTemperatureTextField.setText("");
        	alarmMaxTemperatureTextField.setText("");
        	initialReadTimeTextField.setText("");
        	finalReadTimeTextField.setText("");
        	averageTemperatureTextField.setText("");
        	standardDeviationTextField.setText("");
        	timeUnderMinTemperatureTextField.setText("");
        	timeAboveMaxTemperatureTextField.setText("");
        	dataSplitPane.setRightComponent(chartTabPanel);
        }

        private void showData(DataLog dataLog) {
        	serialNumberTextField.setText(serialNumberFormatter.format(dataLog.getSerialNumber()));
        	alarmMinTemperatureTextField.setText(temperatureFormatter.format(dataLog.getAlarmMinTemperature()));
        	alarmMaxTemperatureTextField.setText(temperatureFormatter.format(dataLog.getAlarmMaxTemperature()));
        	initialReadTimeTextField.setText(dateFormatter.format(dataLog.getInitialReadTime()));
        	finalReadTimeTextField.setText(dateFormatter.format(dataLog.getFinalReadTime()));
        	averageTemperatureTextField.setText(temperatureFormatter.format(dataLog.getAverageTemperature()));
        	standardDeviationTextField.setText(temperatureFormatter.format(dataLog.getStandardDeviation()));
        	timeUnderMinTemperatureTextField.setText(dataLog.getTimeUnderMinTemperature().toString());
        	timeAboveMaxTemperatureTextField.setText(dataLog.getTimeAboveMaxTemperature().toString());
        }
    }
    
    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> configureDataLogger() {
        return new ConfigureDataLoggerTask(getApplication());
    }

    private class ConfigureDataLoggerTask extends org.jdesktop.application.Task<Object, Void> {
    	ConfigureDataLoggerTask(org.jdesktop.application.Application app) {
            super(app);
        }
        @Override protected Object doInBackground() {
	    	DataLoggerConfiguration dataLoggerConfiguration = new DataLoggerConfiguration();
        	try {
		    	dataLoggerConfiguration.setSerialNumber(new Long(configSerialNumberTextField.getText()));
	        	dataLoggerConfiguration.setCalibrationTemperature(new BigDecimal(temperatureFormatter.parse(configCalibrationTemperatureTextField.getText()).doubleValue()));
		    	dataLoggerConfiguration.setSampleInterval(new Integer(configSampleIntervalTextField.getText()));
	        	dataLoggerConfiguration.setAlarmMinTemperature(new BigDecimal(temperatureFormatter.parse(configAlarmMinTemperatureTextField.getText()).doubleValue()));
	        	dataLoggerConfiguration.setAlarmMaxTemperature(new BigDecimal(temperatureFormatter.parse(configAlarmMaxTemperatureTextField.getText()).doubleValue()));
        	} catch (Exception e) {
        		throw new DataLoggerException("error.invalid.configuration");
			}
        	
        	dataLoggerBusiness.writeConfiguration(dataLoggerConfiguration);
        	
            return null;
        }
        
        @Override protected void succeeded(Object result) {
        	showMessage(resourceMap.getString("applicationMessageLabel.device.configured"));
        	clearConfiguration();
        }
        @Override protected void failed(Throwable cause) {
        	showMessage(cause.getLocalizedMessage());
        }
        
        private void clearConfiguration() {
        	configSerialNumberTextField.setText("");
        	configSampleIntervalTextField.setText("");
        	configAlarmMinTemperatureTextField.setText("");
        	configAlarmMaxTemperatureTextField.setText("");
        }
    }

    @Action(block = Task.BlockingScope.NONE)
    public Task<Object, Void> printData() {
        return new PrintDataTask(getApplication());
    }

    private class PrintDataTask extends org.jdesktop.application.Task<Object, Void> {
    	PrintDataTask(org.jdesktop.application.Application app) {
            super(app);
        }
        @Override protected Object doInBackground() {
        	PrinterJob job = PrinterJob.getPrinterJob();
        	PageFormat pf = job.defaultPage();
        	PageFormat pf2 = job.pageDialog(pf);
        	if (pf2 != pf) {
        		job.setPrintable(chartPanel, pf2);
        		if (job.printDialog()) {
        			try {
        				job.print();
        			} catch (PrinterException e) {
        				throw new DataLoggerException("error.printing.chart", e);
        			}
        		}
        	}
        	
            return null;
        }
        
        @Override protected void succeeded(Object result) {
        	showMessage(resourceMap.getString("applicationMessageLabel.chart.printed"));
        }
        @Override protected void failed(Throwable cause) {
        	showMessage(cause.getLocalizedMessage());
        }
    }
}
