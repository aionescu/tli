package com.aionescu.tli.controller;

import com.aionescu.tli.ast.prog.ProgState;
import com.aionescu.tli.repo.Repository;
import com.aionescu.tli.utils.collections.list.List;

public final class Controller {
  Repository _repo;

  public Controller(Repository repo) {
    _repo = repo;
  }

  public ProgState state() {
    return _repo.state();
  }

  public void setState(ProgState state) {
    _repo.setState(state);
  }

  public void typeCheck() {
    _repo.typeCheck();
  }

  public ProgState oneStep() {
    _repo.oneStep();
    return _repo.state();
  }

  public boolean done() {
    return _repo.done();
  }

  public List<ProgState> allSteps() {
    if (done())
      return List.singleton(state());
    else {
      var current = state();
      oneStep();

      return List.cons(current, allSteps());
    }
  }
}