package org.logicng.solvers.functions.splitVariableProvider;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Variable;

import java.util.Collection;
import java.util.SortedSet;

/**
 * A split variable provider for which the variables are fixed.
 * @version 2.3.0
 * @since 2.3.0
 */
public class FixedVariableProvider extends SplitVariableProvider {

    private final SortedSet<Variable> splitVariables;

    public FixedVariableProvider(final FormulaFactory f, final SortedSet<Variable> splitVariables) {
        super(f);
        this.splitVariables = splitVariables;
    }

    @Override
    public SortedSet<Variable> getSplitVars(final Collection<Formula> formulas, final Collection<Variable> variables) {
        return this.splitVariables;
    }
}