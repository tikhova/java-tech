package ru.ifmo.rain.tikhova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class IterativeParallelism implements ListIP {

    ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        this.mapper = null;
    }

    private <T> List<List<? extends T>> getPartialLists(int threads, List<? extends T> list) {
        List<List<? extends T>> lists = new ArrayList<>();
        int list_size = list.size();
        int items_count = list_size / threads;
        int left_count = list_size % threads;

        int from;
        int to = 0;

        for (int i = 0; i != threads; ++i) {
            from = to;
            to = min(to + items_count + (left_count-- > 0 ? 1 : 0), list_size);
            lists.add(list.subList(from, to));
        }
        return lists;
    }

    private <T, R> R makeParallelComputations(int threads,
                                              List<? extends T> list,
                                              Function<List<? extends T>, R> function,
                                              Function<List<R>, R> collector) throws InterruptedException {
        threads = max(1, min(threads, list.size()));
        List<R> res;
        final List<List<? extends T>> lists = getPartialLists(threads, list);
        if (mapper != null) {
            res = mapper.map(function, lists);
        } else {
            res = new ArrayList<>(Collections.nCopies(threads, null));
            final List<Thread> thrs = new ArrayList<>();
            for (int i = 0; i != threads; ++i) {
                final int position = i;
                Thread thread = new Thread(() -> res.set(position, function.apply(lists.get(position))));
                thrs.add(thread);
                thread.start();
            }
            for (Thread thread : thrs) {
                thread.join();
            }
        }
        return collector.apply(res);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return makeParallelComputations(threads, list,
                x -> Collections.max(x, comparator),
                y -> Collections.max(y, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallelComputations(threads, list,
                l -> l.stream().allMatch(predicate),
                r -> r.stream().allMatch(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !(all(threads, list, predicate.negate()));
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        return makeParallelComputations(threads, list,
                l -> l.stream().map(Object::toString).collect(Collectors.joining()),
                r -> r.stream().collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallelComputations(threads, list,
                l -> l.stream().filter(predicate).collect(Collectors.toList()),
                r -> r.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return makeParallelComputations(threads, list,
                l -> l.stream().map(function).collect(Collectors.toList()),
                r -> r.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
