package ru.ifmo.rain.tikhova.statistics;

import net.java.quickcheck.collection.Pair;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

class NumberStatistic extends Statistic {
    NumberStatistic(NumberFormat format) {
        comp = (Comparator<String>) (a, b) -> {
            try {
                return Double.compare(format.parse(a).doubleValue(), format.parse(b).doubleValue());
            } catch (ParseException e) {
                return 0;
            }
        };
    }

    NumberStatistic(NumberFormat format, Locale locale, String text) {
        maxLength = new Pair<>(Integer.MIN_VALUE, "NULL");
        minLength = new Pair<>(Integer.MAX_VALUE, "NULL");

        // Get list of elements of the given format
        List<Pair<Double, String>> list = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getWordInstance(locale);
        iterator.setText(text);
        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            String substring = text.substring(start, end);
            try {
                Number p = format.parse(substring);
                list.add(new Pair<>(p.doubleValue(), substring));
            } catch (ParseException ignored) {
            }
        }

        // Analyse the list
        if (!list.isEmpty()) {
            count = list.size();
            minValue = list.stream().reduce((a, b) -> (a.getFirst() < b.getFirst()) ? a : b).get().getSecond();
            maxValue = list.stream().reduce((a, b) -> (a.getFirst() > b.getFirst()) ? a : b).get().getSecond();


            String minVal = list.stream().map(Pair::getSecond).min(Comparator.comparing(String::length)).orElse(null);
            assert minVal != null;
            minLength = new Pair<>(minVal.length(), minVal);

            String maxVal = list.stream().map(Pair::getSecond).max(Comparator.comparing(String::length)).orElse(null);
            assert maxVal != null;
            maxLength = new Pair<>(maxVal.length(), maxVal);

            averageLength = list.stream().map(e -> e.getSecond().length()).reduce(Integer::sum).orElse(0) / count;
            unique = list.stream().map(x -> String.valueOf(x.getFirst())).collect(Collectors.toSet());
            uniqueCount = unique.size();
        }
    }
}
