package com.aionescu.tli.ast.stmt;

import com.aionescu.tli.ast.Ident;
import com.aionescu.tli.ast.prog.ThreadState;
import com.aionescu.tli.ast.type.Type;
import com.aionescu.tli.ast.type.varinfo.VarInfo;
import com.aionescu.tli.ast.type.varinfo.VarState;
import com.aionescu.tli.exn.typeck.VariableAlreadyDeclaredException;
import com.aionescu.tli.utils.data.map.Map;

public final class Decl implements Stmt {
  private final Ident _ident;
  private final Type _type;

  public Decl(Ident ident, Type type) {
    _ident = ident;
    _type = type;
  }

  @Override
  public String toString() {
    return String.format("let %s: %s", _ident, _type);
  }

  @Override
  public Map<Ident, VarInfo> typeCheck(Map<Ident, VarInfo> sym) {
    return sym.lookup(_ident).match(
      () -> sym.insert(_ident, new VarInfo(_type, VarState.UNINIT)),
      a -> { throw new VariableAlreadyDeclaredException(_ident); });
  }

  @Override
  public ThreadState eval(ThreadState prog) {
    return prog;
  }
}
