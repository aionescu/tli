package com.aionescu.tli.utils.data.list;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.aionescu.tli.utils.Pair;
import com.aionescu.tli.utils.Unit;
import com.aionescu.tli.utils.control.Maybe;
import com.aionescu.tli.utils.data.Foldable;

public abstract class List<A> implements Foldable<A> {
  private static final class Nil<A> extends List<A> {
    public Nil() { }

    @Override
    public <B> B match(Supplier<B> nil, BiFunction<A, List<A>, B> cons) {
      return nil.get();
    }
  }

  private static final class Cons<A> extends List<A> {
    private final A _head;
    private final List<A> _tail;

    public Cons(A head, List<A> tail) {
      _head = head;
      _tail = tail;
    }

    @Override
    public <B> B match(Supplier<B> nil, BiFunction<A, List<A>, B> cons) {
      return cons.apply(_head, _tail);
    }
  }

  private List() { }

  public abstract <B> B match(Supplier<B> nil, BiFunction<A, List<A>, B> cons);

  public final void matchDo(Runnable nil, BiConsumer<A, List<A>> cons) {
    match(
      () -> { nil.run(); return Unit.UNIT; },
      (a, as) -> { cons.accept(a, as); return Unit.UNIT; });
  }

  public static <A> List<A> nil() {
    return new Nil<>();
  }

  public static <A> List<A> cons(A head, List<A> tail) {
    return new Cons<>(head, tail);
  }

  @Override
  public final boolean equals(Object rhs) {
    @SuppressWarnings("unchecked")
    var r = rhs instanceof List<?> && eq((List<A>)rhs);
    return r;
  }

  private final boolean eq(List<A> rhs) {
    return match(rhs::isEmpty, (a, as) -> rhs.match(
      () -> false,
      (b, bs) -> a.equals(b) && as.eq(bs)
    ));
  }

  @Override
  public final String toString() {
    return toString("[", "]");
  }

  public static <A> List<A> singleton(A value) {
    return cons(value, nil());
  }

  public final boolean isEmpty() {
    return match(() -> true, (h, t) -> false);
  }

  public final int length() {
    return foldL((s, a) -> s + 1, 0);
  }

  @SafeVarargs
  public static <A> List<A> of(A... as) {
    return ofStream(Arrays.stream(as));
  }

  public static <A> List<A> ofStream(Stream<A> stream) {
    return stream.reduce(List.<A>nil(), (l, a) -> cons(a, l), List::append).reverse();
  }

  public Stream<A> stream() {
    return foldL((s, a) -> s.add(a), Stream.<A>builder()).build();
  }

  private static String _asString(List<Character> chars, String acc) {
    return chars.match(() -> acc, (h, t) -> _asString(t, acc + h));
  }

  public static String asString(List<Character> chars) {
    return _asString(chars, "");
  }

  public final String toString(String begin, String end) {
    return match(
      () -> begin + end,
      (h, t) -> begin + h + t.foldL((s, a) -> s + ", " + a, "") + end);
  }

  public final Maybe<Pair<A, List<A>>> uncons() {
    return match(Maybe::nothing, (h, t) -> Maybe.just(Pair.of(h, t)));
  }

  public final List<A> append(List<A> b) {
    return match(() -> b, (h, t) -> List.cons(h, t.append(b)));
  }

  public final <B> List<B> map(Function<A, B> f) {
    return match(List::nil, (h, t) -> List.cons(f.apply(h), t.map(f)));
  }

  public final <B> List<B> bind(Function<A, List<B>> f) {
    return match(List::nil, (h, t) -> f.apply(h).append(t.bind(f)));
  }

  public final void iter(Consumer<A> f) {
    matchDo(() -> { }, (h, t) -> { f.accept(h); t.iter(f); });
  }

  public final List<A> filter(Predicate<A> f) {
    return match(List::nil, (a, as) -> f.test(a) ? cons(a, as.filter(f)) : as.filter(f));
  }

  public final Maybe<A> find(Predicate<A> f) {
    return match(Maybe::nothing, (a, as) -> f.test(a) ? Maybe.just(a) : as.find(f));
  }

  public final boolean any(Predicate<A> f) {
    return foldL((s, a) -> s || f.test(a), false);
  }

  public final boolean all(Predicate<A> f) {
    return foldL((s, a) -> s && f.test(a), true);
  }

  public final boolean allUnique() {
    return all(a -> this.filter(e -> e.equals(a)).length() == 1);
  }

  public static List<Integer> range(int start, int end) {
    return
      start >= end
      ? nil()
      : List.cons(start, range(start + 1, end));
  }

  public final <B, C> List<C> zipWith(List<B> bs, BiFunction<A, B, C> f) {
    return match(List::nil, (a, as) ->
      bs.match(List::nil, (b, bs_) ->
        cons(f.apply(a, b), as.zipWith(bs_, f))));
  }

  public final <B> List<Pair<A, B>> zip(List<B> bs) {
    return zipWith(bs, Pair::new);
  }

  public final List<Pair<Integer, A>> indexed() {
    return range(0, length()).zipWith(this, Pair::new);
  }

  public final <I> List<Pair<I, A>> indexedWith(Function<Integer, I> f) {
    return range(0, length()).map(f).zipWith(this, Pair::new);
  }

  public static <A extends Comparable<A>> int compare(List<A> as, List<A> bs) {
    return as.match(
      () -> bs.isEmpty() ? 0 : -1,
      (a, as_) -> bs.match(
        () -> 1,
        (b, bs_) -> {
          var c = a.compareTo(b);
          return c != 0 ? c : compare(as_, bs_);
        }));
  }

  public final List<A> insertSorted(Comparator<A> f, A v) {
    return match(
      () -> List.singleton(v),
      (a, as) -> {
        var c = f.compare(a, v);

        return
          c == 0
          ? List.cons(v, as)
          : c > 0
          ? List.cons(v, this)
          : List.cons(a, as.insertSorted(f, v));
      });
  }

  public final List<A> mergeSorted(Comparator<A> f, List<A> vs) {
    return vs.foldL((s, a) -> s.insertSorted(f, a), this);
  }

  public final <B extends Comparable<B>> List<A> sortBy(Function<A, B> f) {
    Comparator<A> c = (a, b) -> f.apply(a).compareTo(f.apply(b));
    return foldL((s, a) -> s.insertSorted(c, a), nil());
  }

  public final List<A> reverse() {
    return match(() -> this, (h, t) -> t.reverse().append(List.cons(h, List.nil())));
  }

  public final String unlines(String sep) {
    return match(
      () -> "",
      (h, t) -> h.toString() + t.foldL((s, a) -> s + sep + a, ""));
  }

  public final String unlines() {
    return unlines("\n");
  }

  public final ObservableList<A> toObservable() {
    return stream().collect(
      Collectors.collectingAndThen(
        Collectors.toList(),
        FXCollections::observableList));
  }

  @Override
  public final <S> S foldL(BiFunction<S, A, S> f, S zero) {
    return match(() -> zero, (h, t) -> t.foldL(f, f.apply(zero, h)));
  }

  @Override
  public final <S> S foldR(BiFunction<A, S, S> f, S zero) {
    return match(() -> zero, (h, t) -> f.apply(h, t.foldR(f, zero)));
  }
}
