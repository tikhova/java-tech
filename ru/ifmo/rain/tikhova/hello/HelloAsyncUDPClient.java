package ru.ifmo.rain.tikhova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloAsyncUDPClient implements HelloClient {
    private Selector selector;

    public static void main(String[] args) {
        // Check correct usage
        final String USAGE = "Expected usage: HelloUDPClient [host or ip] [port] [prefix] [threads] [requests]";
        if (args == null || args.length != 5) {
            System.out.println(USAGE);
            return;
        }
        try {
            new HelloAsyncUDPClient().run(args[0], Integer.getInteger(args[1]), args[2],
                    Integer.getInteger(args[3]), Integer.getInteger(args[4]));
        } catch (NumberFormatException e) {
            System.out.println("Incorrect number: " + e.getMessage());
        }
    }

    @Override
    public void run(String name, int port, String prefix, int threads, int requests) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.out.println("Failed to open Selector");
            return;
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host : " + name);
            return;
        }

        final SocketAddress destination = new InetSocketAddress(address, port);
        IntStream.range(0, threads).forEach(i -> {
            try {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(destination);
                datagramChannel.register(selector, SelectionKey.OP_READ, i);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            run(selector, destination, prefix, requests);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run(final Selector selector, SocketAddress dest, String prefix, int requests) throws IOException {
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
                    performTask(key, dest, prefix, requests);
                }

//                if (key.isValid() && key.isWritable()) {
//                    receive(key);
//                    send(key);
//                }
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

    private void performTask(SelectionKey key, SocketAddress dest, String prefix, int requests) {
        String base = prefix + key.attachment() + "_";
        for (int i = 0; i != requests; ++i) {
            final String req = base + i;

            DatagramChannel datagramChannel1 = (DatagramChannel) key.channel();
            ByteBuffer request = ByteBuffer.allocate(1024);
            ByteBuffer response = ByteBuffer.allocate(1024);
            System.out.println(req);
            while (true) {
                // Make request
                try {
                    datagramChannel1.send(request, dest);
                } catch (IOException ignored) {
                    continue;
                }

                // Receive response
                try {
                    datagramChannel1.receive(response);
                } catch (IOException ignored) {
                    continue;
                }

                // check response
            }
        }
    }
}
