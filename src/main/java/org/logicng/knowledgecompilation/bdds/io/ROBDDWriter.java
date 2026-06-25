package org.logicng.knowledgecompilation.bdds.io;

import org.logicng.knowledgecompilation.bdds.BDD;
import org.logicng.knowledgecompilation.bdds.jbuddy.BDDOperations;
import java.io.File;
import java.io.IOException;


public final class ROBDDWriter {
  private ROBDDWriter() {

  }
  
  /**
   * Generates a flat integer array representation of the BDD. The format
   * encodes a <a href="https://en.wikipedia.org/wiki/Directed_acyclic_graph">Canonical Directed Acyclic Graph (DAG)</a> with Complemented Edges, matching
   * Wolfram languages internal BDD representations. 
   * 
   * @param bdd the BDD which should be written
   * @return a flat integer array representation of the BDD
   */
  public static int[] write(final BDD bdd) { 
    org.logicng.knowledgecompilation.bdds.jbuddy.BDDKernel kernel = bdd.underlyingKernel();
    BDDOperations operations =  new BDDOperations(kernel);
    return operations.toROBDD(bdd.index(), bdd.getVariableOrder().size());
  }
}
