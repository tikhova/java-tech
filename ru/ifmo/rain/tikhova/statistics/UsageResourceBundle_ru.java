package ru.ifmo.rain.tikhova.statistics;

import java.util.ListResourceBundle;

public class UsageResourceBundle_ru extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            // Titles
            {"textFile", "Анализируемый файл"},
            {"summary", "Сводная статистика"},
            {"wordStat", "Статистика по словам"},
            {"sentenceStat", "Статистика по предложениям"},
            {"lineStat", "Статистика по строкам"},
            {"dateStat", "Статистика по датам"},
            {"currencyStat", "Статистика по валюте"},
            {"numberStat", "Статистика по числам"},

            // Different word forms
            {"Number", "Число"},
            {"number", "число"},
            {"numberGenitive", "числа"},
            {"numbersGenitive", "чисел"},

            {"word","слово"},
            {"wordGenitive", "слова"},
            {"words","слова"},
            {"wordsGenitive", "слов"},

            {"sentence", "предложение"},
            {"sentenceGenitive", "предложения"},
            {"sentences", "предложения"},
            {"sentencesGenitive", "предложений"},

            {"line", "строка"},
            {"lineGenitive", "строки"},
            {"lines", "строки"},
            {"linesGenitive", "строк"},

            {"date", "дата"},
            {"dateGenitive", "даты"},
            {"datesGenitive", "дат"},

            {"currency", "сумма денег"},
            {"currencyGenitive", "суммы денег"},
            {"currenciesGenitive", "сумм денег"},

            {"minNeuter", "Минимальное"},
            {"minFeminine", "Минимальная"},

            {"maxNeuter", "Максимальное"},
            {"maxFeminine", "Mаксимальная"},

            {"averageFeminine", "Средняя"},

            {"length", "длина"},

            {"unique", "уникальных"}
    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}