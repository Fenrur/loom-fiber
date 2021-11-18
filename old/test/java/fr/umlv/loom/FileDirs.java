package fr.umlv.loom;

import static fr.umlv.loom.Task.async;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.walk;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class FileDirs {
  private static long lineCountFile(Path path) { 
    try(var lines = lines(path)) {
      return lines.count();
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  private static long lineCount(Path directory, Predicate<? super Path> filter, int limit) throws IOException {
    try(var files = walk(directory)) {
      return files
          .filter(not(Files::isDirectory))
          .filter(filter)
          .limit(limit)
          .map(path -> async(() -> lineCountFile(path)))
          .collect(toList())
          .stream()
          .mapToLong(Task::join)
          .sum();
    } catch(UncheckedIOException e) {
      throw e.getCause();
    }
  }
  
  @Test
  public void testLineCount() throws IOException {
    var lineCount = lineCount(Path.of("."), path -> path.toString().endsWith(".java"), 5);
    System.out.println("lineCount " + lineCount);
  }
}
