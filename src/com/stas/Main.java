package com.stas;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String line;
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
        line = "uci" + "\n";
        stdin.write(line.getBytes() );
        stdin.flush();

        // "write" the parms into stdin
        line = "go infinite" + "\n";
        stdin.write(line.getBytes() );
        stdin.flush();

        // clean up if any output in stdout
        BufferedReader brCleanUp =
                new BufferedReader (new InputStreamReader(stdout));
        while ((line = brCleanUp.readLine ()) != null) {
            System.out.println ("[Stdout] " + line);
        }
        brCleanUp.close();

        // clean up if any output in stderr
        brCleanUp =
                new BufferedReader (new InputStreamReader (stderr));
        while ((line = brCleanUp.readLine ()) != null) {
            System.out.println ("[Stderr] " + line);
        }
        stdin.close();
        brCleanUp.close();
    }
}
