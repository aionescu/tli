package tli.ast.expr;

import utils.collections.map.Map;

import tli.ast.Ident;
import tli.ast.type.Type;
import tli.ast.type.VarInfo;
import tli.ast.val.Bool;
import tli.ast.val.Int;
import tli.ast.val.Val;

public final class Comp implements Expr {
  public static enum Op {
    LT,
    LTE,
    GT,
    GTE,
    EQ,
    NEQ;

    @Override
    public String toString() {
      return switch (this) {
        case LT -> "<";
        case LTE -> "<=";
        case GT -> ">";
        case GTE -> ">=";
        case EQ -> "=";
        case NEQ -> "<>";
      };
    }

    public Bool evalInt(Int lhs, Int rhs) {
      var a = lhs.val;
      var b = rhs.val;

      return new Bool(switch (this) {
        case LT -> a < b;
        case LTE -> a <= b;
        case GT -> a > b;
        case GTE -> a >= b;
        case EQ -> a == b;
        case NEQ -> a != b;
      });
    }

    public Bool evalBool(Bool lhs, Bool rhs) {
      var a = lhs.val;
      var b = rhs.val;

      return new Bool(switch (this) {
        case LT -> Boolean.compare(a, b) < 0;
        case LTE -> Boolean.compare(a, b) <= 0;
        case GT -> Boolean.compare(a, b) > 0;
        case GTE -> Boolean.compare(a, b) >= 0;
        case EQ -> a == b;
        case NEQ -> a != b;
      });
    }
  }

  private final Expr _lhs, _rhs;
  private final Op _op;

  public static Comp of(Expr lhs, Op op, Expr rhs) {
    return new Comp(lhs, op, rhs);
  }

  public Comp(Expr lhs, Op op, Expr rhs) {
    _lhs = lhs;
    _rhs = rhs;
    _op = op;
  }

  @Override
  public Type typeCheck(Map<Ident, VarInfo> sym) {
    _rhs.typeCheck(sym).expect(_lhs.typeCheck(sym));
    return Type.BOOL;
  }

  @Override
  public Val eval(Map<Ident, Val> sym) {

    var lhs = _lhs.eval(sym);
    var rhs = _rhs.eval(sym);

    return
      lhs.type() == Type.INT
      ? _op.evalInt((Int)lhs, (Int)rhs)
      : _op.evalBool((Bool)lhs, (Bool)rhs);
  }

  @Override
  public String toString() {
    return String.format("(%s %s %s)", _lhs, _op, _rhs);
  }
}
