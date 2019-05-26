package ru.ifmo.rain.tikhova.statistics;

import net.java.quickcheck.collection.Pair;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.Locale;

class StringStatistic extends Statistic {

    StringStatistic(Locale locale) {
        comp = Collator.getInstance(locale);
    }

    StringStatistic(BreakIterator iterator, Locale locale, String text) {
        comp = Collator.getInstance(locale);
        iterator.setText(text);

        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            String substring = text.substring(start, end);
            if (substring.isEmpty()) continue;
            count++;
            averageLength += substring.length();

            unique.add(substring);

            if (minLength == null || substring.length() < minLength.getFirst()) {
                minLength = new Pair<>(substring.length(), substring);
            }

            if (maxLength == null || substring.length() > maxLength.getFirst()) {
                maxLength = new Pair<>(substring.length(), substring);
            }

            if (minValue == null || ((Collator) comp).compare(substring, minValue) < 0) {
                minValue = substring;
            }

            if (maxValue == null || ((Collator) comp).compare(substring, maxValue) > 0) {
                maxValue = substring;
            }


        }

        uniqueCount = unique.size();
        averageLength /= count;
    }
}
