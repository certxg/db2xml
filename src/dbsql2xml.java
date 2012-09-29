/*
* The "dbsql2xml" is Java tool (class) for transforming 
* (export) relational databases into hierarchical XML.
*
* Copyright (C) 2005–2006 Stepan Rybar
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
* For more information, see http://sourceforge.net/projects/dbsql2xml/
*
*     Stepan RYBAR
*     Modra 6
*     Stodulky 1979
*     Praha
*     the Czech Republic
*
*     xrybs01@seznam.cz
*     http://sourceforge.net/projects/dbsql2xml/
*/
//package net.sf.dbsql2xml;

import java.io.*;
import java.sql.*;
import java.util.*;

//packages for general XML manipulation
import javax.xml.parsers.*;
import org.w3c.dom.*;

//packages for XPath
import javax.xml.xpath.*;

//packages for saving Document
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//packages for XSD validation
import javax.xml.validation.*;
import javax.xml.XMLConstants;


// certxg
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/*
ToDo
*) XSLT transformation within class (on server)
*) add support for CLOB and BLOB ResultSet datatypes (because of XML, WKT and WKB formats)

*) close connection and file on exit and exception? or by gc?
*/

/**
<code>dbsql2xml</code>
<br />
"dbsql2xml" is Java tool (class) for transformation (export, convert) relational database 
into hierarchical XML. It requires JRE 5.0, JDBC and SQL DBMS. "dbsql2xml" uses XML document 
for mapping database tables and columns into elements of tree XML.
<br />
For more information see "https://sourceforge.net/projects/dbsql2xml/".

@version 0.21 2006-01-08
*/

