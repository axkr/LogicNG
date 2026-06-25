///////////////////////////////////////////////////////////////////////////
// __ _ _ ________ //
// / / ____ ____ _(_)____/ | / / ____/ //
// / / / __ \/ __ `/ / ___/ |/ / / __ //
// / /___/ /_/ / /_/ / / /__/ /| / /_/ / //
// /_____/\____/\__, /_/\___/_/ |_/\____/ //
// /____/ //
// //
// The Next Generation Logic Library //
// //
///////////////////////////////////////////////////////////////////////////
// //
// Copyright 2015-20xx Christoph Zengler //
// //
// Licensed under the Apache License, Version 2.0 (the "License"); //
// you may not use this file except in compliance with the License. //
// You may obtain a copy of the License at //
// //
// http://www.apache.org/licenses/LICENSE-2.0 //
// //
// Unless required by applicable law or agreed to in writing, software //
// distributed under the License is distributed on an "AS IS" BASIS, //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or //
// implied. See the License for the specific language governing //
// permissions and limitations under the License. //
// //
///////////////////////////////////////////////////////////////////////////

package src.test.java.org.logicng.knowledgecompilation.bdds.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.logicng.TestWithExampleFormulas.parse;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.handlers.NumberOfNodesBDDHandler;
import org.logicng.handlers.TimeoutBDDHandler;
import org.logicng.knowledgecompilation.bdds.BDD;
import org.logicng.knowledgecompilation.bdds.io.ROBDDReader;
import org.logicng.knowledgecompilation.bdds.io.ROBDDWriter;
import org.logicng.knowledgecompilation.bdds.jbuddy.BDDKernel; 

import java.math.BigInteger;

public class ROBDDTest {

    @Test
    public void testROBDDRoundtrip() {
      final FormulaFactory f = new FormulaFactory();
      // Wolfram function `BooleanFunction[30, 3] // FullForm` produces the following ROBDD representation:
      final int[] fromArray = new int[] {-3, 0, 1, -2, 1, 3, -1, 2, 1, -1};
      BDD bdd = ROBDDReader.read(fromArray, f);
      final int[] toArray = ROBDDWriter.write(bdd);
      assertEquals(fromArray, toArray);
    }

}
