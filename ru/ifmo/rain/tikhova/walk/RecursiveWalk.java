package ru.ifmo.rain.tikhova.walk;

import java.io.*;
import java.nio.file.*;

public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected usage: java Walk <source name> <destination name>");
            return;
        }
        String sourceName = args[0];
        String destinationName = args[1];
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(sourceName), "UTF-8"))) {
            // Create destination file if it doesn't exist
            File dest = new File(destinationName);
            if (!dest.exists()) {
                if (dest.getParentFile() != null)
                    dest.getParentFile().mkdirs();
                dest.createNewFile();
            }

            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destinationName), "UTF-8"))) {
                String path;
                while (in.ready()) {
                    path = in.readLine();
                    walk(path, out);
                }
                out.flush();
            }
        } catch (FileNotFoundException err) {
            System.out.println("Error: " + sourceName + " not found");
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }


    public static int FNV1Hash(String path) {
        try (InputStream in = new FileInputStream(path)) {
            int h = 0x811c9dc5;
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; ++i)
                    h = (h * 0x01000193) ^ (buffer[i] & 0xff);
            }
            return h;
        } catch (IOException err) {
            return 0;
        }
    }

    public static void printResult(String path, BufferedWriter out, int value) {
        try {
            out.write(String.format("%08x", value) + " " + path);
            out.newLine();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

    private static void walk(String path, BufferedWriter out) {
        File in;
        try {
            in = new File(path);

            if (in.isDirectory()) {
                Files.walkFileTree(in.toPath(), new MyFileVisitor(out));
            } else if (in.isFile()) {
                printResult(path, out, FNV1Hash(path));
            } else {
                printResult(path, out, 0);
            }
        } catch (NullPointerException err) {
            printResult(path, out, 0);
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }


}
