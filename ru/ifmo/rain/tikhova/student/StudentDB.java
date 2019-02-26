package ru.ifmo.rain.tikhova.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {
    private List<String> getStringList(List<Student> students, Function<Student, String> mapper) {
        return students.stream().map(mapper).collect(Collectors.toList());
    }

    private List<Student> findStudentList(Collection<Student> students, Predicate<Student> filter) {
        return students.stream().filter(filter).collect(Collectors.toList());
    }

    // StudentQuery
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStringList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStringList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getStringList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStringList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).
                thenComparing(Student::getId));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortStudentsByName(findStudentList(students, student -> name.equals(student.getFirstName())));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return sortStudentsByName(findStudentList(students, student -> name.equals(student.getLastName())));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return sortStudentsByName(findStudentList(students, student -> group.equals(student.getGroup())));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup(students, group).stream().
                collect(Collectors.toMap(Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())));

    }

    private Stream<Map.Entry<String, List<Student>>> getGroupStream(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, Collectors.toList())).entrySet().stream();
    }

    private Stream<Map.Entry<String, List<Student>>> getSortedGroupStream(Collection<Student> students) {
        return getGroupStream(students).sorted(Comparator.comparing(Map.Entry::getKey));
    }

    private List<Group> getSortedGroupList(Collection<Student> students, UnaryOperator<List<Student>> studentSort) {
        return getSortedGroupStream(students).
                map(group -> new Group(group.getKey(), studentSort.apply(group.getValue()))).
                collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroupList(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroupList(students, this::sortStudentsById);
    }


    private String getLargestGroupName(Collection<Student> students, Comparator<Map.Entry<String, List<Student>>> comparator) {
        return getGroupStream(students).max(comparator.thenComparing(Map.Entry::getKey, Comparator.reverseOrder())).
                map(Map.Entry::getKey).orElse("");
    }

    /**
     * Returns name of the group containing maximum number of students.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupName(students,
                Comparator.comparingInt((Map.Entry<String, List<Student>> group) -> group.getValue().size()));
    }

    /**
     * Returns name of the group containing maximum number of students with distinct first names.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupName(students,
                Comparator.comparingInt((Map.Entry<String, List<Student>> group) -> getDistinctFirstNames(group.getValue()).size()));
    }

}