package com.aionescu.tli.ast.val;

import java.util.function.UnaryOperator;

import com.aionescu.tli.ast.type.Type;
import com.aionescu.tli.utils.data.set.Set;

public abstract class Val implements Comparable<Val> {
  @Override
  public final boolean equals(Object rhs) {
    return getClass().equals(rhs.getClass()) && compareTo((Val)rhs) == 0;
  }

  public abstract Type type();

  public Set<Integer> getInnerAddrs() {
    return Set.empty();
  }

  public Val mapInnerAddrs(UnaryOperator<Integer> f) {
    return this;
  }
}