public class dbsql2xml {
  private String applicationInfo;
  //fields from constructor
  private String pathToMappingXmlFile;
  private String pathToXsdFile;
  private String namesOfGlobalVariables;
  private String valuesOfGlobalVariables;
  private String pathToOutputXmlFile;
  //fields to be global visible
  private HashMap<String,String> globalVariables = new HashMap<String,String>();
  private Connection connection;
  private Statement statement;
  private XPath xPath;
  private Document outDocument;
  private Boolean multiStatementDriver;
  private String globalCharsetName;
  
/**
Creates a <code>dbsql2xml</code> for output to <code>File</code>.

@param pathToMappingXmlFile e.g "treeXMLMapping.xml"
@param pathToXsdFile e.g. "dbsql2xml.xsd"
@param namesOfGlobalVariables e.g. "$gvG01#$gvG02#$gvG03" or "myVarX#myVarY#myVarZ"
@param valuesOfGlobalVariables e.f "01#John Smith#2005-05-10"
@param pathToOutputXmlFile e.g "treeXMLOut.xml"
<br />
<br />
token, which delimites names and values of global variables, is set in mapping XML file 
inside element <code>tokenForCommandLineStringArrays</code>
<br />
it can be single or multiple character
<br />
token is the same for names and values of global variables
<br />
<br />
So example of creating <code>dbsql2xml</code> object is:
<br />
<code>dbsql2xml myDbSql2Xml = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "$gvG01", "001", "treeXMLOut.xml");</code>
<br />
See example in distribution (standalone application and servlet). 
*/
//constructor for output into File
  public dbsql2xml(String pathToMappingXmlFile, String pathToXsdFile, String namesOfGlobalVariables, String valuesOfGlobalVariables, String pathToOutputXmlFile) {
    this.pathToMappingXmlFile = pathToMappingXmlFile;
    this.pathToXsdFile = pathToXsdFile;
    this.namesOfGlobalVariables = namesOfGlobalVariables;
    this.valuesOfGlobalVariables = valuesOfGlobalVariables;
    this.pathToOutputXmlFile = pathToOutputXmlFile;
    this.connection = null;
  }
  
/**
Creates a <code>dbsql2xml</code> for output to <code>String</code>, <code>Document</code> and <code>System.out</code>.

@param pathToMappingXmlFile e.g "treeXMLMapping.xml"
@param pathToXsdFile e.g. "dbsql2xml.xsd"
@param namesOfGlobalVariables e.g. "$gvG01#$gvG02#$gvG03" or "myVarX#myVarY#myVarZ"
@param valuesOfGlobalVariables e.f "01#John Smith#2005-05-10"
<br />
<br />
token, which delimites names and values of global variables, is set in mapping XML file 
inside element <code>tokenForCommandLineStringArrays</code>
<br />
it can be single or multiple character
<br />
token is the same for names and values of global variables
<br />
<br />
So example of creating <code>dbsql2xml</code> object is:
<br />
<code>dbsql2xml myDbSql2Xml = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "$gvG01", "001");</code>
<br />
See example in distribution (standalone application and servlet). 
*/
//constructor for other outputs
  public dbsql2xml(String pathToMappingXmlFile, String pathToXsdFile, String namesOfGlobalVariables, String valuesOfGlobalVariables) {
    this.pathToMappingXmlFile = pathToMappingXmlFile;
    this.pathToXsdFile = pathToXsdFile;
    this.namesOfGlobalVariables = namesOfGlobalVariables;
    this.valuesOfGlobalVariables = valuesOfGlobalVariables;
    this.pathToOutputXmlFile = null;
    this.connection = null;
  }
  
/**
Creates a <code>dbsql2xml</code> for output to <code>String</code>, <code>Document</code> and <code>System.out</code> with <code>Connection</code> already defined.

@param pathToMappingXmlFile e.g "treeXMLMapping.xml"
@param pathToXsdFile e.g. "dbsql2xml.xsd"
@param namesOfGlobalVariables e.g. "$gvG01#$gvG02#$gvG03" or "myVarX#myVarY#myVarZ"
@param valuesOfGlobalVariables e.g. "01#John Smith#2005-05-10"
@param connection 
<br />
<br />
token, which delimites names and values of global variables, is set in mapping XML file 
inside element <code>tokenForCommandLineStringArrays</code>
<br />
it can be single or multiple character
<br />
token is the same for names and values of global variables
<br />
<br />
So example of creating <code>dbsql2xml</code> object is:
<br />
<code>dbsql2xml myDbSql2Xml = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "$gvG01", "001");</code>
<br />
See example in distribution (standalone application and servlet). 
*/
//constructor for other outputs
  public dbsql2xml(String pathToMappingXmlFile, String pathToXsdFile, String namesOfGlobalVariables, String valuesOfGlobalVariables, Connection connection) throws Exception {
    this.pathToMappingXmlFile = pathToMappingXmlFile;
    this.pathToXsdFile = pathToXsdFile;
    this.namesOfGlobalVariables = namesOfGlobalVariables;
    this.valuesOfGlobalVariables = valuesOfGlobalVariables;
    this.pathToOutputXmlFile = null;
    this.connection = connection;
  }
  
//main function, which do all export  
  void doExport() {//former main()
    try {
      checkInputParameters();
      //load mapping (configuration) XML file
      Document document = createDocumentFromFile();
      //initialize XPath for future use
      xPath = XPathFactory.newInstance().newXPath();
      //load global variables from constructors parameters and mapping XML file
      loadGlobalVariables(namesOfGlobalVariables, valuesOfGlobalVariables, document);
      
      //create output XML document
      outDocument = createNewDocument();
      //create root element of output XML document
      Element rootElement = outDocument.createElement("dbsql2xml");
      applicationInfo = initApplicationInfo();
      rootElement.appendChild(outDocument.createComment(applicationInfo));
      outDocument.appendChild(rootElement);
      globalCharsetName = getString("config/globalCharsetName", document);

      if (connection == null) {
        //get jdbc connection parameters from XML file and connect to the database
        String jdbcDriver = getString("config/connectionProperties/jdbcDriver", document);
        String jdbcURL = getString("config/connectionProperties/jdbcURL", document);
        String jdbcUserName = getString("config/connectionProperties/jdbcUserName", document);
        String jdbcPassword = getString("config/connectionProperties/jdbcPassword", document);
        connection = getConnection(jdbcDriver, jdbcURL, jdbcUserName, jdbcPassword);
      }//end_if
      if (getBoolean("config/connectionProperties/multiStatementDriver", document)) {
        multiStatementDriver = true; 
      } else {
        multiStatementDriver = false;
      }//end_if-else
      statement = getStatement(connection);
      
      //load data from database to hierarchical XML document
      Element topLevelTableElement = getElement("/config/table", document);
      processTableElement(topLevelTableElement, rootElement);
      connection.close();//hmmm, and what about connection pooling?
      
      //save output XML document into file (later somewhere else)
      outDocument.normalizeDocument();
      
      //append to or process output XML document XSLT or CSS (if any) in mapping file
      Element processingInstructionsElement = getElement("config/processingInstructions", document);
      doProcessingInstructions(processingInstructionsElement, rootElement);
      
      System.out.println("Should be done.");
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(e.getCause());
      System.gc();
    } finally {
    }
  }
  
