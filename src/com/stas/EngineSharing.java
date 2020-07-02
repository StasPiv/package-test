package com.stas;

import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EngineSharing {

    private static WebSocketClient client;
    private static OutputStream stdin = null;

    public static void main(String[] args) throws IOException, URISyntaxException {
        initWebSocketClient();
        String engineLine;
        InputStream stderr = null;
        InputStream stdout = null;


        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
//        String uciEnginePath = "/home/stanislav/Downloads/stockfish-11-linux/stockfish-11-linux/Linux/stockfish_20011801_x64";
        System.out.print("Enter path to engine: ");
        String uciEnginePath = reader.readLine();

        Process process = Runtime.getRuntime ().exec (uciEnginePath);
        stdin = process.getOutputStream ();
        stderr = process.getErrorStream ();
        stdout = process.getInputStream ();

        // "write" the parms into stdin
        sendCommand("setoption name Skill Level value 20");
        sendCommand("setoption name Hash value 1024");
        sendCommand("setoption name Threads value 8");
        sendCommand("setoption name Clear Hash value 1");
        sendCommand("setoption name Contempt value 0");
        sendCommand("setoption name multipv value 2");

        // clean up if any output in stdout
        BufferedReader brCleanUp =
                new BufferedReader (new InputStreamReader(stdout));
        while ((engineLine = brCleanUp.readLine ()) != null) {
            if (!engineLine.contains("seldepth")) {
                continue;
            }

            System.out.println ("[Stdout] " + engineLine);
            if (client.isOpen()) {
                sendWsMessage("Engine output: " + engineLine);
            }
        }
        brCleanUp.close();

        // clean up if any output in stderr
        brCleanUp =
                new BufferedReader (new InputStreamReader (stderr));
        while ((engineLine = brCleanUp.readLine ()) != null) {
            System.out.println ("[Stderr] " + engineLine);
        }
        stdin.close();
        brCleanUp.close();
    }

    private static void sendCommand(String command) {
        String line;
        line = command + "\n";
        try {
            stdin.write(line.getBytes() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stdin.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendWsMessage(String message) throws URISyntaxException {
        JsonObject messageJson = new JsonObject();
        messageJson.addProperty("direct", "stas");
        messageJson.addProperty("message", message);
        client.send(messageJson.toString());
    }

    private static void initWebSocketClient() throws URISyntaxException {
//        System.out.print("Try to init websocket client");
        client = new WebSocketClient(new URI("ws://3.20.233.150:8000")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
//                System.out.println ("[Stdout] " + "open connection");
                try {
                    sendWsMessage("subscribe.direct.stas");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String message) {
                System.out.println ("[Stdout] " + message);
                if (message.equals("stop engine")) {
                    sendCommand("stop");
                    return;
                }

                // String to be scanned to find the pattern.
                String pattern = "start-infinite (.+)";

                // Create a Pattern object
                Pattern r = Pattern.compile(pattern);

                // Now create matcher object.
                Matcher m = r.matcher(message);

                if (!m.find( )) {
                    return;
                }

                String fen = m.group(1);

                sendCommand("stop");
                sendCommand("position fen " + fen);
                sendCommand("go infinite");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {
                System.out.println ("[Stderr] " + ex.getMessage());
            }
        };

        client.connect();

    }
}
