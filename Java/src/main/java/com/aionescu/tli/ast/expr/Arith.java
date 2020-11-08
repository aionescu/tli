package com.aionescu.tli.ast.expr;

import com.aionescu.tli.utils.collections.map.Map;

import java.math.BigInteger;

import com.aionescu.tli.ast.Ident;
import com.aionescu.tli.ast.type.TInt;
import com.aionescu.tli.ast.type.Type;
import com.aionescu.tli.ast.type.varinfo.VarInfo;
import com.aionescu.tli.ast.val.VInt;
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

  public Arith(Expr lhs, Op op, Expr rhs) {
    _lhs = lhs;
    _rhs = rhs;
    _op = op;
  }

  @Override
  public String toString() {
    return String.format("(%s %s %s)", _lhs, _op, _rhs);
  }

  @Override
  public Type typeCheck(Map<Ident, VarInfo> sym) {
    _lhs.typeCheck(sym).expect(TInt.t);
    _rhs.typeCheck(sym).expect(TInt.t);
    return TInt.t;
  }

  @Override
  public Val eval(Map<Ident, Val> sym) {
    var lhs = ((VInt)_lhs.eval(sym)).val;
    var rhs = ((VInt)_rhs.eval(sym)).val;

    return new VInt(switch (_op) {
      case ADD -> lhs.add(rhs);
      case SUB -> lhs.subtract(rhs);
      case MUL -> lhs.multiply(rhs);
      case DIV -> switch (rhs.compareTo(BigInteger.ZERO)) {
        case 0 -> throw new DivisionByZeroException();
        default -> lhs.divide(rhs);
      };
      case REM -> switch (rhs.compareTo(BigInteger.ZERO)) {
        case 0 -> throw new DivisionByZeroException();
        default -> lhs.remainder(rhs);
      };
    });
  }
}
