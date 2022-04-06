package fr.umlv.loom.monad;

import fr.umlv.loom.monad.AsyncMonad.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AsyncMonadMain {
  static void withAListOfTasks() throws InterruptedException {
    var tasks = List.<Task<Integer, RuntimeException>>of(
        () -> {
          System.out.println(Thread.currentThread());
          Thread.sleep(500);
          return 500;
        },
        () -> {
          System.out.println(Thread.currentThread());
          Thread.sleep(100);
          return 100;
        });
    List<Integer> list;
    try(var asyncMonad = AsyncMonad.of(tasks)) {
      list = asyncMonad.result(Stream::toList);
    }
    System.out.println("withAListOfTasks" + list);
  }

  static void withASetOfTasks() throws InterruptedException {
    var tasks = Set.<Task<Integer, RuntimeException>>of(
        () -> {
          System.out.println(Thread.currentThread());
          Thread.sleep(500);
          return 500;
        },
        () -> {
          System.out.println(Thread.currentThread());
          Thread.sleep(100);
          return 100;
        });
    List<Integer> list;
    try(var asyncMonad = AsyncMonad.of(tasks)) {
      list = asyncMonad.result(Stream::toList);
    }
    System.out.println("withASetOfTasks " + list);
  }

  static void findAny() throws InterruptedException {
    int result;
    try(var asyncMonad = AsyncMonad.<Integer, RuntimeException>of(forker -> {
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(500);
        return 500;
      });
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(100);
        return 100;
      });
    })) {
      result = asyncMonad
          .unordered()
          .result(Stream::findAny)
          .orElseThrow();
    }
    System.out.println("findAny " + result);
  }

  static void withAnException() throws IOException, InterruptedException {
    int result;
    try(var asyncMonad = AsyncMonad.<Integer, IOException>of(forker -> {
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(500);
        return 500;
      });
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(100);
        throw new IOException("oups");
      });
    })) {
      result = asyncMonad.result(stream -> stream.mapToInt(v -> v).sum());
    }
    System.out.println("withAnException" + result);
  }

  static void recoverAnException() throws InterruptedException {
    int result;
    try(var asyncMonad = AsyncMonad.<Integer, IOException>of(forker -> {
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(500);
        return 500;
      });
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(100);
        throw new IOException("oups");
      });
    })) {
      result = asyncMonad
          .recover(exception -> 0)
          .result(stream -> stream.mapToInt(v -> v).sum());
    }
    System.out.println("recoverAnException " + result);
  }

  class MyException extends Exception {
    public MyException(Throwable cause) {
      super(cause);
    }
  }

  static void wrapAnException() throws InterruptedException, MyException {
    int result;
    try(var asyncMonad = AsyncMonad.<Integer, IOException>of(forker -> {
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(500);
        throw new IOException("oups");
      });
      forker.fork(() -> {
        System.out.println(Thread.currentThread());
        Thread.sleep(100);
        return 100;
      });
    })) {
      result = asyncMonad
          .recover(exception -> { throw new MyException(exception); })
          .result(stream -> stream.mapToInt(v -> v).sum());
    }
    System.out.println("recoverAnException " + result);
  }

  public static void main(String[] args) throws InterruptedException {
    withAListOfTasks();

    withASetOfTasks();

    findAny();

    try {
      withAnException();
    } catch (IOException e) {
      System.out.println("withAnException exception ! " + e.getMessage());
    }

    recoverAnException();

    try {
      wrapAnException();
    } catch (MyException e) {
      System.out.println("wrapAnException exception ! " + e.getMessage());
    }
  }
}