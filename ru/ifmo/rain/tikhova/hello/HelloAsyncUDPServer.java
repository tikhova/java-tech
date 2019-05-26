package ru.ifmo.rain.tikhova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

public class HelloAsyncUDPServer implements HelloServer {
    private Selector selector;
    private final byte[] hello = ("Hello, ").getBytes(StandardCharsets.UTF_8);
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) {
        // Check correct usage
        final String USAGE = "Expected usage: HelloUDPServer [port] [threads]";
        if (args == null || args.length != 2) {
            System.out.println(USAGE);
            return;
        }
        try {
            new HelloAsyncUDPServer().start(Integer.getInteger(args[0]), Integer.getInteger(args[1]));
        } catch (NumberFormatException e) {
            System.out.println("Incorrect number: " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.out.println("Failed to open Selector");
            return;
        }

        // Register [threads] channels
        IntStream.range(0, threads).forEach(i -> {
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
            datagramChannel.configureBlocking(false);
            datagramChannel.connect(new InetSocketAddress(port));
            datagramChannel.register(selector, SelectionKey.OP_READ);

            receiveBuffer.clear();
            datagramChannel.read(receiveBuffer);
            System.out.println(receiveBuffer.position());

            sendBuffer.clear();
            sendBuffer.put(hello);
            //sendBuffer.put();
            sendBuffer.flip();
            datagramChannel.send(sendBuffer, new InetSocketAddress(InetAddress.getLocalHost(), port));
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("Failed to submit task " + e.getMessage() + "\nport: " + port);
        }
        });

        // Run selector
//        try {
//            System.out.println("Run selector:");
//
//            run(selector);
//        } catch (IOException e) {
//            System.out.println("Failed to run selector");
//        }

    }

    private void run(final Selector selector) throws IOException {
        System.out.println(selector.keys().size());
        while (selector.isOpen()) {
            selector.selectNow();
//            if (selector.selectedKeys().size() == 0) {
//                System.out.println("Yep");
//                break;
//            }
            for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {

                System.out.println("==== Running");
                final SelectionKey key = i.next();
                i.remove();
                if (key.isReadable()) {
                    receive(key);
                }

                if (key.isValid() && key.isWritable()) {
                    receive(key);
                    send(key);
                }
//                try (DatagramChannel datagramChannel = (DatagramChannel) key.channel()) {
//                    if (key.isReadable()) {
//                        request.flip();
//                        datagramChannel.receive(request);
//                        response.compact();
//                        response.put(hello);
//                        response.put(request);
//                    }
//                    if (key.isValid() && key.isWritable()) {
//                        response.flip();
//                        datagramChannel.send(response, datagramChannel.getLocalAddress());
//                    }
//                } finally {
//                    i.remove();
//                }
            }
        }
    }

    private void send(SelectionKey key) throws IOException {
        System.out.println("Sending");
        DatagramChannel datagramChannel1 = (DatagramChannel) key.channel();

        System.out.println(sendBuffer.toString());
        sendBuffer.flip();
        datagramChannel1.send(sendBuffer, datagramChannel1.getLocalAddress());
        sendBuffer.compact();
        key.cancel();
    }

    private void receive(SelectionKey key) throws IOException {
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        datagramChannel.receive(receiveBuffer);

        receiveBuffer.flip();
        String receiveData = receiveBuffer.toString();

        //String outputData = receiveData.substring(0,receiveData.lastIndexOf("\n")+1);
        System.out.println(receiveData);


//        if(outputData.equals("echo:byte\n\n")){
//            key.cancel();
//            datagramChannel.close();
//            System.out.println("关闭与服务器的连接");
//            selector.close();
//            System.exit(0);
//        }
//
//        ByteBuffer temp = charset.encode(outputData);
//        receiveBuffer.position(temp.limit());
//        receiveBuffer.compact();
    }

    @Override
    public void close() {
        System.out.println("Closing");
        try {
            selector.close();
        } catch (IOException e) {
            System.out.println("Failed to close selector");
        }
    }
}
