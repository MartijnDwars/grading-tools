package nl.tudelft.in4303.grading.langxmlgen;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class Main {
    private static String nl = System.getProperty("line.separator");


    public static void main(String[] args) throws IOException {
        final String baseDirectoryString = args[0];
        final Path baseDirectory = Paths.get(baseDirectoryString);

        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(nl);
        sb.append("<report name=\"\">");
        sb.append(nl);
        sb.append("<language esv=\"compiled/analysis-correct/include/MiniJava.packed.esv\" />");
        sb.append(nl);
        sb.append("<group name=\"Test Suite Efficiency\">");
        sb.append(nl);

        Files.walkFileTree(baseDirectory, new FileVisitor<Path>() {
            private boolean inCompiled = false;

            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final String name = dir.getFileName().toString();
                if(name.equals("compiled"))
                    inCompiled = true;

                if(inCompiled)
                    return FileVisitResult.CONTINUE;

                sb.append("<group name=\"" + capitalizeFirstLetter(name) + "\">");
                sb.append(nl);

                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(file.toString().toLowerCase().endsWith(".packed.esv")) {
                    final Path relative = baseDirectory.relativize(file);
                    sb.append("<language esv=\"trans/analysis-errors/errors/" + relative.toString()
                        + "\" description=\"\" points=\"1.0\" />");
                    sb.append(nl);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                final String name = dir.getFileName().toString();
                if(name.equals("compiled")) {
                    inCompiled = false;
                    return FileVisitResult.CONTINUE;
                }

                if(inCompiled)
                    return FileVisitResult.CONTINUE;

                sb.append("</group>");
                sb.append(nl);
                return FileVisitResult.CONTINUE;
            }
        });

        sb.append("</group>");
        sb.append(nl);
        sb.append("</report>");
        sb.append(nl);

        final String output = sb.toString();
        System.out.println(output);
    }

    private static String capitalizeFirstLetter(String original) {
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
