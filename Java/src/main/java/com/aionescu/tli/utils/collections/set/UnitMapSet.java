package com.aionescu.tli.utils.collections.set;

import java.util.function.Function;

import com.aionescu.tli.utils.Pair;
import com.aionescu.tli.utils.Unit;
import com.aionescu.tli.utils.collections.list.List;
import com.aionescu.tli.utils.collections.map.Map;

public final class UnitMapSet<A extends Comparable<A>> implements Set<A> {
  private final Map<A, Unit> _map;

  private UnitMapSet(Map<A, Unit> map) {
    _map = map;
  }

  public static <A extends Comparable<A>> UnitMapSet<A> empty() {
    return new UnitMapSet<A>(Map.empty());
  }

  @Override
  public List<A> toList() {
    return _map.toList().map(Pair::fst_);
  }

  @Override
  public Map<A, Unit> toMap() {
    return _map;
  }

  @Override
  public boolean isEmpty() {
    return _map.toList().isEmpty();
  }

  @Override
  public Set<A> insert(A a) {
    return new UnitMapSet<>(_map.insert(a, Unit.UNIT));
  }

  @Override
  public Set<A> diff(Set<A> rhs) {
    return new UnitMapSet<>(_map.diff(rhs.toMap()));
  }

  @Override
  public Set<A> union(Set<A> rhs) {
    return new UnitMapSet<>(_map.union(rhs.toMap()));
  }

  @Override
  public <B extends Comparable<B>> Set<B> map(Function<A, B> f) {
    return new UnitMapSet<B>(Map.fromList(_map.toList().map(Pair::fst_).map(f).map(a -> Pair.of(a, Unit.UNIT))));
  }
}
