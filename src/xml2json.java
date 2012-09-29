import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class xml2json {

  /**
   * @param args
   * @throws JSONException 
   */
  public static void main(String[] args) throws JSONException {
    String xml = "<employee><name>ABC</name><age>32</age></employee>";
    JSONObject obj = XML.toJSONObject(xml);
    System.out.println(obj);
  }

}
