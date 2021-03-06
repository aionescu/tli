package com.aionescu.tli.exn.eval;

import com.aionescu.tli.ast.val.VStr;

public final class IncorrectFileFormatException extends EvalException {
  private final static long serialVersionUID = 1;

  private final String _file;

  public IncorrectFileFormatException(String file) {
    super();

    _file = file;
  }

  @Override
  public String getMessage() {
    return String.format("The file %s contains invalid Toy Language values.", VStr.escapeString(_file));
  }
}
