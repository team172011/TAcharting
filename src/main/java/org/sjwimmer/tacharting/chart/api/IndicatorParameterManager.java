package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.IndicatorParameter;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IndicatorParameterManager {
    /**
     * Reads a parameter from the parameter file
     * <p/>
     * @param key key for property
     * @param paramName default string array if not found
     * @return the corresponding parameter as String value
     * @throws XPathExpressionException if there is no such entry in the xml file
     */
    String getParameter(IndicatorKey key, String paramName) throws XPathExpressionException;

    /**
     *
     * @param key the key colorOf the indicator
     * @return the category if found, DEFAULT else
     * @throws XPathExpressionException i
     */
    IndicatorCategory getCategory(IndicatorKey key) throws XPathExpressionException;

    //TODO overload those with extra int id for further color, shape and stroke params
    Shape getShapeOf(IndicatorKey key) throws XPathExpressionException;

    Shape getShapeOf(IndicatorKey key, String name) throws XPathExpressionException;

    Paint getColorOf(IndicatorKey key) throws XPathExpressionException;

    Paint getColorOf(IndicatorKey key, String name) throws XPathExpressionException;

    /**
     * returns the main {@link Stroke} colorOf the indicator identified by key
     * @param key the identifier colorOf the indicator
     * @return Stroke object or null
     * @throws XPathExpressionException
     */
    Stroke getStrokeOf(IndicatorKey key) throws XPathExpressionException;

    Stroke getStrokeOf(IndicatorKey key, String name) throws XPathExpressionException;

    ChartType getChartType(IndicatorKey key) throws XPathExpressionException;

    void setParameter(IndicatorKey key, String paramName, String value) throws IOException, XPathExpressionException, TransformerException;

    /**
     * @return all indicator names for that are properties stored as a list
     */
    java.util.List<IndicatorKey> getAllKeys();

    List<IndicatorKey> getKeysForCategory(IndicatorCategory category) throws XPathExpressionException;

    /**
     * Get all parameters for a
     * @param key identifier colorOf the indicator
     * @return a Map colorOf name and value colorOf the parameter
     */
    Map<String,IndicatorParameter> getParametersFor(IndicatorKey key) throws XPathExpressionException;

    /**
     * Dublicates a node that represents an indicator instance
     * @param key The key of the Indicator in the xml file (ofFormat "name_id")
     * @return the key of the duplicated indicator
     * @throws XPathExpressionException xpath expression exception
     * @throws TransformerException transformer exception
     */
    String duplicate(IndicatorKey key) throws XPathExpressionException, TransformerException;

    String getDescription(IndicatorKey key) throws XPathExpressionException;

    String getParameterType(IndicatorKey key, String param) throws XPathExpressionException;
}
