package ru.ifmo.rain.tikhova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> thrs = new ArrayList<>();
    private final Queue<Runnable> queue = new ArrayDeque<>();

    public ParallelMapperImpl(int threads) {
        assert (threads > 0);
        for (int i = 0; i < threads; ++i) {
            Thread t = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) performTask();
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            thrs.add(t);
            t.start();
        }
    }


    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        CountDownLatch doneSignal = new CountDownLatch(list.size());
        List<R> result = new ArrayList<>(Collections.nCopies(list.size(), null));

        for (int i = 0; i != list.size(); ++i) {
            final int ind = i;
            synchronized (queue) {
                queue.add(() -> {
                    result.set(ind, function.apply(list.get(ind)));
                    doneSignal.countDown();
                });
                queue.notify();
            }
        }
        doneSignal.await();
        return result;
    }

    @Override
    public void close() {
        for (Thread t : thrs) {
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void performTask() throws InterruptedException {
        Runnable task;
        synchronized (queue) {
            while (queue.isEmpty()) queue.wait();
            task = queue.poll();
        }
        task.run();
    }
}