/*
 GNU Lesser General Public License

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.implementation.model;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;

import org.sjwimmer.tacharting.chart.api.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.IndicatorParameter;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.chart.model.types.StrokeType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.controller.ChartController;
import org.sjwimmer.tacharting.implementation.util.ConverterUtils;
import org.sjwimmer.tacharting.implementation.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read and write parameters and settings of {@link ChartIndicator ChartIndicators} to and from XML files
 * Mostly used from the {@link BaseIndicatorBox} to init the indicators and from the {@link ChartController} to show
 * controls for the settings in gui and to store the settings in xml file.
 */
//TODO extract interface
public class BaseIndicatorParameterManager implements IndicatorParameterManager {

    private final Logger log = LoggerFactory.getLogger(BaseIndicatorParameterManager.class);
    private Document doc;
    private XPath xPath;
    private StreamResult result;
    private Transformer transformer;


    public BaseIndicatorParameterManager() {
        loadParametersFile();
    }

    /**
     * Load the property file (if there is any) and initialize the class variables
     */
    private void loadParametersFile() {
        try {
            File propertiesFile = new File(Parameter.USER_INDICATOR_PROPERTIES_FILE);
            if(!propertiesFile.exists()){
                throw new FileNotFoundException(
                        String.format("Properties file could not be found. Should be in %s",Parameter.USER_INDICATOR_PROPERTIES_FILE));
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(propertiesFile);
            doc.getDocumentElement().normalize();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            xPath = xPathfactory.newXPath();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            result = new StreamResult(propertiesFile);
        } catch (IOException e) {
            //TODO: Exception handling
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }

    }

    /**
     * Reads a parameter from the parameter file
     * <p/>
     * @param key key for property
     * @param paramName default string array if not found
     * @return the corresponding parameter as String value
     * @throws XPathExpressionException if there is no such entry in the xml file
     */
    @Override
    public String getParameter(String key, String paramName) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']/param[@name='%s']/text()",indicator,id,paramName);
        XPathExpression expr = xPath.compile(command);
        Node resultNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        String content = resultNode.getTextContent();
        return resultNode.getTextContent();
    }