  String initApplicationInfo() {
    String string = "The \"dbsql2xml\" is Java tool (class) for transforming (export) relational databases into hierarchical XML.\nFor more information, see http://sourceforge.net/projects/dbsql2xml/";
    return string;
  }
  
  void checkInputParameters() throws Exception {
    try {
      if (pathToMappingXmlFile == null | pathToMappingXmlFile == "") {
        throw new Exception("No mapping XML file specified.");
      }//end_if
      if (pathToXsdFile == null | pathToXsdFile == "") {
        throw new Exception("No XML Schema specified.");
      }//end_if
      if (namesOfGlobalVariables == null | valuesOfGlobalVariables == null) {
        namesOfGlobalVariables = "";
        valuesOfGlobalVariables = "";
      }//end_if
    } catch (Exception e) {
      System.out.println("pathToMappingXmlFile=\"" + pathToMappingXmlFile + "\", pathToXsdFile=\"" + pathToXsdFile + "\", namesOfGlobalVariables=\"" + namesOfGlobalVariables + "\", valuesOfGlobalVariables=\"" + valuesOfGlobalVariables + "\"");
      System.out.println("error in checkInputParameters");
      throw e;
    }//end_try-catch
  }
  
/**
Output results to <code>File</code>. It is intended for standalone export 
(transformation, extraction) application. Output encoding is in UTF-8. 
*/  
//do export and save it into output File  
  public void doExportIntoFile() throws Exception {
    try {
      if (pathToOutputXmlFile == null | pathToOutputXmlFile == "") {
        throw new Exception("It looks like not four parameters was given - no file for output");
      } else {
        doExport();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source source = new DOMSource(outDocument);
        StreamResult streamResult = new StreamResult(new File(pathToOutputXmlFile));
        transformer.transform(source, streamResult);
      }//end_if-else
    } catch (Exception e) {
      System.out.println("error in doExportIntoFile");
      throw e;
    }//end_try-catch
  }
/**
Output results to <code>String</code>. It is intended for Java servlets. 
Output encoding is in UTF-8. 
*/  
//do export and save it into String  
  public String doExportIntoString() throws Exception {
    try {
      doExport();
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      Source source = new DOMSource(outDocument);
      StreamResult streamResult = new StreamResult(byteArrayOutputStream);
      transformer.transform(source, streamResult);
      return byteArrayOutputStream.toString("UTF-8");//output is always in UTF-8
    } catch (Exception e) {
      System.out.println("error in doExportIntoString");
      throw e;
    }//end_try-catch
  }
/**
Output results to <code>System.out</code>. It is intended for debugging purposes on systems 
with console. Output encoding is in UTF-8. 
*/  
//do export and save it into standard output  
  public void doExportIntoStandardOutput() throws Exception {
    try {
      System.out.println(doExportIntoString());
    } catch (Exception e) {
      System.out.println("error in doExportIntoStandardOutput");
      throw e;
    }//end_try-catch
  }
/**
Output results to <code>Document</code>. It is intended for application 
integration into other XML related ones. Output encoding is in UTF-8. 
*/  
//do export and save it into Document  
  public Document doExportIntoDocument() throws Exception {
    try {
      doExport();
    return(outDocument);
    } catch (Exception e) {
      System.out.println("error in doExportIntoString");
      throw e;
    }//end_try-catch
  }
  
// load global variables from constructor and mapping XML document
  void loadGlobalVariables(String namesOfGlobalVariables, String valuesOfGlobalVariables, Document document) throws Exception {
    try {
      //load global variables given by mapping file
      NodeList nodeList = getNodeList("config/globalVariables/globalVariable", document);
      for (int i = 0; i < nodeList.getLength(); i++) {
        globalVariables.put(getString("@name", nodeList.item(i)), getString("self::node()", nodeList.item(i)));
      }//end_for
      //load global variables given by constructor
      String token = getString("config/tokenForCommandLineStringArrays", document);
      String[] globalVariablesNames = namesOfGlobalVariables.split(token);
      String[] globalVariablesValues = valuesOfGlobalVariables.split(token);
      if (globalVariablesNames.length == globalVariablesValues.length) {
        for (int i = 0; i < globalVariablesNames.length; i++) {
          globalVariables.put(globalVariablesNames[i], globalVariablesValues[i]);//global variables is HashMap
        }//end_for
      } else {
        throw new Exception("Constructor parameters global variables names and values does not match in count.");
      }//end_if-else
    } catch (Exception e) {
      System.out.println("error in loadGlobalVariables");
      throw e;
    }//end_try-catch
  }
  
// create Document from File
  Document createDocumentFromFile() throws Exception {//path to file is global
    String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      //validate against XML Schema in dbsql2xml.xsd
      documentBuilderFactory.setNamespaceAware(true);
      documentBuilderFactory.setValidating(true);//mapping file will be validated against definition in XML Schema
      documentBuilderFactory.setXIncludeAware(true);//mapping file can use XInclude
      documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, new File(pathToXsdFile));
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(new File(pathToMappingXmlFile));
      return document;
    } catch (Exception e) {
      System.out.println("error in createDocumentFromFile");
      throw e;
    }//end_try-catch
  }
  
