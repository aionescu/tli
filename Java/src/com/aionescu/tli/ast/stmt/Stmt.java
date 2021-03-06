package com.aionescu.tli.ast.stmt;

import com.aionescu.tli.ast.Ident;
import com.aionescu.tli.ast.prog.ThreadState;
import com.aionescu.tli.ast.type.varinfo.VarInfo;
import com.aionescu.tli.utils.data.list.List;
import com.aionescu.tli.utils.data.map.Map;

public interface Stmt {
  Map<Ident, VarInfo> typeCheck(Map<Ident, VarInfo> sym);
  ThreadState eval(ThreadState prog);

  default List<Stmt> explode() {
    return List.singleton(this);
  }
}
