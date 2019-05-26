package ru.ifmo.rain.tikhova.statistics;

import net.java.quickcheck.collection.Pair;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

class DateStatistic extends Statistic {

    DateStatistic(Locale locale) {
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        comp = (Comparator<String>) (a, b) -> {
            try {
                return format.parse(a).compareTo(format.parse(b));
            } catch (ParseException e) {
                return 0;
            }
        };
    }

    DateStatistic(Locale locale, String text) {
        maxLength = new Pair<>(Integer.MIN_VALUE, "NULL");
        minLength = new Pair<>(Integer.MAX_VALUE, "NULL");

        // Get list of elements of the given format
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        List<Pair<Date, String>> list = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getWordInstance(locale);
        iterator.setText(text);
        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            String substring = text.substring(start, end);
            try {
                list.add(new Pair<>(format.parse(substring), substring));
            } catch (ParseException ignored) {
            }
        }

        // Analyse the list
        if (!list.isEmpty()) {
            count = list.size();
            minValue = list.stream().reduce((a, b) -> (a.getFirst().compareTo(b.getFirst()) < 0 ? a : b)).get().getSecond();
            maxValue = list.stream().reduce((a, b) -> (a.getFirst().compareTo(b.getFirst()) > 0 ? a : b)).get().getSecond();


            String minVal = list.stream().map(Pair::getSecond).min(Comparator.comparing(String::length)).get();
            minLength = new Pair<>(minVal.length(), minVal);

            String maxVal = list.stream().map(Pair::getSecond).max(Comparator.comparing(String::length)).get();
            maxLength = new Pair<>(maxVal.length(), maxVal);

            averageLength = list.stream().map(e -> e.getSecond().length()).reduce(Integer::sum).get() / count;
            uniqueCount = (int) list.stream().distinct().count();
        }
    }
}
