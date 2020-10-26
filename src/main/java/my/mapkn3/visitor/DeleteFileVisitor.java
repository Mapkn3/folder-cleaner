package my.mapkn3.visitor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteFileVisitor implements FileVisitor<Path> {
    private final Pattern mask;

    public DeleteFileVisitor(Pattern mask) {
        this.mask = mask;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        Matcher matcher = mask.matcher(fileName);
        System.out.printf("Check %s - ", file);
        if (matcher.matches()) {
            Files.delete(file);
            System.out.println("✓");
        } else {
            System.out.println("✕");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Objects.requireNonNull(file);
        if (exc.getClass().equals(AccessDeniedException.class)) {
            System.out.printf("Access denied exception: %s\n", file);
            return FileVisitResult.SKIP_SUBTREE;
        }
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
