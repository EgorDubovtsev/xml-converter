import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleXmlConverter {
    private String url;
    private String diverClassName;
    private String username;
    private String password;
    private int n;
    private JdbcTemplate jdbcTemplate;
    private DocumentBuilder documentBuilder;

    private JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.url(url);
            dataSourceBuilder.driverClassName(diverClassName);
            dataSourceBuilder.username(username);
            dataSourceBuilder.password(password);
            jdbcTemplate = new JdbcTemplate(dataSourceBuilder.build());
        }
        return jdbcTemplate;

    }

    private void clearDb() {
        String clearDbSql = "DELETE FROM TEST";
        getJdbcTemplate().update(clearDbSql);
    }

    public void insertValues() {
        String insertValuesSql = "INSERT INTO TEST VALUES(?)";
        clearDb();
        int[] values = new int[n];
        for (int i = 1; i <= n; i++) {
            values[i - 1] = i;
        }
//        SqlParameterSource[] batch = SqlParameterSourceUtils
//                .createBatch(values.toArray());
        jdbcTemplate.batchUpdate(insertValuesSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1,values[i]);
            }

            @Override
            public int getBatchSize() {
                return values.length;
            }
        });
    }

    private int[] getValues() {
        String selectAllValuesSql = "SELECT * FROM TEST";
        int[] values;
        try {
            Statement statement = getJdbcTemplate().getDataSource()
                    .getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(selectAllValuesSql);
            resultSet.last();
            values = new int[resultSet.getRow()];
            resultSet.beforeFirst();
            int i = 0;
            while (resultSet.next()) {
                values[i] = resultSet.getInt(1);
                i++;
            }
            return values;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new int[0];
    }

    public void getValuesAndCreateXml(String documentName) throws ParserConfigurationException, TransformerException {
        int[] vales = getValues();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element rootElement = doc.createElement("entries");
        doc.appendChild(rootElement);


        for (int i = 1; i <= vales.length; i++) {
            Element entryElement = doc.createElement("entry");
            Element fieldElement = doc.createElement("field");
            fieldElement.appendChild(doc.createTextNode(String.valueOf(i)));
            entryElement.appendChild(fieldElement);
            rootElement.appendChild(entryElement);
        }


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult streamResult = new StreamResult(new File("./" + documentName));
        transformer.transform(source, streamResult);


    }

    public void convertXmlDocument(String nameBefore, String nameAfter) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(new File("converterHelper.xsl"));
        Transformer transformer = transformerFactory.newTransformer(xslt);
        Source xml = new StreamSource(new File(nameBefore));
        transformer.transform(xml, new StreamResult(new File(nameAfter)));
    }

    public void printArithmeticSumFromXml(String documentName) throws IOException, SAXException {
        Document document = documentBuilder.parse(documentName);
        Node root = document.getDocumentElement();
        NodeList listOfEntries = root.getChildNodes();
        double[] values = new double[listOfEntries.getLength()];
        for (int i = 0; i < listOfEntries.getLength(); i++) {
            values[i] = Integer.parseInt(listOfEntries.item(i).getAttributes().getNamedItem("field").getNodeValue());
        }
        double sum = ((values[0] + values[values.length - 1]) * values.length) / 2;
        System.out.println(sum);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDiverClassName() {
        return diverClassName;
    }

    public void setDiverClassName(String diverClassName) {
        this.diverClassName = diverClassName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