// create new Document
  Document createNewDocument() throws Exception {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      return document;
    } catch (Exception e) {
      System.out.println("error in createNewDocument");
      throw e;
    }//end_try-catch
  }
  
// do (append or process) ProcessingInstruction to Document
  void doProcessingInstructions(Element processingInstructionsElement, Element rootElement) throws Exception {
    try {
      String xslt = getString("xslt", processingInstructionsElement);
      String css = getString("css", processingInstructionsElement);
      //System.out.println(xslt + ", " + css);
      if (xslt != "") {
        if (0 == 1) {
          //not now - to be done processing XML using XSLT in this program
        } else {
          ProcessingInstruction xmlstylesheet = outDocument.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"" + xslt + "\"");
          outDocument.insertBefore(xmlstylesheet, rootElement);
        }//end_if-else
      }//end_if
      if (css != "") {
        ProcessingInstruction xmlstylesheet = outDocument.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"" + css + "\"");
        outDocument.insertBefore(xmlstylesheet, rootElement);
      }//end_if
    } catch (Exception e) {
      System.out.println("error in doProcessingInstructions");
      throw e;
    }//end_try-catch
  }
  
// process TABLE elements
// should be better commented 
  void processTableElement(Element tableElement, Element parentElement) throws Exception {
    String tableElementName = tableElement.getAttribute("xmlName");
    String tableSql = tableElement.getAttribute("sql");//get SQL SELECT statement to be used for query column values
    tableSql = replaceGlobalVariable(tableSql);//replace global variables names by their values
    try {
      ResultSet resultSet = getResultSet(connection, tableSql);
      
      while (resultSet.next()) {
        Element newElement = outDocument.createElement(tableElementName);//each record is encapsulated by table xmlName
        NodeList nl = tableElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {//walk through each column of table element
          if (nl.item(i).getNodeName() == "column") {//XSD ensures columns first, table second
            processColumnElement((Element) nl.item(i), resultSet, newElement);
          }//end_if
          if (nl.item(i).getNodeName() == "table") {
            processTableElement((Element) nl.item(i), newElement);
          }//end_if
        }//end_for
        parentElement.appendChild(newElement);
      }//end_while
      //System.out.print(".");
    } catch (Exception e) {
      System.out.println(tableSql);
      System.out.println("error in processTableElement");
      throw e;
    }//end_try-catch
  }
  
