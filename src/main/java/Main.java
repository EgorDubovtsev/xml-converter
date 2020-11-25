import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public class Main {
    public static void main(String[] args) {
        SimpleXmlConverter simpleXmlConverter = new SimpleXmlConverter();
        simpleXmlConverter.setDiverClassName("org.postgresql.Driver");
        simpleXmlConverter.setN(2);
        simpleXmlConverter.setUsername("postgres");
        simpleXmlConverter.setPassword("2002");
        simpleXmlConverter.setUrl("jdbc:postgresql://localhost:5432/postgres");

        simpleXmlConverter.insertValues();
        try {
            simpleXmlConverter.getValuesAndCreateXml("1.xml");
            simpleXmlConverter.convertXmlDocument("1.xml","2.xml");
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
