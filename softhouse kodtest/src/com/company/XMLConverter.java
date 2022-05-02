package com.company;

import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

public class XMLConverter {
    // read/write files
    BufferedReader br;
    StreamResult sr;

    // root xml elements
    Document xmldoc;
    Element root;
    Element person = null;
    Element family = null;

    //marked indicates if elements are to be assigned to person or family (address, phone number)
    String marked = "";

    //path to files in "files" folder    change to ./src/files/ if you want to run from here
    File txtPath = new File("people.txt");
    File xmlPath = new File("people.xml");


    public static void main(String[] args) {
        new XMLConverter().run();
    }

    // initialize reader/writer and read the file
    public void run() {
        try {
            br = new BufferedReader(new FileReader(txtPath));
            sr = new StreamResult(xmlPath);
            initXML();
            String str;
            while ((str = br.readLine()) != null) {
                process(str);
            }
            br.close();
            writeXML();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //initialize builders to create doc reference and root element reference
    public void initXML() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        xmldoc = impl.createDocument(null, "people", null);
        root = xmldoc.getDocumentElement();
    }

    // filter by index 0 and create required elements
    public void process(String str) {
        String[] elements = str.split("\\|");
        Node n;

        // P for person, creates elements and appends
        if (elements[0].equals("P")) {

            if (person != null) {
                if(marked.equals("F")){
                    person.appendChild(family);
                    family=null;
                }
                // person information is complete and appended to root.
                root.appendChild(person);
                person = null;
            }
            //marked changes to P as future values will be assigned to person rather than family.
            marked = "P";

            //initialize new person and append information.
            person = xmldoc.createElement("person");
            Element fname = xmldoc.createElement("firstname");
            n = xmldoc.createTextNode(elements[1]);
            fname.appendChild(n);
            Element lname = xmldoc.createElement("lastname");
            n = xmldoc.createTextNode(elements[2]);
            lname.appendChild(n);

            person.appendChild(fname);
            person.appendChild(lname);


          // T for telephone, initialize parent element phone and append phone numbers.
        } else if (elements[0].equals("T")) {

            Element phone = xmldoc.createElement("phone");
            Element cellphone = xmldoc.createElement("cellphone");
            n = xmldoc.createTextNode(elements[1]);
            cellphone.appendChild(n);

            Element landline = xmldoc.createElement("landline");
            n = xmldoc.createTextNode(elements[2]);
            landline.appendChild(n);
            phone.appendChild(cellphone);
            phone.appendChild(landline);

            // append to parent depending on whose it is
            if (marked.equals("F")) {
                family.appendChild(phone);
            } else {
                person.appendChild(phone);
            }


            //A for address, initialize parent element and append information
        } else if (elements[0].equals("A")) {

            Element address = xmldoc.createElement("address");
            Element street = xmldoc.createElement("street");
            n = xmldoc.createTextNode(elements[1]);
            street.appendChild(n);

            Element city = xmldoc.createElement("city");
            n = xmldoc.createTextNode(elements[2]);
            city.appendChild(n);


            address.appendChild(street);
            address.appendChild(city);


            // special case as certain addresses did not contain zip codes
            if (elements.length == 4) {
                Element zip = xmldoc.createElement("zipcode");
                n = xmldoc.createTextNode(elements[3]);
                zip.appendChild(n);
                address.appendChild(zip);
            }

                // append to parent depending on whose it is
            if (marked.equals("F")) {

                family.appendChild(address);

            } else {

                person.appendChild(address);

            }

            // F for family, create parent element and append information
        } else if (elements[0].equals("F")) {
            //marked set to F so that future values will be appended to family rather than person
            marked = "F";

            // new F tag encountered, append previous family and start a new one
            if(family != null){
                person.appendChild(family);
            }

            family = xmldoc.createElement("family");
            Element name = xmldoc.createElement("name");
            Element born = xmldoc.createElement("born");

            n = xmldoc.createTextNode(elements[1]);
            name.appendChild(n);
            n = xmldoc.createTextNode(elements[2]);
            born.appendChild(n);

            family.appendChild(name);
            family.appendChild(born);


        }

    }

    public void writeXML() throws TransformerException {
        // append last person
        if(person!= null){
            root.appendChild(person);
        }
        // properties for xmldoc and then write using sr
        DOMSource domSource = new DOMSource(xmldoc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(domSource, sr);
    }

}
