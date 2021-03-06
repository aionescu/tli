package com.aionescu.tli.view.tui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.BiConsumer;

import com.aionescu.tli.ast.prog.GlobalState;
import com.aionescu.tli.controller.Controller;
import com.aionescu.tli.exn.eval.EvalException;
import com.aionescu.tli.exn.typeck.TypeCheckerException;
import com.aionescu.tli.parser.TLParser;
import com.aionescu.tli.utils.Pair;
import com.aionescu.tli.utils.data.list.List;
import com.aionescu.tli.utils.data.map.Map;
import com.aionescu.tli.utils.control.Maybe;
import com.aionescu.tli.utils.uparsec.exn.UParsecException;
import com.aionescu.tli.view.View;

public final class TUIView implements View {
  private final Controller _controller;
  private static final Map<String, Pair<String, BiConsumer<TUIView, String>>> _commands = _getCommands();

  public TUIView(Controller controller) {
    _controller = controller;
  }

  private static BiConsumer<TUIView, String> _toLambda(Method m) {
    return (view, arg) -> {
      try {
        m.invoke(view, arg);
      } catch (InvocationTargetException e) {
        var cause = e.getCause();

        if (cause instanceof RuntimeException)
          throw (RuntimeException)cause;
        else
          cause.printStackTrace();
      } catch (IllegalAccessException | IllegalArgumentException e) {
        e.printStackTrace();
      }
    };
  }

  private static Map<String, Pair<String, BiConsumer<TUIView, String>>> _getCommands() {
    var ann = Command.class;

    return
      Map.fromList(
        List.ofStream(
          Arrays.<Method>stream(
            TUIView.class.getDeclaredMethods())
          .filter(m -> m.isAnnotationPresent(ann))
          .map(m -> {
            var c = m.getAnnotation(ann);
            return Pair.of(c.name(), Pair.of(c.desc(), _toLambda(m)));
          })));
  }

  @Command(name = "exit", desc = "Exits the interactive interpreter.")
  private void _exit(String arg) {
    System.exit(0);
  }

  @Command(name = "help", desc = "Displays a list of available commands.")
  private void _showHelp(String arg) {
    System.out.println("Available commands:");
    _commands.toList().iter(kvp -> System.out.println(kvp.fst + ": " + kvp.snd.fst));

    System.out.println();
  }

  @Command(name = "load", desc = "Parses, typechecks and loads the specified TL file.")
  private void _loadCode(String arg) {
    try {
      var code = Files.readString(Path.of(arg));
      var ast = TLParser.parse(code);
      ast.typeCheck(Map.empty());

      var global = GlobalState.initial(ast);

      _controller.setState(global);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void _runSmallStep() {
    System.out.println(_controller.state());

    while (!_controller.done()) {
      _controller.oneStep();
      System.out.println(_controller.state().get());
    }
  }

  private void _runBigStep() {
    while (!_controller.done())
      _controller.oneStep();

    System.out.println(_controller.state().get().output());
  }

  @Command(name = "run", desc = "Runs the loaded program. If --small-step is passed, displays all intermediate states.")
  private void _run(String arg) {
    if (arg.equals("--small-step"))
      _runSmallStep();
    else
      _runBigStep();
  }

  @Command(name = "show-state", desc = "Shows the AST of the loaded program.")
  private void _showState(String arg) {
    System.out.println(_controller.state().get());
  }

  @Command(name = "parse", desc = "Reads a program from its argument, then parses, typechecks, and loads it.")
  private void _parseStdin(String arg) {
    var ast = TLParser.parse(arg);
    ast.typeCheck(Map.empty());

    var prog = GlobalState.initial(ast);
    _controller.setState(prog);
  }

  @Command(name = "set-log-file", desc = "Sets the log file to the specified path.")
  private void _SetLogPath(String arg) {
    _controller.setLogPath(Maybe.just(arg));
  }

  @Command(name = "unset-log-file", desc = "Unsets the log file, causing the repository to stop logging.")
  private void _unsetLogPath(String arg) {
    _controller.setLogPath(Maybe.nothing());
  }

  @Command(name = "set-gc-threshold", desc = "Sets the amount of allocations to perform before a collection occurs.")
  private void _setGCThreshold(String arg) {
    var gcThreshold = Integer.parseInt(arg);
    _controller.state().getAndUpdate(s -> s.withGCStats(s.gcStats.withGCThreshold(gcThreshold)));;
  }

  @Command(name = "set-max-heap-size", desc = "Sets the maximum size of the heap.")
  private void _setMaxHeapSize(String arg) {
    var maxHeapSize = Integer.parseInt(arg);
    _controller.state().getAndUpdate(s -> s.withGCStats(s.gcStats.withMaxHeapSize(maxHeapSize)));
  }

  private void _dispatch(String cmd, String arg) {
    _commands.lookup(cmd).matchDo(
      () -> System.out.println("Command \"" + cmd + "\" not recognized."),
      c -> c.snd.accept(this, arg));
  }

  @Override
  public void run(String[] args) {
    try (var in = new Scanner(System.in)) {
      while (true) {
        try {
          System.out.print("tli> ");

          var line = in.nextLine();
          var parts = line.split(" ", 2);

          if (parts.length > 0)
            _dispatch(parts[0], parts.length > 1 ? parts[1] : "");

        } catch (UParsecException e) {
          System.out.println("Parser error: " + e.getMessage());
        } catch (TypeCheckerException e) {
          System.out.println("Type error: " + e.getMessage());
        } catch (EvalException e) {
          System.out.println("Evaluation error: " + e.getMessage());
        } catch (NoSuchElementException e) {
          System.out.println("EOF detected. Quitting.");
          System.exit(0);
        } catch (Throwable e) {
          System.out.println("Something unexpected occurred:");
          e.printStackTrace();
        }
      }
    }
  }
}