    /**
     *
     * @param key the key colorOf the indicator
     * @return the category if found, DEFAULT else
     * @throws XPathExpressionException i
     */
    @Override
    public IndicatorCategory getCategory(String key) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = "@category";
        XPathExpression expr = xPath.compile(command);
        String categorie = (String) expr.evaluate(node, XPathConstants.STRING);
        if(categorie.equals(""))
            return IndicatorCategory.DEFAULT;
        return IndicatorCategory.valueOf(categorie);
    }

    //TODO overload those with extra int id for further color, shape and stroke params
    @Override
    public Shape getShapeOf(String key) throws XPathExpressionException {
        return getShapeOf(key,"Shape");
    }

    @Override
    public Shape getShapeOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']",name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String shape = paramNode.getTextContent();
        if (shape.equals(""))
            return null;
        return ShapeType.valueOf(shape.toUpperCase()).shape;
    }

    @Override
    public Paint getColorOf(String key) throws XPathExpressionException {
        return getColorOf(key,"Color");
    }

    @Override
    public Paint getColorOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']",name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String color = paramNode.getTextContent();
        return ConverterUtils.ColorAWTConverter.fromString(color);
    }

    /**
     * returns the main {@link Stroke} colorOf the indicator identified by key
     * @param key the identifier colorOf the indicator
     * @return Stroke object or null
     * @throws XPathExpressionException
     */
    @Override
    public Stroke getStrokeOf(String key) throws XPathExpressionException {
        return getStrokeOf(key, "Stroke");
    }

    @Override
    public Stroke getStrokeOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']", name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String stroke = paramNode.getTextContent();
        if(stroke.equals("")){
            return null;
        }
        return StrokeType.valueOf(stroke.toUpperCase()).stroke;
    }

    @Override
    public ChartType getChartType(String key) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = "./param[@name='Chart Type']";
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String chartType = paramNode.getTextContent();
        if(chartType.equals("")){
            return null;
        }
        return ChartType.valueOf(chartType);
    }

    @Override
    public void setParameter(String key, String paramName, String value) throws IOException, XPathExpressionException, TransformerException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']/param[@name='%s']",indicator,id,paramName);
        XPathExpression expr = xPath.compile(command);
        Node resultNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        //((Element) resultNode).getAttribute("type"); //TODO implement type check
        resultNode.setTextContent(value);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
    }

    /**
     * @return all indicator names for that are properties stored as a list
     */
    @Override
    public java.util.List<String> getAllKeys() {
        List<String> keyList = new ArrayList<String>();
        NodeList indicators = doc.getElementsByTagName("indicator");
        for (int i = 0; i < indicators.getLength(); i++){
            Node node = indicators.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE){
                String indicator= ((Element)node).getAttribute("identifier");
                NodeList instances = ((Element) node).getElementsByTagName("instance");
                for (int j=0; j<instances.getLength(); j++){
                    String id = ((Element)instances.item(j)).getAttribute("id");
                    keyList.add(indicator+"_"+id);
                }
            }
        }
        return keyList;
    }

    @Override
    public List<String> getKeysForCategory(IndicatorCategory category) throws XPathExpressionException {
        String command = String.format("//instance[@category='%s']",category.toString());
        XPathExpression expr = xPath.compile(command);
        DTMNodeList nodes = (DTMNodeList) expr.evaluate(doc, XPathConstants.NODESET);

        List<String> keyList = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++){
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE){
                String id = ((Element) node).getAttribute("id");
                Element parent =(Element) node.getParentNode();
                String name = parent.getAttribute("identifier");
                keyList.add(name+"_"+id);
            }
        }
        return keyList;
    }

    /**
     * Get all parameters for a
     * @param key identifier colorOf the indicator
     * @return a Map of name and value colorOf the parameter
     */
    @Override
    public List<IndicatorParameter> getParametersFor(String key) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']/*",indicator,id);
        XPathExpression expr = xPath.compile(command);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<IndicatorParameter> parameters = new ArrayList<>();
        for (int i = 0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            if(node.getNodeType()==Node.ELEMENT_NODE){
                Element paraElement = (Element) node;
                if(paraElement.getNodeName().equals("param")){
                    String name = paraElement.getAttribute("name");
                    String type = paraElement.getAttribute("type");
                    String value = paraElement.getTextContent();
                    parameters.add(new IndicatorParameter(name, FormatUtils.indicatorParameterTypeOf(type),value));
                }
            }
        }
        return parameters;
    }

    /**
     * Dublicates a node that represents an indicator instance
     * @param key The key of the Indicator in the xml file (ofFormat "name_id")
     * @return the key of the duplicated indicator
     * @throws XPathExpressionException xpath expression exception
     * @throws TransformerException transformer exception
     */
    @Override
    public String duplicate(String key) throws XPathExpressionException, TransformerException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        //get valid id (the biggest+1 ...)
        String command = String.format("//indicator[@identifier='%s']/instance",indicator);
        XPathExpression expr = xPath.compile(command);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        int nextID = 1;
        for(int i = 0; i < nodes.getLength(); i++){
            Element instance = (Element) nodes.item(i);
            int elementId = Integer.parseInt(instance.getAttribute("id"));
            if (nextID<=elementId){
                nextID = elementId;
            }
        }
        nextID++;

        // get instance, clone it and append to parent colorOf instance
        Node toDuplicate = getNodeForInstance(key);
        Element duplicate = (Element) toDuplicate.cloneNode(true);
        duplicate.setAttribute("id", String.valueOf(nextID));
        Node parent = toDuplicate.getParentNode();
        parent.appendChild(duplicate);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return indicator+"_"+nextID;
    }

    private Node getNodeForInstance(String key) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']",indicator,id);
        XPathExpression expr = xPath.compile(command);
        return (Node) expr.evaluate(doc, XPathConstants.NODE);
    }


    @Override
    public String getDescription(String key) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String command = String.format("//indicator[@identifier='%s']/description/text()",indicator);
        XPathExpression expr = xPath.compile(command);
        return (String) expr.evaluate(doc,XPathConstants.STRING);
    }

    @Override
    public String getParameterType(String key, String param) throws XPathExpressionException {
        Node instanceNode = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']/@type",param);
        XPathExpression expr = xPath.compile(command);
        return  (String) expr.evaluate(instanceNode,XPathConstants.STRING);
    }
}
