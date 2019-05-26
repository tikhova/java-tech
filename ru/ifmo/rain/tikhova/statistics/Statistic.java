package ru.ifmo.rain.tikhova.statistics;

import net.java.quickcheck.collection.Pair;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

class Statistic {
    int count = 0;
    int uniqueCount = 0;
    String maxValue = null;
    String minValue = null;
    Pair<Integer, String> maxLength = null;
    Pair<Integer, String> minLength = null;
    double averageLength = 0;
    protected Set<String> unique = new HashSet<>();
    Comparator comp;
}
