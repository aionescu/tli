package tli.ast.stmt;

import utils.collections.map.Map;

import tli.ast.Ident;
import tli.ast.prog.ProgState;
import tli.ast.type.Type;
import tli.ast.type.VarInfo;
import tli.ast.type.VarState;
import tli.exn.typeck.VariableAlreadyDeclaredException;

public final class Decl implements Stmt {
  private final Ident _ident;
  private final Type _type;

  public static Decl of(Ident ident, Type type) {
    return new Decl(ident, type);
  }

  public Decl(Ident ident, Type type) {
    _ident = ident;
    _type = type;
  }

  @Override
  public Map<Ident, VarInfo> typeCheck(Map<Ident, VarInfo> sym) {
    if (sym.lookup(_ident).isPresent())
      throw new VariableAlreadyDeclaredException(_ident);

    return sym.insert(_ident, VarInfo.of(_type, VarState.UNINIT));
  }

  @Override
  public ProgState eval(ProgState prog) {
    return prog;
  }

  @Override
  public String toString() {
    return String.format("%s : %s", _ident, _type);
  }
}
