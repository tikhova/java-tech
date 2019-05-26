package ru.ifmo.rain.tikhova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private final ConcurrentMap<String, Counter> hosts = new ConcurrentHashMap<>();

    private class Counter {
        private int count = 0;

        boolean available() {
            return count < perHost;
        }

        void increment() {
            ++count;
        }

        void decrement() {
            --count;
            this.notify();
        }
    }

    /**
     * @param downloader  allows to load pages and extract links
     * @param downloaders is maximum of simultaneously loaded pages
     * @param extractors  is maximum of pages to simultaneously extract links from
     * @param perHost     is maximum of pages to be simultaneously loaded from the same host
     *                    To define host method getHost of class URLUtils should be used
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;

    }

    private void addLinks(final Document page, final int depth, final Set<String> downloaded,
                          final ConcurrentMap<String, IOException> errors,
                          final Phaser phaser, final Set<String> added) {
        try {
            page.extractLinks().stream().filter(added::add).
                    forEach(link -> {
                        phaser.register();
                        downloaders.submit(() -> add(link, depth, downloaded, errors, phaser, added));
                    });

        } catch (IOException ignored) {
        } finally {
            phaser.arrive();
        }
    }

    private void add(final String url, final int depth, final Set<String> downloaded,
                     final ConcurrentMap<String, IOException> errors,
                     final Phaser phaser, final Set<String> added) {

        final String host;
        try {
            host = URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return;
        }
        synchronized (hosts) {
            if (!hosts.containsKey(host)) {
                hosts.put(host, new Counter());
            }
        }

        try {
            synchronized (hosts.get(host)) {
                while (!hosts.get(host).available()) {
                    try {
                        hosts.get(host).wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                hosts.get(host).increment();
            }
            final Document page = downloader.download(url);
            downloaded.add(url);
            if (depth > 1) {
                phaser.register();
                extractors.submit(() -> addLinks(page, depth - 1, downloaded, errors, phaser, added));
            }
        } catch (IOException e) {
            errors.put(url, e);
        } finally {
            synchronized (hosts.get(host)) {
                //System.out.println("host entered");
                hosts.get(host).decrement();
            }
            phaser.arrive();
        }


    }


    @Override
    public Result download(String s, int i) {
        final Set<String> added = ConcurrentHashMap.newKeySet();
        final Set<String> downloaded = ConcurrentHashMap.newKeySet();
        final ConcurrentHashMap<String, java.io.IOException> errors = new ConcurrentHashMap<>();
        final Phaser phaser = new Phaser(1);
        added.add(s);
        phaser.register();
        downloaders.submit(() -> add(s, i, downloaded, errors, phaser, added));
        phaser.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    // usage: WebCrawler url [depth [downloads [extractors [perHost]]]]
    public static void main(String[] args) {
        // Check correct usage
        if (args == null || args.length == 0) {
            System.out.println("Expected usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Arguments should not be null");
            return;
        }
        int[] arguments = new int[4];
        for (int i = 1; i < 5; ++i) {
            try {
                arguments[i - 1] = i < args.length ? Integer.parseInt(args[i]) : 1;
            } catch (NumberFormatException e) {
                System.out.println(args[i] + " is not a correct number");
                return;
            }
        }

        // Download
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), arguments[1], arguments[2], arguments[3])) {
            crawler.download(args[0], arguments[0]);
        } catch (IOException e) {
            System.out.println("Unable to create instance of CachingDownloader: " + e.getMessage());
        }
    }
}
