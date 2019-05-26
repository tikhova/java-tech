package ru.ifmo.rain.tikhova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    public static void main(String[] args) {
        // Check correct usage
        final String USAGE = "Expected usage: HelloUDPClient [host or ip] [port] [prefix] [threads] [requests]";
        if (args == null || args.length != 5) {
            System.out.println(USAGE);
            return;
        }
        try {
            new HelloUDPClient().run(args[0], Integer.getInteger(args[1]), args[2],
                    Integer.getInteger(args[3]), Integer.getInteger(args[4]));
        } catch (NumberFormatException e) {
            System.out.println("Incorrect number: " + e.getMessage());
        }
    }

    @Override
    public void run(String name, int port, String prefix, int threads, int requests) {
        InetAddress address;
        try {
            address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host : " + name);
            return;
        }

        final SocketAddress destination = new InetSocketAddress(address, port);
        final ExecutorService workers = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads).forEach(i -> workers.submit(() -> performTask(i, destination, prefix, requests)));

        workers.shutdown();
        try {
            if (!workers.awaitTermination(threads * requests * 100, TimeUnit.SECONDS)) {
                System.out.println("Can't shutdown threads");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

    private void performTask(int thread, SocketAddress dest, String prefix, int requests) {
        String base = prefix + thread + "_";
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            for (int i = 0; i != requests; ++i) {
                final String req = base + i;
                final DatagramPacket request = new DatagramPacket(req.getBytes(StandardCharsets.UTF_8), req.length(), dest);
                final DatagramPacket response = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
                System.out.println(req);
                while (true) {
                    // Make request
                    try {
                        socket.send(request);
                    } catch (IOException ignored) {
                        continue;
                    }

                    // Receive response
                    try {
                        socket.receive(response);
                    } catch (IOException ignored) {
                        continue;
                    }

                    // Check response
                    String resp = new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8);
                    if (resp.contains(req)) {
                        System.out.println(resp);
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Failed to create socket: " + e.getMessage());
        }
    }
}
