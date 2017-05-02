///////////////////////////////////////////////////////////////////////////
//                   __                _      _   ________               //
//                  / /   ____  ____ _(_)____/ | / / ____/               //
//                 / /   / __ \/ __ `/ / ___/  |/ / / __                 //
//                / /___/ /_/ / /_/ / / /__/ /|  / /_/ /                 //
//               /_____/\____/\__, /_/\___/_/ |_/\____/                  //
//                           /____/                                      //
//                                                                       //
//               The Next Generation Logic Library                       //
//                                                                       //
///////////////////////////////////////////////////////////////////////////
//                                                                       //
//  Copyright 2015-2016 Christoph Zengler                                //
//                                                                       //
//  Licensed under the Apache License, Version 2.0 (the "License");      //
//  you may not use this file except in compliance with the License.     //
//  You may obtain a copy of the License at                              //
//                                                                       //
//  http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                       //
//  Unless required by applicable law or agreed to in writing, software  //
//  distributed under the License is distributed on an "AS IS" BASIS,    //
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or      //
//  implied.  See the License for the specific language governing        //
//  permissions and limitations under the License.                       //
//                                                                       //
///////////////////////////////////////////////////////////////////////////


package org.logicng.formulas;

import org.logicng.collections.LNGIntVector;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ExtendedFormulaFactory extends FormulaFactory {

  private int nextStateId;
  private final LNGIntVector validStates = new LNGIntVector();

  @Override
  public void clear() {
    this.posLiterals = new LinkedHashMap<>();
    this.negLiterals = new LinkedHashMap<>();
    this.generatedVariables = new LinkedHashSet<>();
    this.nots = new LinkedHashMap<>();
    this.implications = new LinkedHashMap<>();
    this.equivalences = new LinkedHashMap<>();
    this.ands2 = new LinkedHashMap<>();
    this.ands3 = new LinkedHashMap<>();
    this.ands4 = new LinkedHashMap<>();
    this.andsN = new LinkedHashMap<>();
    this.ors2 = new LinkedHashMap<>();
    this.ors3 = new LinkedHashMap<>();
    this.ors4 = new LinkedHashMap<>();
    this.orsN = new LinkedHashMap<>();
    this.pbConstraints = new LinkedHashMap<>();
    this.ccCounter = 0;
    this.pbCounter = 0;
    this.cnfCounter = 0;
  }

  public FormulaFactoryState save() {
    int[] state = new int[18];
    state[0] = this.posLiterals.size();
    state[1] = this.negLiterals.size();
    state[2] = this.generatedVariables.size();
    state[3] = this.nots.size();
    state[4] = this.implications.size();
    state[5] = this.equivalences.size();
    state[6] = this.ands2.size();
    state[7] = this.ands3.size();
    state[8] = this.ands4.size();
    state[9] = this.andsN.size();
    state[10] = this.ors2.size();
    state[11] = this.ors3.size();
    state[12] = this.ors4.size();
    state[13] = this.orsN.size();
    state[14] = this.pbConstraints.size();
    state[15] = this.ccCounter;
    state[16] = this.pbCounter;
    state[17] = this.cnfCounter;
    final int id = this.nextStateId++;
    validStates.push(id);
    return new FormulaFactoryState(id, state);
  }

  public void load(final FormulaFactoryState state) {
    int index = -1;
    for (int i = validStates.size() - 1; i >= 0 && index == -1; i--)
      if (validStates.get(i) == state.id())
        index = i;
    if (index == -1)
      throw new IllegalArgumentException("The given formula factory state is not valid anymore.");
    this.validStates.shrinkTo(index + 1);

    shrinkMap(this.posLiterals, state.state()[0]);
    shrinkMap(this.negLiterals, state.state()[1]);
    shrinkSet(this.generatedVariables, state.state()[2]);
    shrinkMap(this.nots, state.state()[3]);
    shrinkMap(this.implications, state.state()[4]);
    shrinkMap(this.equivalences, state.state()[5]);
    shrinkMap(this.ands2, state.state()[6]);
    shrinkMap(this.ands3, state.state()[7]);
    shrinkMap(this.ands4, state.state()[8]);
    shrinkMap(this.andsN, state.state()[9]);
    shrinkMap(this.ors2, state.state()[10]);
    shrinkMap(this.ors3, state.state()[11]);
    shrinkMap(this.ors4, state.state()[12]);
    shrinkMap(this.orsN, state.state()[13]);
    shrinkMap(this.pbConstraints, state.state()[14]);
    this.ccCounter = state.state()[15];
    this.pbCounter = state.state()[16];
    this.cnfCounter = state.state()[17];

    cnfEncoder().clearCaches();
  }

  public void fixateCurrentState() {
    this.validStates.clear();
  }

  @Override
  public boolean shouldCache() {
    return validStates.empty();
  }

  static <T, U> void shrinkMap(final Map<T, U> map, int newSize) {
    if (!(map instanceof LinkedHashMap)) {
      throw new IllegalStateException("Cannot shrink a map which is not of type LinkedHashMap");
    }
    if (newSize > map.size()) {
      throw new IllegalStateException("Cannot shrink a map of size " + map.size() + " to new size " + newSize);
    }
    Iterator<Map.Entry<T, U>> entryIterator = map.entrySet().iterator();
    int count = 0;
    while (count < newSize) {
      entryIterator.next();
      count++;
    }
    while (entryIterator.hasNext()) {
      entryIterator.next();
      entryIterator.remove();
    }
  }

  static <T> void shrinkSet(final Set<T> set, int newSize) {
    if (!(set instanceof LinkedHashSet)) {
      throw new IllegalStateException("Cannot shrink a set which is not of type LinkedHashSet");
    }
    if (newSize > set.size()) {
      throw new IllegalStateException("Cannot shrink a set of size " + set.size() + " to new size " + newSize);
    }
    Iterator<T> entryIterator = set.iterator();
    int count = 0;
    while (count < newSize) {
      entryIterator.next();
      count++;
    }
    while (entryIterator.hasNext()) {
      entryIterator.next();
      entryIterator.remove();
    }
  }
}
