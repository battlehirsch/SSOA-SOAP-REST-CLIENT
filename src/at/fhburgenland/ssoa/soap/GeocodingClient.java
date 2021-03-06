package at.fhburgenland.ssoa.soap;

import at.fhburgenland.ssoa.common.ConstantHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

/**
 * Created by Norbert.Fesel on 18.12.2016.
 * SOAP-Client welcher den internationalen SOAP-Geocodingservice von "Postcodeanywhere" anspricht.
 */
public class GeocodingClient {
    /**
     * Versucht den Geocoding-Service von "PostCodeAnywhere" mit den zuvor eingegebenen Parametern abzufragen.
     * Ist der API Key, das Land oder der Ort ungültig, so kommt es zu einer Fehlermeldung.
     * Als Vorlage für diese Methode diente eine Referenzimplementierung von "Postcodeanywhere".
     * @param Key
     * gültiger API Key von PostCodeAnywhere
     * @param Country
     * gewünschtes Land als Name in englischer Sprache bzw als ISO-Code
     * @param Location
     * gewünschter Ort im gewählten Land als Name in englischer Sprache oder Postleitzahl
     * @return
     * Retourniert die Antwort als Hashtable.
     * @throws Exception
     * Fehlermeldung wird absichtlich geworfen, da sich im ServiceConsumer um eine korrekte Fehlerausgabe gekümmert wird.
     */
    public Hashtable[] Geocoding_International_Geocode_v1_10(String Key, String Country, String Location) throws Exception {

        //Initialisierung der benötigten Variablen
        String requestUrl = new String();
        String key = new String();
        String value = new String();

        //Aufbau der benötigten URL zum Abruf des Services
        requestUrl = ConstantHelper.GEOCODING_URL;
        requestUrl += "&Key=" + URLEncoder.encode(Key);
        requestUrl += "&Country=" + URLEncoder.encode(Country);
        requestUrl += "&Location=" + URLEncoder.encode(Location);

        //Einholen der Daten von der generierten URL
        URL url = new URL(requestUrl);
        InputStream stream = url.openStream();
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dataDoc = docBuilder.parse(stream);

        //Einholen der Referenzen für die Schemen / Daten
        NodeList schemaNodes = dataDoc.getElementsByTagName("Column");
        NodeList dataNotes = dataDoc.getElementsByTagName("Row");

        //Errorhandling
        if (schemaNodes.getLength() == 4 && schemaNodes.item(0).getAttributes().getNamedItem("Name").getNodeValue().equals("Error")) {
            throw new Exception(dataNotes.item(0).getAttributes().getNamedItem("Description").getNodeValue());
        }
        ;

        //Bearbeitung der einzelnen Elemente des Resultats
        Hashtable[] results = new Hashtable[dataNotes.getLength()];
        for (int rowCounter = 0; rowCounter < dataNotes.getLength(); rowCounter++) {
            Hashtable rowData = new java.util.Hashtable();
            for (int colCounter = 0; colCounter < schemaNodes.getLength(); colCounter++) {
                key = (String) schemaNodes.item(colCounter).getAttributes().getNamedItem("Name").getNodeValue();
                if (dataNotes.item(rowCounter).getAttributes().getNamedItem(key) == null) {
                    value = "";
                } else {
                    value = (String) dataNotes.item(rowCounter).getAttributes().getNamedItem(key).getNodeValue();
                }
                ;
                rowData.put(key, value);
            }
            results[rowCounter] = rowData;
        }

        //Return der Resultate
        return results;
    }

    /**
     * Verarbeitet die Antwort einer erfolgreichen Abfrage der "PostCodeAnywhere" API und zeigt relevante Informationen in der Konsole an.
     * @param geocodingResult
     * Ergebnis der Abfrage als Hashtable
     */
    public void printGeocodingResult(Hashtable[] geocodingResult) {
        if (geocodingResult != null || geocodingResult[0] != null) {
            if (geocodingResult[0].get("Name") != null && !"".equals(geocodingResult[0].get("Name"))) {
                System.out.println("Name: " + geocodingResult[0].get("Name"));
            } else {
                System.out.println("Name not found.");
            }

            if (geocodingResult[0].get("Latitude") != null && !"".equals(geocodingResult[0].get("Latitude"))) {
                System.out.println("Latitude: " + geocodingResult[0].get("Latitude"));
            } else {
                System.out.println("Latitude not found.");
            }

            if (geocodingResult[0].get("Longitude") != null && !"".equals(geocodingResult[0].get("Longitude"))) {
                System.out.println("Longitude: " + geocodingResult[0].get("Longitude"));
            } else {
                System.out.println("Longitude not found.");
            }
        }
    }
}
