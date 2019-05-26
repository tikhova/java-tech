package ru.ifmo.rain.tikhova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private ExecutorService workers;
    private DatagramSocket socket;
    private int bufferSize;

    public static void main(String[] args) {
        // Check correct usage
        final String USAGE = "Expected usage: HelloUDPServer [port] [threads]";
        if (args == null || args.length != 2) {
            System.out.println(USAGE);
            return;
        }
        try {
            new HelloUDPServer().start(Integer.getInteger(args[0]), Integer.getInteger(args[1]));
        } catch (NumberFormatException e) {
            System.out.println("Incorrect number: " + e.getMessage());
        }
    }


    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            workers = Executors.newFixedThreadPool(threads);
            bufferSize = socket.getReceiveBufferSize();
            for (int i = 0; i != threads; ++i) {
                workers.submit(this::performTask);
            }
        } catch (SocketException e) {
            System.out.println("Can't create socket");
        }

    }


    private void performTask() {
        try {
            while (!socket.isClosed()) {
                DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                socket.receive(request);
                String name = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
                byte[] resp = ("Hello, " + name).getBytes(StandardCharsets.UTF_8);
                DatagramPacket response = new DatagramPacket(resp, resp.length, request.getSocketAddress());
                socket.send(response);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        workers.shutdownNow();
        try {
            if (!workers.awaitTermination( 0, TimeUnit.SECONDS)) {
                System.out.println("Can't shutdown threads");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}
