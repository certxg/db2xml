/*
examples how to use "dbsql2xml"
these mapping XML files works without modification only on certain configuration of HSQLDB 1.8.0
otherwise You need to modify them
*/
import java.util.*;
import org.w3c.dom.*;

import org.json.JSONException;
import org.json.JSONObject;

public class examplesOfUse {

  public static void main(String[] args) {
    Date date;
    Date oldDate;
    /*
    standard output should be used for debugging
    File should beused for standalone conversion tool 
    String and Document should be used for server side component
      such as servlet or application server
    */
    try {
/*
      // FLAT XML

      //do export into standard output
      System.out.println("flat into standard output: ");
      oldDate = new Date();
      dbsql2xml test01 = new dbsql2xml("flatXMLMapping.xml", "dbsql2xml.xsd", "", "");
      test01.doExportIntoStandardOutput();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into File
      System.out.println("flat into File: ");
      oldDate = new Date();
      dbsql2xml test02 = new dbsql2xml("flatXMLMapping.xml", "dbsql2xml.xsd", "", "", "flatXMLOut.xml");
      test02.doExportIntoFile();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into String
      System.out.println("flat into String: ");
      oldDate = new Date();
      dbsql2xml test03 = new dbsql2xml("flatXMLMapping.xml", "dbsql2xml.xsd", "", "");
      String outputString03 = test03.doExportIntoString();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into XML Document
      System.out.println("flat into XML Document: ");
      oldDate = new Date();
      dbsql2xml test04 = new dbsql2xml("flatXMLMapping.xml", "dbsql2xml.xsd", "", "");
      Document outDocument04 = test04.doExportIntoDocument();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());
  
    	
      // TREE XML

      //do export into standard output
      System.out.println("tree into standard output: ");
      oldDate = new Date();
      dbsql2xml test05 = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "", "");
      test05.doExportIntoStandardOutput();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into File
      System.out.println("tree into File: ");
      oldDate = new Date();
      dbsql2xml test06 = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "", "", "treeXMLOut.xml");
      test06.doExportIntoFile();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into String
      System.out.println("tree into String: ");
      oldDate = new Date();
      dbsql2xml test07 = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "", "");
      String outputString07 = test07.doExportIntoString();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into XML Document
      System.out.println("tree into XML Document: ");
      oldDate = new Date();
      dbsql2xml test08 = new dbsql2xml("treeXMLMapping.xml", "dbsql2xml.xsd", "", "");
      Document outDocument08 = test08.doExportIntoDocument();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      // TREE XML WITH CHARSET AND XINCLUSION

      //do export into standard output
      System.out.println("advanced tree into standard output: ");
      oldDate = new Date();
      dbsql2xml test09 = new dbsql2xml("treeXMLMappingWithXInclusionAndCharset.xml", "dbsql2xml.xsd", "", "");
      test09.doExportIntoStandardOutput();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());

      //do export into File
      System.out.println("advanced tree into File: ");
      oldDate = new Date();
      dbsql2xml test10 = new dbsql2xml("treeXMLMappingWithXInclusionAndCharset.xml", "dbsql2xml.xsd", "", "", "treeXMLMappingWithXInclusionAndCharsetOut.xml");
      test10.doExportIntoFile();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());
*/
      //do export into String
      System.out.println("advanced tree into String: ");
      oldDate = new Date();
      dbsql2xml test11 = new dbsql2xml("treeXMLMappingWithXInclusionAndCharset.xml", "dbsql2xml.xsd", "", "");
      String outputString11 = test11.doExportIntoString();
      
      JSONObject obj = test11.xml2json(outputString11);
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());
/*
      //do export into XML Document
      System.out.println("advanced tree into XML Document: ");
      oldDate = new Date();
      dbsql2xml test12 = new dbsql2xml("treeXMLMappingWithXInclusionAndCharset.xml", "dbsql2xml.xsd", "", "");
      Document outDocument12 = test12.doExportIntoDocument();
      date = new Date();
      System.out.println(date.getTime() - oldDate.getTime());
*/
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(e.getCause());
    } 
  }
  
}
