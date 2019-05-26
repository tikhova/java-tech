package ru.ifmo.rain.tikhova.statistics;

import java.io.*;
import java.text.BreakIterator;
import java.text.NumberFormat;
import java.util.*;

public class TextStatistics {
    public static void main(String[] args) {
        // Check usage
        if (args.length != 4) {
            System.out.println("Invalid number of arguments - " + args.length);
            System.out.println("TextStatistics <text locale> <output locale> <text file> <report file>");
            return;
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Arguments should not be null");
        }

        // Setup
        Locale textLocale;
        Locale outputLocale;

        try {
            textLocale = Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.getISO3Language().equals(args[0])).findFirst().get();
            outputLocale = Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.getISO3Language().equals(args[1])).findFirst().get();
        } catch (NoSuchElementException e) {
            System.out.println("Please choose from the following locales:");
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::getISO3Language).distinct().forEach(System.out::println);
            return;
        }

        ResourceBundle bundle;
        switch (outputLocale.getLanguage()) {
            case "en":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.tikhova.statistics.UsageResourceBundle_en");
                break;
            case "ru":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.tikhova.statistics.UsageResourceBundle_ru");
                break;
            default:
                System.err.println("Unsupported output locale: only Russian and English locale supported");
                return;
        }

        // Input and calculations

        Statistic sentenceStat;
        Statistic lineStat;
        Statistic wordStat;

        Statistic numberStat;
        Statistic currencyStat;
        Statistic dateStat;

        try (InputStream in = new FileInputStream(args[2])) {
            String text = new String(in.readAllBytes());
            sentenceStat = new StringStatistic(BreakIterator.getSentenceInstance(textLocale), textLocale, text);
            lineStat = new StringStatistic(BreakIterator.getLineInstance(textLocale), textLocale, text);
            wordStat = new StringStatistic(BreakIterator.getWordInstance(textLocale), textLocale, text);

            numberStat = new NumberStatistic(NumberFormat.getNumberInstance(textLocale), textLocale, text);
            currencyStat = new NumberStatistic(NumberFormat.getCurrencyInstance(textLocale), textLocale, text);
            dateStat = new DateStatistic(textLocale, text);
        } catch (IOException e) {
            System.out.println("Error occurred while reading file: " + e.getMessage());
            return;
        }

        // Output

        String out = String.format("<html>\n" +
                        " <head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <title>Text statistic</title>\n" +
                        " </head>\n" +
                        " <body>\n" +
                        "\n" +
                        "  <h1>%s: %s</h1>\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n</p>" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +
                        " </body>\n" +
                        "</html>",
                // Заголовок
                bundle.getString("textFile"),
                args[2],

                // Сводная статистика
                bundle.getString("summary"),

                bundle.getString("Number"),
                bundle.getString("sentencesGenitive"),
                sentenceStat.count,

                bundle.getString("Number"),
                bundle.getString("wordsGenitive"),
                wordStat.count,

                bundle.getString("Number"),
                bundle.getString("linesGenitive"),
                lineStat.count,

                bundle.getString("Number"),
                bundle.getString("datesGenitive"),
                dateStat.count,

                bundle.getString("Number"),
                bundle.getString("numbersGenitive"),
                numberStat.count,

                bundle.getString("Number"),
                bundle.getString("currenciesGenitive"),
                currencyStat.count,


                // Статистика по предложениям
                bundle.getString("sentenceStat"),

                bundle.getString("Number"),
                bundle.getString("sentencesGenitive"),
                sentenceStat.count,
                sentenceStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minNeuter"),
                bundle.getString("sentence"),
                sentenceStat.minValue,

                bundle.getString("maxNeuter"),
                bundle.getString("sentence"),
                sentenceStat.maxValue,

                bundle.getString("minFeminine"),
                bundle.getString("length"),
                bundle.getString("sentenceGenitive"),
                sentenceStat.minLength.getFirst(),
                sentenceStat.minLength.getSecond(),

                bundle.getString("maxFeminine"),
                bundle.getString("length"),
                bundle.getString("sentenceGenitive"),
                sentenceStat.maxLength.getFirst(),
                sentenceStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("sentenceGenitive"),
                (int) sentenceStat.averageLength,

                // Статистика по словам
                bundle.getString("wordStat"),

                bundle.getString("Number"),
                bundle.getString("wordsGenitive"),
                wordStat.count,
                wordStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minNeuter"),
                bundle.getString("word"),
                wordStat.minValue,

                bundle.getString("maxNeuter"),
                bundle.getString("word"),
                wordStat.maxValue,

                bundle.getString("minFeminine"),
                bundle.getString("length"),
                bundle.getString("wordGenitive"),
                wordStat.minLength.getFirst(),
                wordStat.minLength.getSecond(),

                bundle.getString("maxFeminine"),
                bundle.getString("length"),
                bundle.getString("wordGenitive"),
                wordStat.maxLength.getFirst(),
                wordStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("wordGenitive"),
                (int) wordStat.averageLength,

                // Статистика по строкам
                bundle.getString("lineStat"),

                bundle.getString("Number"),
                bundle.getString("linesGenitive"),
                lineStat.count,
                lineStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minFeminine"),
                bundle.getString("line"),
                lineStat.minValue,

                bundle.getString("maxFeminine"),
                bundle.getString("line"),
                lineStat.maxValue,

                bundle.getString("minFeminine"),
                bundle.getString("length"),
                bundle.getString("lineGenitive"),
                lineStat.minLength.getFirst(),
                lineStat.minLength.getSecond(),

                bundle.getString("maxFeminine"),
                bundle.getString("length"),
                bundle.getString("lineGenitive"),
                lineStat.maxLength.getFirst(),
                lineStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("lineGenitive"),
                (int) lineStat.averageLength,

                // Статистика по числам
                bundle.getString("numberStat"),

                bundle.getString("Number"),
                bundle.getString("numbersGenitive"),
                numberStat.count,
                numberStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minNeuter"),
                bundle.getString("number"),
                numberStat.minValue,

                bundle.getString("maxNeuter"),
                bundle.getString("number"),
                numberStat.maxValue,

                bundle.getString("minNeuter"),
                bundle.getString("length"),
                bundle.getString("numberGenitive"),
                numberStat.minLength.getFirst(),
                numberStat.minLength.getSecond(),

                bundle.getString("maxNeuter"),
                bundle.getString("length"),
                bundle.getString("numberGenitive"),
                numberStat.maxLength.getFirst(),
                numberStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("numberGenitive"),
                (int) numberStat.averageLength,

                // Статистика по деньгам
                bundle.getString("currencyStat"),

                bundle.getString("Number"),
                bundle.getString("currenciesGenitive"),
                currencyStat.count,
                currencyStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minFeminine"),
                bundle.getString("currency"),
                currencyStat.minValue,

                bundle.getString("maxFeminine"),
                bundle.getString("currency"),
                currencyStat.maxValue,

                bundle.getString("minFeminine"),
                bundle.getString("length"),
                bundle.getString("currencyGenitive"),
                currencyStat.minLength.getFirst(),
                currencyStat.minLength.getSecond(),

                bundle.getString("maxFeminine"),
                bundle.getString("length"),
                bundle.getString("currencyGenitive"),
                currencyStat.maxLength.getFirst(),
                currencyStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("currencyGenitive"),
                (int) currencyStat.averageLength,

                // Статистика по датам
                bundle.getString("dateStat"),

                bundle.getString("Number"),
                bundle.getString("datesGenitive"),
                dateStat.count,
                dateStat.uniqueCount,
                bundle.getString("unique"),

                bundle.getString("minFeminine"),
                bundle.getString("date"),
                dateStat.minValue,

                bundle.getString("maxFeminine"),
                bundle.getString("date"),
                dateStat.maxValue,

                bundle.getString("minFeminine"),
                bundle.getString("length"),
                bundle.getString("dateGenitive"),
                dateStat.minLength.getFirst(),
                dateStat.minLength.getSecond(),

                bundle.getString("maxFeminine"),
                bundle.getString("length"),
                bundle.getString("dateGenitive"),
                dateStat.maxLength.getFirst(),
                dateStat.maxLength.getSecond(),

                bundle.getString("averageFeminine"),
                bundle.getString("length"),
                bundle.getString("dateGenitive"),
                (int) dateStat.averageLength
        );

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
            writer.write(out);
            writer.close();
        } catch (IOException ignored) {
        }
    }
}
