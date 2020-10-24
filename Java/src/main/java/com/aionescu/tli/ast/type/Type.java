package com.aionescu.tli.ast.type;

import com.aionescu.tli.exn.typeck.TypeMismatchException;

public enum Type {
  INT,
  BOOL;

  public void expect(Type expected) {
    if (this != expected)
      throw new TypeMismatchException(expected, this);
  }

  @Override
  public String toString() {
    return switch (this) {
      case INT -> "Int";
      case BOOL -> "Bool";
    };
  }
}