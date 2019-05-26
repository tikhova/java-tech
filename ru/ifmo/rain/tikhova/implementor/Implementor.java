package ru.ifmo.rain.tikhova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Class implementing {@link JarImpler} interface
 * @version 1.0
 * @author Tikhova Mariya
 */
public class Implementor implements JarImpler {
    private Class<?> token;
    private Path root;
    private Path file;
    // Strings:
    /**
     * {@link String} Generated class name
     */
    private String implName;
    /**
     * {@link String} "Impl"
     */
    private String SUFFIX = "Impl";
    /**
     * {@link String} ".java"
     */
    private String JAVA_EXT = ".java";
    /**
     * {@link String} ".class"
     */
    private String CLASS_EXT = ".class";
    /**
     * {@link String} tabulation consisting of 4 spaces
     */
    private String TAB = "    ";
    /**
     * {@link String} files separator
     */
    private char SEPARATOR = File.separatorChar;
    /**
     * {@link String} line separator
     */
    private String NEWLINE = System.lineSeparator();

    /**
     * Produces code implementing class or interface specified by provided token.
     * <p>
     * Generated class classes name should be same as classes name of the type token with Impl suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * root directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to $root/java/util/ListImpl.java
     *
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException when implementation cannot be
     * generated.
     */
    public void implement(Class<?> token, Path root) throws ImplerException {
        this.token = token;
        this.root = root;
        implName = token.getSimpleName() + SUFFIX;
        checkExtentable();
        createFile();
        try (BufferedWriter out = Files.newBufferedWriter(file)) {
            out.write(getPackages());
            out.write(getHeader());
            if (!token.isInterface())
                out.write(getConstructors());
            out.write(getMethods());
            out.write("}");
        } catch (IOException e) {
            throw new ImplerException("Failed to write in file");
        }
    }

    // Methods used in implement directly

    /**
     * @throws ImplerException if {@link this#token} cannot be extended
     */
    private void checkExtentable() throws ImplerException {
        if (token.isPrimitive()) throw new ImplerException("Can't extend primitive");
        if (token.isArray()) throw new ImplerException("Can't extend array");
        if (Modifier.isFinal(token.getModifiers())) throw new ImplerException("Can't extend final class");
        if (token.equals(java.lang.Enum.class)) throw new ImplerException("Can't extend enum directly");
        if (token.isEnum()) throw new ImplerException("Can't extend enum");
    }

