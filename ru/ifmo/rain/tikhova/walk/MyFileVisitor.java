package ru.ifmo.rain.tikhova.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor extends SimpleFileVisitor<Path> {
    BufferedWriter out;

    MyFileVisitor(BufferedWriter writer) {
        out = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        RecursiveWalk.printResult(file.toString(), out, RecursiveWalk.FNV1Hash(file.toString()));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        RecursiveWalk.printResult(file.toString(), out, 0);
        return FileVisitResult.SKIP_SUBTREE;
    }
}