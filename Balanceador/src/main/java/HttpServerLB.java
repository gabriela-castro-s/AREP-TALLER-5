import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Locale;

public class HttpServerLB {

    private static Integer port;
    private static Integer cont = 0;
    private static String[] name = {"backend1", "backend2", "backend3"};
    private static Integer[] ports = {35001, 35002, 35003};

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = null;
        try {
            port = getPort();
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;

        boolean running = true;


        while(running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean primerLinea = true;
            String file = "";

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (primerLinea) {
                    file = inputLine.split(" ")[1];
                    System.out.println("File" + file);
                    primerLinea = false;
                }
                if (!in.ready()) {
                    break;
                }
            }
            if (file.startsWith("/cadena")) {
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Title of the document</title>\n"
                        + "</head>"
                        + "<body>"
                        + "<label>Cadena:</label>\n"
                        + "<input name=\"name\">\n"
                        + "<button class=\"button\" onclick=\"inputValues()\">Result</button>"
                        + "<script>"
                        + "let numberc;"
                        + "let res1 = \"\";"
                        + "function inputValues() {\n" +
                        "    const url = '';\n" +
                        "    numberc = document.getElementsByName(\"name\")[0].value;\n" +
                        "    console.log(numberc);\n" +
                        "\n" +
                        //"    const url1 = `http://localhost:"+34003+"/consulta?cadena=${numberc}`;\n" +
                        "    const url1 = `http://ec2-100-25-213-230.compute-1.amazonaws.com:"+34003+"/consulta?cadena=${numberc}`;\n" +
                        "\n" +
                        "    getapi(url1);\n" +
                        "}"
                        + "async function getapi(url1){"
                        + "console.log(url1);"
                        + " const response = await fetch(url1, {method: 'GET', headers: {'Content-Type': 'application/json'}});\n"
                        + " let data = await response.json();"
                        + " res1 = data;"
                        + "console.log(res1);"
                        +" var x = document.getElementById(\"resultt\");\n" +
                        "    x.querySelector(\".example\").innerHTML = (JSON.stringify(res1));\n"
                        + "}"

                        + "</script>"
                        + "<div id=\"resultt\">\n" +
                        "                <p class=\"example\"></p>\n" +
                        "            </div"
                        + "</body>"
                        + "</html>";

            }else if(file.startsWith("/consulta")) {
                String res = "";
                try {
                    String nameC = file.toLowerCase(Locale.ROOT).split("=")[1];
                    res = getJSONClima(nameC);
                    System.out.println(res);
                    System.out.println(nameC + " sssssss");
                }catch (Exception e){

                }

                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"
                        + res;
            }
            else {
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Title of the document</title>\n"
                        + "</head>"
                        + "<body>"
                        + "My Web Site"
                        + "</body>"
                        + "</html>";
            }
            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567; //returns default port if heroku-port isn't set
    }
    private static String getJSONClima(String ciudad) {
        String res = "{}";
        URL url = null;
        try {
            url = new URL("http://"+name[cont]+":"+ports[cont]+"/savecadena?cadena="+ ciudad);
            if(cont == 2) {
                cont = 0;
            }else {
                cont++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (
                BufferedReader reader= new BufferedReader(new InputStreamReader(url.openStream()))) {
            String inputLine = null;

            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);
                res = inputLine;
            }
        } catch (IOException x) {
            System.err.println(x);
        }
        return res;
    }
}