// process COLUMN elements
// should be better commented 
  void processColumnElement(Element columnElement, ResultSet resultSet, Element parentElement) throws Exception {
    String columnElementName = columnElement.getAttribute("xmlName");
    String columnElementValue = columnElement.getAttribute("sqlName");
    String columnGlobalVariableName = columnElement.getAttribute("globalVariableName");
    String charsetName = columnElement.getAttribute("charsetName");
    if (charsetName == null | charsetName == "") {
      charsetName = globalCharsetName;
    }
    try {
      int columnIndex = resultSet.findColumn(columnElementValue);//index of this attribute in the ResultSet
      String columnValue = getColumnValueAsString(resultSet, columnIndex, charsetName);
      //is value of this column used inside following SQL SELECT statements? as global variable...
      if (columnGlobalVariableName != null) {
        globalVariables.put(columnGlobalVariableName, columnValue);
      }//end_if
      Element newElement = outDocument.createElement(columnElementName);
      Text newText = outDocument.createTextNode(columnValue);
      newElement.appendChild(newText);
      parentElement.appendChild(newElement);
    } catch (Exception e) {
      System.out.println("xmlName=\"" + columnElementName + "\", sqlName=\"" + columnElementValue + "\", globalVariableName=\"" + columnGlobalVariableName + "\", charsetName=\"" + charsetName + "\"");
      System.out.println("error in processColumnElement");
      throw e;
    }//end_try-catch
  }
  
// GENERIC FUNCTION - SQL, XML and XPATH functions
// can be in the separate class in some future
  
// XPath generic functions - they retrieve NodeList, Node, (String) Node, (Double) Node, (Boolean) Node, (Element) Node
// do not use them more, than needed - very slow!
  Element getElement(String xPathExpression, Node node) throws Exception {
    try {
      return (Element) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getNode");
      throw e;
    }//end_try-catch
  }
  Node getNode(String xPathExpression, Node node) throws Exception {
    try {
      return (Node) xPath.evaluate(xPathExpression, node, XPathConstants.NODE);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getNode");
      throw e;
    }//end_try-catch
  }
  NodeList getNodeList(String xPathExpression, Node node) throws Exception {
    try {
      return (NodeList) xPath.evaluate(xPathExpression, node, XPathConstants.NODESET);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getNodeList");
      throw e;
    }//end_try-catch
  }
  Double getNumber(String xPathExpression, Node node) throws Exception {
    try {
      return (Double) xPath.evaluate(xPathExpression, node, XPathConstants.NUMBER);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getNumber");
      throw e;
    }//end_try-catch
  }
  String getString(String xPathExpression, Node node) throws Exception {
    try {
      return (String) xPath.evaluate(xPathExpression, node, XPathConstants.STRING);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getString");
      throw e;
    }//end_try-catch
  }
  Boolean getBoolean(String xPathExpression, Node node) throws Exception {
    try {
      return (Boolean) xPath.evaluate(xPathExpression, node, XPathConstants.BOOLEAN);
    } catch (Exception e) {
      System.out.println("xPathExpression=\"" + xPathExpression + "\", node=\"" + node + "\"");
      System.out.println("error in getBoolean");
      throw e;
    }//end_try-catch
  }
  
// RegExp change global variables in SQL SELECT statement using regular expression
  String replaceGlobalVariable(String tableSql) {
    Set set = globalVariables.keySet();
    Iterator iterator = set.iterator();
    while(iterator.hasNext()){//walk through each global variables pair
      String key = (String) iterator.next();
      String value = (String) globalVariables.get(key);
      //here can be an error in RegEx - should be tested by users
      tableSql = tableSql.replaceAll("\\"+key+"{1,}", value);//replace key by value
    }//end_while
    return tableSql;
  }
  
