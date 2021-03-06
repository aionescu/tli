package com.aionescu.tli.exn.typeck;

import com.aionescu.tli.ast.Ident;

public final class VariableAlreadyDeclaredException extends TypeCheckerException {
  private final static long serialVersionUID = 1;

  private final Ident _ident;

  public VariableAlreadyDeclaredException(Ident ident) {
    super();

    _ident = ident;
  }

  @Override
  public String getMessage() {
    return String.format("Variable %s has already been declared.", _ident);
  }
}
