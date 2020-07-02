package com.stas;

import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class EngineSharing {

    private static WebSocketClient client;

    public static void main(String[] args) throws IOException, URISyntaxException {
        initWebSocketClient();
        String engineLine;
        OutputStream stdin = null;
        InputStream stderr = null;
        InputStream stdout = null;

        // launch EXE and grab stdin/stdout and stderr
//        String uciEnginePath = "/home/stanislav/Downloads/stockfish-11-linux/stockfish-11-linux/Linux/stockfish_20011801_x64";

        System.out.print("Enter path to engine: ");

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        String uciEnginePath = reader.readLine();

        Process process = Runtime.getRuntime ().exec (uciEnginePath);
        stdin = process.getOutputStream ();
        stderr = process.getErrorStream ();
        stdout = process.getInputStream ();

        // "write" the parms into stdin
        sendCommand(stdin, "setoption name Skill Level value 20");
        sendCommand(stdin, "setoption name Hash value 1024");
        sendCommand(stdin, "setoption name Threads value 8");
        sendCommand(stdin, "setoption name Clear Hash value 1");
        sendCommand(stdin, "setoption name Contempt value 0");
        sendCommand(stdin, "setoption name multipv value 2");
        sendCommand(stdin, "go infinite");

        // clean up if any output in stdout
        BufferedReader brCleanUp =
                new BufferedReader (new InputStreamReader(stdout));
        while ((engineLine = brCleanUp.readLine ()) != null) {
            if (!engineLine.contains("seldepth")) {
                continue;
            }
            /*
             * if (!engineLine.toString().match(/seldepth/)) {
             *         return;
             *     }
             *
             *     console.log(`stdout: ${engineLine}`);
             *     let lines = engineLine.toString().split('\n');
             *     console.log('Lines', lines);
             *     lines.forEach(oneLine => {
             *         let data = JSON.stringify({
             *             direct: direct,
             *             message: 'Engine output: ' + oneLine
             *         });
             *         return oneLine.length > 0 ? socket.send(data) : null;
             *     });
             */

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

    private static void sendCommand(OutputStream stdin, String command) throws IOException {
        String line;
        line = command + "\n";
        stdin.write(line.getBytes() );
        stdin.flush();
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
            }

            @Override
            public void onMessage(String message) {

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