// SQL generic functions
  Connection getConnection(String jdbcDriver, String jdbcUrl, String jdbcUserName, String jdbcPassword) throws Exception {
    try {
      Class.forName(jdbcDriver);
      Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword);
      return connection;
    } catch (Exception e) {
      System.out.println("jdbcDriver=\"" + jdbcDriver + "\", jdbcUrl=\"" + jdbcUrl + "\", jdbcUserName and jdbcPassword are not logged");
      System.out.println("error in getConnection");
      throw e;
    }//end_try-catch
  }
  Statement getStatement(Connection connection) throws Exception {
    try {
      Statement statement = connection.createStatement();
      return statement;
    } catch (Exception e) {
      System.out.println("connection=\"" + connection + "\"");
      System.out.println("error in getStatement");
      throw e;
    }//end_try-catch
  }
  ResultSet getResultSet(Connection connection, String sqlSelect) throws Exception {
    try {
    //if-else for drivers, which can not have multiple ResultSet-s per one Statement
      if (multiStatementDriver) {
        Statement statement = getStatement(connection);
        return statement.executeQuery(sqlSelect);
      } else {
        return statement.executeQuery(sqlSelect);
      }//end_if-else
    } catch (Exception e) {
      System.out.println("connection=\"" + connection + "\", sqlSelect=\"" + sqlSelect + "\", multiStatementDriver=\"" + multiStatementDriver + "\"");
      System.out.println("error in getResultSet");
      throw e;
    }//end_try-catch
  }
  String getColumnValueAsString(ResultSet resultSet, int columnIndex, String charsetName) throws Exception {
    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
    try {
      switch (resultSetMetaData.getColumnType(columnIndex)) {
        case java.sql.Types.VARCHAR : 
        case java.sql.Types.CHAR : 
        case java.sql.Types.LONGVARCHAR : 
          byte[] byteArray = resultSet.getBytes(columnIndex);
          if (byteArray == null || resultSet.wasNull()) {
            return "null";
          } else {
            String stringFromChar = new String(byteArray, charsetName);
            return stringFromChar;
          }//end_if-else
        case java.sql.Types.DOUBLE : 
        case java.sql.Types.INTEGER : 
        case java.sql.Types.BIGINT : 
        case java.sql.Types.TINYINT : 
        case java.sql.Types.SMALLINT : 
        case java.sql.Types.REAL : 
        case java.sql.Types.FLOAT : 
        case java.sql.Types.DECIMAL : 
        case java.sql.Types.NUMERIC : 
        case java.sql.Types.BIT : 
          String stringFromNumeric = resultSet.getString(columnIndex);
          if (resultSet.wasNull()) {
            return "null";
          } else {
            return stringFromNumeric;
          }//end_if-else
        case java.sql.Types.BINARY : 
        case java.sql.Types.VARBINARY : 
        case java.sql.Types.LONGVARBINARY : 
          String stringFromBinary = resultSet.getString(columnIndex);
          if (resultSet.wasNull()) {
            return "null";
          } else {
            return stringFromBinary;
          }//end_if-else
        case java.sql.Types.DATE : 
          java.sql.Date date = resultSet.getDate(columnIndex);
          if (resultSet.wasNull()) {
            return "null";
          } else {
            return date.toString();
          }//end_if-else
        case java.sql.Types.TIME : 
          java.sql.Time time = resultSet.getTime(columnIndex);
          if (resultSet.wasNull()) {
            return "null";
          } else {
            return time.toString();
          }//end_if-else
        case java.sql.Types.TIMESTAMP : 
          java.sql.Timestamp timestamp = resultSet.getTimestamp(columnIndex);
          if (resultSet.wasNull()) {
            return "null";
          } else {
            return timestamp.toString();
          }//end_if-else
        default : 
          return "unsupported data type (CLOB and BLOB should be, ARRAY, REF, STRUCT, JAVA OBJECT hard to be)";
      }//end_switch
    } catch (Exception e) {
      System.out.println("resultSet=\"" + resultSet + "\", column index=\"" + columnIndex + "\", column type=\"" + resultSetMetaData.getColumnType(columnIndex) + "\", charsetName=\"" + charsetName + "\"");
      System.out.println("error in getColumnValueAsString");
      throw e;
    }//end_try-catch
  }
  
  /**
   * @param xml
   * @throws JSONException
   * @returns JSONObject
   */
  public JSONObject xml2json(String xml) throws JSONException {
    //String xml = "<employee><name>ABC</name><age>32</age></employee>";
    JSONObject obj = XML.toJSONObject(xml);
    System.out.println(obj);
    return obj;
  }

  
}