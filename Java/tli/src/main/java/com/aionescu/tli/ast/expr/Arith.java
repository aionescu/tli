package com.aionescu.tli.ast.expr;

import com.aionescu.tli.utils.collections.map.Map;

import com.aionescu.tli.ast.Ident;
import com.aionescu.tli.ast.type.Type;
import com.aionescu.tli.ast.type.VarInfo;
import com.aionescu.tli.ast.val.Int;
import com.aionescu.tli.ast.val.Val;
import com.aionescu.tli.exn.eval.DivisionByZeroException;

public final class Arith implements Expr {
  public static enum Op {
    ADD,
    SUB,
    MUL,
    DIV,
    REM;

    @Override
    public String toString() {
      return switch (this) {
        case ADD -> "+";
        case SUB -> "-";
        case MUL -> "*";
        case DIV -> "/";
        case REM -> "%";
      };
    }
  }

  private final Expr _lhs, _rhs;
  private final Op _op;

  public static Arith of(Expr lhs, Op op, Expr rhs) {
    return new Arith(lhs, op, rhs);
  }

  public Arith(Expr lhs, Op op, Expr rhs) {
    _lhs = lhs;
    _rhs = rhs;
    _op = op;
  }

  @Override
  public Type typeCheck(Map<Ident, VarInfo> sym) {
    _lhs.typeCheck(sym).expect(Type.INT);
    _rhs.typeCheck(sym).expect(Type.INT);
    return Type.INT;
  }

  @Override
  public Val eval(Map<Ident, Val> sym) {
    var lhs = ((Int)_lhs.eval(sym)).val;
    var rhs = ((Int)_rhs.eval(sym)).val;

    return new Int(switch (_op) {
      case ADD -> lhs + rhs;
      case SUB -> lhs - rhs;
      case MUL -> lhs * rhs;
      case DIV -> switch (rhs) {
        case 0 -> throw new DivisionByZeroException();
        default -> lhs / rhs;
      };
      case REM -> switch (rhs) {
        case 0 -> throw new DivisionByZeroException();
        default -> lhs % rhs;
      };
    });
  }

  @Override
  public String toString() {
    return String.format("(%s %s %s)", _lhs, _op, _rhs);
  }
}
