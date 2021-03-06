package com.aionescu.tli.utils.data.stack;

import com.aionescu.tli.utils.Pair;
import com.aionescu.tli.utils.control.Maybe;
import com.aionescu.tli.utils.data.list.List;

public final class ConsListStack<A> implements Stack<A> {
  private final List<A> _list;

  private ConsListStack(List<A> list) {
    _list = list;
  }

  public static <A> ConsListStack<A> empty() {
    return new ConsListStack<>(List.nil());
  }

  public static <A> ConsListStack<A> ofList(List<A> list) {
    return new ConsListStack<>(list);
  }

  @Override
  public boolean equals(Object rhs) {
    return rhs instanceof ConsListStack<?> && _list.equals(((ConsListStack<?>)rhs)._list);
  }

  @Override
  public String toString() {
    return _list.toString();
  }

  @Override
  public Stack<A> push(A val) {
    return new ConsListStack<>(List.cons(val, _list));
  }

  @Override
  public Maybe<Pair<A, Stack<A>>> pop() {
    return _list.uncons().map(Pair.second(ConsListStack::new));
  }

  @Override
  public boolean isEmpty() {
    return _list.isEmpty();
  }

  @Override
  public List<A> toList() {
    return _list;
  }
}
