/***
 * github @kev1nl1u
 * HTTP server for calendar management webapp
 */

// libraries
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// main class
public class Server {

    public static void main(String[] args) {
        int port = 8080; // Default port
        String root = "public"; // Default dir

        // start server and handle requests
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[INFO] Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Worker worker = new Worker(clientSocket, root);
                worker.start(); // new req
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// worker class
class Worker extends Thread {
    private Socket clientSocket;
    private String root;

    public Worker(Socket clientSocket, String root) {
        this.clientSocket = clientSocket;
        this.root = root;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream output = clientSocket.getOutputStream();

            String request = input.readLine(); // First line of the request
            String[] reqInfo = request.split("\\s+"); // Split by whitespace
            String method = reqInfo[0]; // method - allowed: GET, POST
            String fileName = reqInfo[1];
            if (fileName.equals("/")) fileName = "/index"; // Default file

            // GET and POST requests
            if (method.equals("GET")) {
                // do not log common files get requests
                if(!fileName.contains("/common/")) System.out.println("[INFO] " + clientSocket.getInetAddress() + " GET " + fileName);
                get(output, fileName, root);
            } else if (method.equals("POST")) {
                post(input, output, fileName, root);
            } else {
                System.out.println("[WARN] " + clientSocket.getInetAddress() + " Unsupported method: " + method);
                clientSocket.close();
                return;
            }

            output.flush();
            output.close();
            input.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get function
    private void get(OutputStream output, String fileName, String root) throws IOException {
        if(fileName.equals("/dl")){
            // download xml
            File file = new File("calendar.xml");
            byte[] fileContent = new byte[(int) file.length()];
            FileInputStream fileInput = new FileInputStream(file);
            fileInput.read(fileContent);
            fileInput.close();

            output.write("HTTP/1.1 200 OK\r\n".getBytes());
            output.write(("Content-Length: " + fileContent.length + "\r\n").getBytes());
            output.write("Content-Disposition: attachment; filename=\"calendar.xml\"\r\n".getBytes());
            output.write("\r\n".getBytes());
            output.write(fileContent);
            return;
        }
        // if filename does not contain /common/ add .html
        if(!fileName.contains("/common/")) fileName += ".html";
        
        File file = new File(root + fileName);

        if (file.exists() && !file.isDirectory()) {
            // read file as byte array
            byte[] fileContent = new byte[(int) file.length()];
            FileInputStream fileInput = new FileInputStream(file);
            fileInput.read(fileContent);
            fileInput.close();

            output.write("HTTP/1.1 200 OK\r\n".getBytes());
            output.write(("Content-Length: " + fileContent.length + "\r\n").getBytes());
            output.write("\r\n".getBytes());
            output.write(fileContent);
        } else {
            // 404
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            System.out.println("[WARN] " + clientSocket.getInetAddress() + " 404: " + fileName);
        }
    }

    // post function
    private void post(BufferedReader input, OutputStream output, String fileName, String root) throws IOException {
        String line;
        StringBuilder requestBody = new StringBuilder();

        // Read and log request
        while ((line = input.readLine()) != null)
            if (line.isEmpty())
                break; // End of headers - empty line

        // read request body
        while (input.ready()) requestBody.append((char) input.read());

        switch(fileName){
            case "/post/getEvents": // get events by date
                System.out.println("[INFO] " + clientSocket.getInetAddress() + " POST " + fileName + " " + requestBody.toString());

                // parse date
                String date = requestBody.toString().split(":")[1].replaceAll("[^0-9-]", "");
                // get events for date in xml
                NodeList events = getXMLEventsByDate(date);
                // convert events to json
                String json = nodeList2Json(events);
                System.out.println(json);

                // send json
                output.write("HTTP/1.1 200 OK\r\n".getBytes());
                output.write(("Content-Length: " + json.length() + "\r\n").getBytes());
                output.write("\r\n".getBytes());
                output.write(json.getBytes());
                break;

            case "/post/addEvent": // add event
                System.out.println("[INFO] " + clientSocket.getInetAddress() + " POST " + fileName + " " + requestBody.toString());

                addEvent(requestBody.toString());

                // send 200
                output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                break;

            case "/post/deleteEvent": // delete event
                System.out.println("[INFO] " + clientSocket.getInetAddress() + " POST " + fileName + " " + requestBody.toString());

                // get id from request body
                String id = requestBody.toString().split("=")[1];

                // delete event from xml
                deleteEvent(id);

                // send 200
                output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                break;

            default:
                // 404
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                System.out.println("[WARN] " + clientSocket.getInetAddress() + " 404: " + fileName);
                break;
        }

    }

    // get events by date from xml
    private NodeList getXMLEventsByDate(String date) {
        try {
            File file = new File("calendar.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList events = doc.getElementsByTagName("event");
            ArrayList<Node> res = new ArrayList<>();
            // for every event
            for (int i = 0; i < events.getLength(); i++) {
                Node event = events.item(i); // <event> i
                NodeList children = event.getChildNodes(); // <event> i children
                for (int j = 0; j < children.getLength(); j++) { // for every child
                    Node child = children.item(j); // <event> --> <summary> | <location> | <start> | <end> | ...
                    if(child.getNodeName().equals("start")){ // if child is <start>
                        // get <date>
                        for(int k = 0; k < child.getChildNodes().getLength(); k++) {
                            if(child.getChildNodes().item(k).getNodeName().equals("date") && child.getChildNodes().item(k).getTextContent().equals(date)){
                                res.add(event); // add event to result
                                System.out.println("[INFO] Event found: " + res.size());
                            }
                        }
                    }
                }
            }
            // arraylist to nodelist
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document newDoc = builder.newDocument();
            Element root = newDoc.createElement("events");
            newDoc.appendChild(root);
            for (Node node : res) {
                Node imported = newDoc.importNode(node, true);
                root.appendChild(imported);
            }
            return root.getChildNodes(); // returns a NodeList
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // conversion based on https://www.w3schools.com/js/js_json_xml.asp
    public static String nodeList2Json(NodeList nodeList) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"events\": [");
        
        // for every child of root
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i); // node <event>
            json.append("{");

            // add id attribute to json
            json.append("\"id\": \"" + node.getAttributes().getNamedItem("id").getNodeValue() + "\",");

            // count how many child ELEMENT_NODE to add comma for elements that have more children []
            int total_el = 0; for(int j = 0; j < node.getChildNodes().getLength(); j++) if(node.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) total_el++;
            int count_el = 0;

            // for every child of node <event>
            for (int j = 0; j < node.getChildNodes().getLength(); j++) {


                Node child = node.getChildNodes().item(j); // child of <event> i


                if(child.getNodeType() == Node.ELEMENT_NODE){ // if child is element (not text or comment or ...)

                    count_el++; // count how many elements are added

                    // classi, insegnanti, start, end are json arrays
                    if(!child.getNodeName().equals("classi") && !child.getNodeName().equals("insegnanti") && !child.getNodeName().equals("start") && !child.getNodeName().equals("end")){


                        json.append("\"" + child.getNodeName() + "\": \"" + child.getTextContent() + "\"");


                        if(j < node.getChildNodes().getLength() - 1) json.append(","); // add comma if it's not the last element


                    } else {


                        // count how many ELEMENT_NODE to add comma
                        int count = 0; for(int k = 0; k < child.getChildNodes().getLength(); k++) if(child.getChildNodes().item(k).getNodeType() == Node.ELEMENT_NODE) count++;
                        int commas_count = 0;
                        //System.out.println("COUNT " + count);

                        // if it's classi or insegnanti use array
                        // if it's start or end use object
                        json.append("\"" + child.getNodeName() + "\": [");
                        if(child.getNodeName().equals("start") || child.getNodeName().equals("end")){ // append node name
                            json.append("{");
                            for(int k = 0; k < child.getChildNodes().getLength(); k++){
                                Node subChild = child.getChildNodes().item(k);
                                if(subChild.getNodeType() == Node.ELEMENT_NODE){ // if subchild is element (not text or comment or ...)
                                    // add subchild to json
                                    json.append("\"" + subChild.getNodeName() + "\": \"" + subChild.getTextContent() + "\""); 
                                    commas_count++;
                                    //System.out.println("COMMAS " + commas_count);
                                    if(commas_count < count){
                                        json.append(",");
                                    }
                                }
                            }
                            // add timezone attribute
                            json.append(",\"timezone\": \"" + child.getAttributes().getNamedItem("timezone").getNodeValue() + "\"");
                            json.append("}");
                        }else{ // do not append node name
                            for(int k = 0; k < child.getChildNodes().getLength(); k++){
                                Node subChild = child.getChildNodes().item(k);
                                if(subChild.getNodeType() == Node.ELEMENT_NODE){ // if subchild is element (not text or comment or ...)
                                    json.append("\"" + subChild.getTextContent() + "\"");
                                    commas_count++;
                                    if(commas_count < count){
                                        json.append(",");
                                    }
                                }
                            }
                        }


                        // append ]
                        json.append("]");
                        
                        // add comma if it's not the last element
                        if(count_el < total_el){
                            json.append(",");
                        }

                    }
                }
            }
            json.append("}");
            // there shouldn't be other node types than ELEMENT_NODE in root children
            if(i < nodeList.getLength() - 1){
                json.append(",");
            }
        }

        json.append("]}");

        return json.toString();
    }

    // add event to xml
    private static void addEvent(String requestBody) {
        try {
            // replace + with space and decode
            requestBody = requestBody.replaceAll("\\+", " ");
            requestBody = java.net.URLDecoder.decode(requestBody, "UTF-8");
            // Parse the query string into key-value pairs
            Map<String, Object> params = new HashMap<>();

            // Split the requestBody into pairs
            String[] pairs = requestBody.split("&");

            // Loop through each pair to populate the params map with value or array of values
            for (String pair : pairs) {
                String[] keyValue = pair.split("="); // Split the pair into key and value
                String key = keyValue[0]; // Get the key
                if(key.contains("[]")){ // if key contains [] it's an array
                    key = key.replace("[]", ""); // remove [] from key
                    if (!params.containsKey(key)) { // if key is not in params add it
                        params.put(key, new ArrayList<String>());
                    }
                    ((ArrayList<String>) params.get(key)).add(keyValue[1]); // add value to array
                }else{ // if key does not contain [] it's a single value
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }

            // LOG params
            for (Map.Entry<String, Object> entry : params.entrySet()) System.out.println("\t" + entry.getKey() + ": " + entry.getValue());

            // add event to xml
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse("calendar.xml");
            doc.getDocumentElement().normalize();
            // get last id
            int id = Integer.parseInt(doc.getElementsByTagName("event").item(doc.getElementsByTagName("event").getLength() - 1).getAttributes().getNamedItem("id").getNodeValue());
            // <event>
            Element event = doc.createElement("event");
            event.setAttribute("id", String.valueOf(id + 1));

            // instances to write to file
            TransformerFactory transformerFactory;
            Transformer transformer;
            DOMSource source;
            StreamResult result;

            String eventType = (String) params.get("type");

            switch(eventType) { // switch prevents post injection
                case "SOSTITUZIONE":
                case "ENTRATA POSTICIPATA":
                case "USCITA ANTICIPATA":
                case "PRENOTAZIONE AULA":
                    setEventParams(doc, event, eventType, params);
                    break;

                case "ALTRO":
                    // set params
                    setEventParams(doc, event, (String) params.get("summary"), params);
                    break;

                default:
                    break;
            }

            // append event to root
            doc.getDocumentElement().appendChild(event);

            // write to file
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            source = new DOMSource(doc);
            result = new StreamResult(new File("calendar.xml"));
            transformer.transform(source, result);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static private void setEventParams(Document doc, Element event, String summaryText, Map<String, Object> params) {
        // <summary>
        Element summary = doc.createElement("summary");
        summary.appendChild(doc.createTextNode(summaryText));
        event.appendChild(summary);

        // <location>
        if(params.containsKey("location")) {
            Element location = doc.createElement("location");
            location.appendChild(doc.createTextNode((String) params.get("location")));
            event.appendChild(location);
        }

        // <classi>
        if(params.containsKey("classi")) {
            Element classi = doc.createElement("classi");
            ArrayList<String> classiList = (ArrayList<String>) params.get("classi");
            for(String classe : classiList) {
                Element cEl = doc.createElement("classe");
                cEl.appendChild(doc.createTextNode(classe));
                classi.appendChild(cEl);
            }
            event.appendChild(classi);
        }

        // <insegnanti>
        if(params.containsKey("insegnanti")) {
            Element insegnanti = doc.createElement("insegnanti");
            ArrayList<String> insegnantiList = (ArrayList<String>) params.get("insegnanti");
            for(String insegnante : insegnantiList) {
                Element iEl = doc.createElement("insegnante");
                iEl.appendChild(doc.createTextNode(insegnante));
                insegnanti.appendChild(iEl);
            }
            event.appendChild(insegnanti);
        }

        // <start>
        Element start = doc.createElement("start");
        start.setAttribute("timezone", "Europe/Rome");
        Element stdate = doc.createElement("date");
        stdate.appendChild(doc.createTextNode((String) params.get("date")));
        start.appendChild(stdate);
        Element sttime = doc.createElement("time");
        sttime.appendChild(doc.createTextNode((String) params.get("start") + ":00"));
        start.appendChild(sttime);
        event.appendChild(start);

        // <end>
        if(params.containsKey("end")) {
            Element end = doc.createElement("end");
            end.setAttribute("timezone", "Europe/Rome");
            Element enddate = doc.createElement("date");
            enddate.appendChild(doc.createTextNode((String) params.get("date")));
            end.appendChild(enddate);
            Element endtime = doc.createElement("time");
            endtime.appendChild(doc.createTextNode((String) params.get("end") + ":00"));
            end.appendChild(endtime);
            event.appendChild(end);
        }
    }

    // delete event from xml
    private static void deleteEvent(String id) {
        try {
            // read xml
            File file = new File("calendar.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            // get events
            NodeList events = doc.getElementsByTagName("event");
            // for every event
            for (int i = 0; i < events.getLength(); i++) {
                Node event = events.item(i); // <event> i
                // if id matches remove event
                if(event.getAttributes().getNamedItem("id").getNodeValue().equals(id)) event.getParentNode().removeChild(event);
            }

            // write to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("calendar.xml"));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
