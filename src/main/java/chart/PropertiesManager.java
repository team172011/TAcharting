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

package chart;

import chart.parameters.IndicatorParameters.IndicatorCategory;
import chart.parameters.IndicatorParameters.TaChartType;
import chart.parameters.IndicatorParameters.TaShape;
import chart.parameters.IndicatorParameters.TaStroke;
import chart.parameters.Parameter;
import chart.utils.FormatUtils;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to read and write parameters and settings of {@link ChartIndicator ChartIndicators} to and from XML files
 * Mostly used from the {@link ChartIndicatorBox} to init the indicators and from the {@link RootController} to show
 * controls for the settings and to store the settings.
 */
//TODO extract interface
public class PropertiesManager {

    private final Logger log = LoggerFactory.getLogger(PropertiesManager.class);
    private Document doc;
    private XPath xPath;
    private StreamResult result;
    private Transformer transformer;


    public PropertiesManager() {
        loadParametersFile();
    }

    /**
     * Load the property file (if there is any) and initialize the class variables
     */
    private void loadParametersFile() {
        try {
            URL fileURL = getClass().getClassLoader().getResource(Parameter.INDICATOR_PROPERTIES_FILE);

            File propertiesFile;
            if (fileURL == null) {
                log.info("No parameters file found");
                propertiesFile = new File(Parameter.INDICATOR_PROPERTIES_FILE);
                if(propertiesFile.createNewFile()){
                    log.info("New parameters file created {}", propertiesFile.getPath());
                }
            } else {
                propertiesFile = new File(fileURL.getFile());
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
    public String getParameter(String key, String paramName) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']/param[@name='%s']",indicator,id,paramName);
        XPathExpression expr = xPath.compile(command);
        Node resultNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        return resultNode.getTextContent();
    }

    /**
     *
     * @param key the key colorOf the indicator
     * @return the category if found, DEFAULT else
     * @throws XPathExpressionException i
     */
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
    public Shape getShapeOf(String key) throws XPathExpressionException {
        return getShapeOf(key,"Shape");
    }

    public Shape getShapeOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']",name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String shape = paramNode.getTextContent();
        if (shape.equals(""))
            return null;
        return TaShape.valueOf(shape).getShape();
    }

    public Paint getColorOf(String key) throws XPathExpressionException {
        return getColorOf(key,"Color");
    }

    public Paint getColorOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']",name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String color = paramNode.getTextContent();
        return FormatUtils.colorOf(color);
    }

    /**
     * returns the main {@link Stroke} colorOf the indicator identified by key
     * @param key the identifier colorOf the indicator
     * @return Stroke object or null
     * @throws XPathExpressionException
     */
    public Stroke getStrokeOf(String key) throws XPathExpressionException {
        return getStrokeOf(key, "Stroke");
    }

    public Stroke getStrokeOf(String key, String name) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']", name);
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String stroke = paramNode.getTextContent();
        if(stroke.equals("")){
            return null;
        }
        return TaStroke.valueOf(stroke).getStroke();
    }

    public TaChartType getChartType(String key) throws XPathExpressionException {
        Node node = getNodeForInstance(key);
        String command = "./param[@name='Chart Type']";
        XPathExpression expr = xPath.compile(command);
        Node paramNode = (Node) expr.evaluate(node,XPathConstants.NODE);
        String chartType = paramNode.getTextContent();
        if(chartType.equals("")){
            return null;
        }
        return TaChartType.valueOf(chartType);
    }

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
     * @return a Map colorOf name and value colorOf the parameter
     */
    public Map<String,String> getParametersFor(String key) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String id = raw[1];
        String command = String.format("//indicator[@identifier='%s']/instance[@id='%s']/*",indicator,id);
        XPathExpression expr = xPath.compile(command);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        Map<String,String> mapNameValue = new HashMap<>();
        for (int i = 0; i<nodes.getLength(); i++){
            Node node = nodes.item(i);
            if(node.getNodeType()==Node.ELEMENT_NODE){
                Element paraElement = (Element) node;
                if(paraElement.getNodeName().equals("param")){
                    String name = paraElement.getAttribute("name");
                    String value = paraElement.getTextContent();
                    mapNameValue.put(name,value);
                }

            }
        }
        return mapNameValue;
    }

    /**
     * @param key The key colorOf the Indicator in the xml file (ofFormat "name_id")
     * @return the key colorOf the duplicated indicator
     * @throws XPathExpressionException xpath expression exception
     * @throws TransformerException transformer exception
     */
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


    public String getDescription(String key) throws XPathExpressionException {
        String raw[] = key.split("_");
        String indicator = raw[0];
        String command = String.format("//indicator[@identifier='%s']/description/text()",indicator);
        XPathExpression expr = xPath.compile(command);
        return (String) expr.evaluate(doc,XPathConstants.STRING);
    }

    public String getParameterType(String key, String param) throws XPathExpressionException {
        Node instanceNode = getNodeForInstance(key);
        String command = String.format("./param[@name='%s']/@type",param);
        XPathExpression expr = xPath.compile(command);
        return  (String) expr.evaluate(instanceNode,XPathConstants.STRING);
    }
}
