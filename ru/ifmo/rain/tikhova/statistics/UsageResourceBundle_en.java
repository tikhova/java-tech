package ru.ifmo.rain.tikhova.statistics;

import java.util.ListResourceBundle;

public class UsageResourceBundle_en extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            // Titles
            {"textFile", "Analysed file"},
            {"summary", "Summary statistics"},
            {"wordStat", "Words statistics"},
            {"sentenceStat", "Sentences statistics"},
            {"lineStat", "Lines statistics"},
            {"dateStat", "Dates statistics"},
            {"currencyStat", "Currency statistics"},
            {"numberStat", "Numbers statistics"},

            // Different word forms
            {"Number", "Number of"},
            {"number", "number"},
            {"numberGenitive", "number"},
            {"numbersGenitive", "numbers"},

            {"word","word"},
            {"wordGenitive", "word"},
            {"words","words"},
            {"wordsGenitive", "words"},

            {"sentence", "sentence"},
            {"sentenceGenitive", "sentence"},
            {"sentences", "sentences"},
            {"sentencesGenitive", "sentences"},

            {"line", "line"},
            {"lineGenitive", "line"},
            {"lines", "lines"},
            {"linesGenitive", "lines"},

            {"date", "date"},
            {"dateGenitive", "date"},
            {"datesGenitive", "dates"},

            {"currency", "money amount"},
            {"currencyGenitive", "money amount"},
            {"currenciesGenitive", "money amounts"},

            {"minNeuter", "Min"},
            {"minFeminine", "Min"},

            {"maxNeuter", "Max"},
            {"maxFeminine", "Max"},

            {"averageFeminine", "Average"},

            {"length", "length of a"},

            {"unique", "unique"}
    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}