    /**
     * Resolves path to the generated class file and creates all non-existent directories in it
     * @throws ImplerException if {@link Files#createDirectories(Path, FileAttribute[])} throws IOException
     */
    private void createFile() throws ImplerException {
        String path = token.getCanonicalName().replace('.', SEPARATOR) + SUFFIX + JAVA_EXT;
        file = root.resolve(path).toAbsolutePath();
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
        } catch (IOException e) {
            throw new ImplerException("Failed to create directories");
        }
    }

    /**
     * Generates the package line of the generated class
     * @return {@link String} representing the package of implemented class
     */
    private String getPackages() {
        if (token.getPackage() != null) {
            return "package " + token.getPackageName() + ";" + NEWLINE;
        }
        return "";
    }

    /**
     * Generates the header of the following form ["list of modifiers" class {@link this#implName} {]
     * @return {@link String} representing the package of implemented class
     */
    private String getHeader() {
        return Modifier.toString(token.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.INTERFACE) + // modifiers
                " class " + implName + // class <name>
                (token.isInterface() ? " implements " : " extends ") + token.getCanonicalName() + " {" + NEWLINE;
    }

    /**
     * Generates the code of non-private constructors by calling {@link this#getExecutable(Executable)} method for each one
     * @return {@link String} concatenation of all the constructors
     * @throws ImplerException if there are no constructors to implement
     */
    private String getConstructors() throws ImplerException {
        Constructor[] constructors = Arrays.stream(token.getDeclaredConstructors()).
                filter(constructor -> !Modifier.isPrivate(constructor.getModifiers())).toArray(Constructor[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("No constructors in class");
        }
        StringBuilder res = new StringBuilder();
        for (Constructor constructor : constructors) {
            res.append(getExecutable(constructor));
        }
        return res.toString();
    }

    /**
     * Generates the code of non-abstract methods by calling {@link this#getExecutable(Executable)} method for each one
     * @return {@link String} concatenation of all the methods
     */
    private String getMethods() {
        Set<Method> methods = new HashSet<>();
        Arrays.stream(token.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toCollection(() -> methods));
        Arrays.stream(token.getDeclaredMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toCollection(() -> methods));
        StringBuilder res = new StringBuilder();
        for (Method method : methods) {
            res.append(getExecutable(method));
        }
        return res.toString();
    }

    // Inner logic

    /**
     * Generates the code of executable by calling {@link this#printExecutableHeader(Executable)} and
     * {@link this#getBody(Executable)}
     * @return {@link String} representation of the executable
     */
    private String getExecutable(Executable executable) {
        StringBuilder res = new StringBuilder();
        String mods = Modifier.toString(executable.getModifiers() & ~Modifier.NATIVE & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
        res.append(TAB + mods + ((mods.length() > 0) ? " " : ""));
        res.append(printExecutableHeader(executable));
        res.append(TAB + TAB);
        res.append(getBody(executable));
        res.append(NEWLINE + TAB + "}" + NEWLINE);
        return res.toString();
    }

    /**
     * Generates the header of the following form ["return type" "executable name" ("parameters list") "exceptions" {]
     * @return {@link String} representation of the header
     */
    private String printExecutableHeader(Executable executable) {
        StringBuilder res = new StringBuilder();
        if (executable instanceof Method) {
            res.append(((Method) executable).getReturnType().getCanonicalName()); // <return type>
            res.append(" " + executable.getName()); // <name>
        } else {
            res.append(((Constructor) executable).getDeclaringClass().getSimpleName() + SUFFIX);
        }
        res.append(getParameters(executable, true)); // <parameters>
        res.append(getExceptions(executable)); // <exceptions>
        res.append(" {" + NEWLINE);
        return res.toString();
    }

    /**
     * Returns the list of parameters of executable in brackets separated by commas.
     * Uses {@link this#getParameter(Parameter, boolean)} to get elements of the list
     * @param executable
     * @param useTypename indicates if the list should include typenames of the parameters
     * @return {@link String} representation of the list
     */
    private String getParameters(Executable executable, boolean useTypename) {
        return Arrays.stream(executable.getParameters()).map(param -> getParameter(param, useTypename))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Returns parameter's name if useTypename is false.
     * Returns [parameter's type] [parameter's name] if useTypename is true.
     * @param parameter
     * @param useTypename indicates if the return should include typename of the parameter
     * @return {@link String} representation of the parameter
     */
    private static String getParameter(Parameter parameter, boolean useTypename) {
        return (useTypename ? parameter.getType().getCanonicalName() + " " : "") + parameter.getName();
    }

    /**
     * Returns the list of exceptions thrown by this executable.
     * @param executable
     * @return {@link String} representation of the list of the executable's exceptions
     */
    private String getExceptions(Executable executable) {
        Class[] exceptions = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            return " throws " + Arrays.stream(exceptions).map(Class::getCanonicalName).
                    collect(Collectors.joining(", "));
        }
        return "";
    }

    /**
     * Calls the super function or returns default value depending on the executable.
     * @param executable
     * @return {@link String} representation of the executable's body
     */
    private String getBody(Executable executable) {
        if (executable instanceof Method) {
            return "return " + getReturnValue(((Method) executable).getReturnType()) + ";";
        } else {
            return "super" + getParameters(executable, false) + ";";
        }
    }

    /**
     * Returns the default value of the given class
     * @param token is the given class
     * @return {@link String} representation of the default value
     */
    private static String getReturnValue(Class token) {
        if (token.equals(boolean.class)) return "false";
        if (token.equals(void.class)) return "";
        if (token.isPrimitive()) return "0";
        return "null";
    }
    
    /**
     * Produces .jar file implementing class or interface specified by provided token.
     * <p>
     * Generated class classes name should be same as classes name of the type token with Impl suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param jarFile target .jar file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Null arguments");
        }

        // Create directories
        if (jarFile.getParent() != null) {
            try {
                Files.createDirectories(jarFile.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }

        Path tmpdir;
        try {
            tmpdir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
        } catch (IOException e) {
            throw new ImplerException("Failed to create a temporary directory", e);
        }

        try {
            // Implement class
            implement(token, tmpdir);


            // Compile class
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("Can not find java compiler");
            }
            String f = new File(token.getProtectionDomain().getCodeSource().getLocation().getPath()).toString();
            System.out.println(f);

            String[] args = new String[]{
                    "-cp",
                    f + File.pathSeparator + System.getProperty("java.class.path"),
                    file.toString(),
                    "-encoding", "cp866"
            };


            if (compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Failed to compile generated java classes");
            }

            // Create jar
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

            try (JarOutputStream jarWriter = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                jarWriter.putNextEntry(
                        new ZipEntry(token.getCanonicalName().replace('.', SEPARATOR) + SUFFIX + CLASS_EXT));
                Files.copy(file.getParent().resolve(implName + CLASS_EXT), jarWriter);
            } catch (IOException e) {
                throw new ImplerException("Failed to write jar", e);
            }

        } finally {
            // Delete temporary directory
            try {
                Files.walk(tmpdir)
                        .map(Path::toFile)
                        .sorted(Comparator.reverseOrder())
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new ImplerException("Failed deleting temporary files in " + tmpdir.toString());
            }
        }
    }

    /**
     * Main function of the program
     *
     * @param args ["-jar" "class_name" "jar_name"] to create jar file implementation
     *             ["class_name", "path"] to create java file
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.out.println("Expected usage: -jar <classname> <filename>.jar");
            return;
        }

        for (String arg : args) {
            if (arg == null) {
                System.out.println("Arguments should not be null");
                return;
            }
        }
        Implementor jarImplementor = new Implementor();

        try {
            if (args.length == 2) {
                try {
                    jarImplementor.implement(Class.forName(args[0]), Paths.get(args[1]));
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found :" + args[0]);
                }
            } else if (args.length == 3 && "-jar".equals(args[0])) {
                try {
                    jarImplementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found :" + args[1]);
                }
            } else {
                System.out.println("Expected usage: -jar <classname> <filename>.jar");
            }
        } catch (InvalidPathException e) {
            System.out.println("Invalid path given");
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}
