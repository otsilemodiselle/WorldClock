import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HTTPDaemon {
    public static void main(String[] args) throws Exception {
        int port = 55555;

        ServerSocket server = new ServerSocket(port);
        System.out.println("Listening on http://127.0.0.1:" + port);

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected: " + client.getInetAddress());

            handle(client);

            client.close();
        }
    }

    private static void handle(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream())
        );
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream())
        );

        // 1) Read request line
        String requestLine = in.readLine();
        System.out.println("Request: " + requestLine);

        String method = "";
        String target = "/";

        if (requestLine != null) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                method = parts[0];
                target = parts[1];
            }
        }
        System.out.println("Method=" + method + " Target=" + target);

        LocalTime worldTime = switch (target) {
            case "/Tokyo" -> LocalTime.now(ZoneId.of("Asia/Tokyo"));
            case "/London" -> LocalTime.now(ZoneId.of("Europe/London"));
            case "/New_York" -> LocalTime.now(ZoneId.of("America/New_York"));
            case "/Sao_Paulo" -> LocalTime.now(ZoneId.of("America/Sao_Paulo"));
            case "/Lagos" -> LocalTime.now(ZoneId.of("Africa/Lagos"));
            default -> LocalTime.now(); // Defaults to your local South African time
        };
        LocalTime saTime = LocalTime.now(ZoneId.of("Africa/Johannesburg"));
        String sSATime = stringifyTime(saTime);
        String sWorldTime = stringifyTime(worldTime);

        // 2) Read headers until blank line (important)
        String line;
        while ( ((line = in.readLine()) != null) && (!line.isEmpty())
        ) {
            // Optional: print headers for debugging
            // System.out.println("Header: " + line);
        }

        // 3) Respond with a simple HTML page (for now)
        String body = """
                <html>
                <body>
                    <h2>Hello from COS332</h2>
                    <h2>World Clock</h2>
                    <p>South Africa: %s</p>
                    <p>%s: %s</p>
                    <p>
                        <a href="/Tokyo">Tokyo</a> 
                        <a href="/London">London</a> 
                        <a href="/New_York">New York</a>
                        <a href="/Sao_Paulo">Sao Paulo</a>
                        <a href="/Lagos">Lagos</a>
                        </p>
                </body>
                </html>""".formatted(sSATime, target.replace("/", "").replace("_", " "), sWorldTime);

        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: text/html; charset=UTF-8\r\n");
        out.write("Content-Length: " + body.getBytes("UTF-8").length + "\r\n");
        out.write("Connection: close\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    private static String stringifyTime(LocalTime time){
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}