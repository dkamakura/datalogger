package com.kamakura.datalogger.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.springframework.stereotype.Component;

import com.kamakura.communication.i18n.MessageSourceUtil;
import com.kamakura.datalogger.model.DataLog;

@Component
public class ChartBuilder {
	private static final Integer MARGIN = 20;
	
    public ChartPanel buildChart(DataLog dataLog) {
    	TimeSeriesCollection result = new TimeSeriesCollection();   
    	TimeSeries dateSeries = new TimeSeries(MessageSourceUtil.getMessage("chart.label.temperature"));
    	for(Date date : dataLog.getSamples().keySet()) {
    		dateSeries.add(new TimeSeriesDataItem(new Minute(date), dataLog.getSamples().get(date).doubleValue()));   
    	}
    	result.addSeries(dateSeries);
    	
    	JFreeChart chart = ChartFactory.createXYLineChart(   
    			MessageSourceUtil.getMessage("chart.label.temperature.log"),   
    			MessageSourceUtil.getMessage("chart.label.date.time"),   
    			MessageSourceUtil.getMessage("chart.label.temperature"),    
    			result,
    			PlotOrientation.VERTICAL,   
    			false,    
    			false,    
    			false
    	);
    	
    	XYPlot plot = (XYPlot) chart.getPlot();   
    	plot.setDomainGridlinePaint(Color.white);   
    	plot.setDomainGridlineStroke(new BasicStroke(1.0f));   
    	plot.setRangeGridlinePaint(Color.lightGray);   
    	plot.setRangeGridlineStroke(new BasicStroke(1.0f));   
    	plot.setRangeTickBandPaint(new Color(240, 240, 240));   

    	// set axis margins to allow space for marker labels...   
    	PeriodAxis domainAxis = new PeriodAxis(null, new Hour(dataLog.getInitialReadTime()), new Hour(dataLog.getFinalReadTime()));   
    	PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];   
    	info[0] = new PeriodAxisLabelInfo(Minute.class, new SimpleDateFormat("HH:mm"));   
    	info[1] = new PeriodAxisLabelInfo(Day.class, new SimpleDateFormat("dd-MMM"));   
    	domainAxis.setLabelInfo(info);
    	
    	plot.setDomainAxis(domainAxis);   

    	ValueAxis rangeAxis = plot.getRangeAxis();
    	BigDecimal maxTemperature = dataLog.getAlarmMaxTemperature().compareTo(dataLog.getTopTemperature()) > 0 ? dataLog.getAlarmMaxTemperature() : dataLog.getTopTemperature(); 
    	BigDecimal minTemperature = dataLog.getAlarmMinTemperature().compareTo(dataLog.getBottomTemperature()) < 0 ? dataLog.getAlarmMinTemperature() : dataLog.getBottomTemperature(); 
    	rangeAxis.setRange(minTemperature.doubleValue() - MARGIN, maxTemperature.doubleValue() + MARGIN);   

    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();   
    	renderer.setBaseShapesVisible(true);   
    	renderer.setSeriesFillPaint(0, Color.white);   
    	renderer.setUseFillPaint(true);

    	Marker cooling = new IntervalMarker(dataLog.getAlarmMinTemperature().doubleValue(), dataLog.getAlarmMaxTemperature().doubleValue());   
    	cooling.setLabelOffsetType(LengthAdjustmentType.EXPAND);   
    	cooling.setPaint(new Color(150, 150, 255));   
    	plot.addRangeMarker(cooling, Layer.BACKGROUND);   
    	
    	Marker minTemp = new ValueMarker(dataLog.getAlarmMinTemperature().doubleValue(), Color.blue,    
    	        new BasicStroke(2.0f));
    	minTemp.setLabel(MessageSourceUtil.getMessage("chart.label.minimum.temperature"));   
    	minTemp.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));   
    	minTemp.setLabelPaint(Color.BLUE);   
    	minTemp.setLabelAnchor(RectangleAnchor.TOP_LEFT);   
    	minTemp.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);   
    	
    	Marker maxTemp = new ValueMarker(dataLog.getAlarmMaxTemperature().doubleValue(), Color.blue,    
    	        new BasicStroke(2.0f));
    	maxTemp.setLabel(MessageSourceUtil.getMessage("chart.label.maximum.temperature"));   
    	maxTemp.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));   
    	maxTemp.setLabelPaint(Color.BLUE);   
    	maxTemp.setLabelAnchor(RectangleAnchor.TOP_LEFT);   
    	maxTemp.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);   
    	
    	plot.addRangeMarker(minTemp, Layer.BACKGROUND);   
    	plot.addRangeMarker(maxTemp, Layer.BACKGROUND);    

    	return new ChartPanel(chart);
    }
}
