package org.logicng.solvers.functions.splitvariablesprovider;

import org.logicng.formulas.Variable;
import org.logicng.solvers.SATSolver;

import java.util.Collection;
import java.util.SortedSet;

/**
 * A split variable provider which provides split variables which occur particularly seldom in the formulas on the solver. The variables occurring in the
 * formulas are sorted by their occurrence. This provider returns those variables with the smallest occurrence.
 * @version 2.4.0
 * @since 2.4.0
 */
public class LeastCommonVariablesProvider extends SplitVariableProviderWithTakeRate {

    /**
     * Creates a split variable provider returning the least common variables with a take rate of {@code 0.5}.
     */
    public LeastCommonVariablesProvider() {
        super(0.5);
    }

    /**
     * Creates a split variable provider returning the least common variables.
     * <p>
     * The take rate specifies the number of variables which should be returned in {@link #getSplitVars}.
     * So the result will contain {@code (int) Math.ceil(variables.size() * takeRate)} variables.
     * @param takeRate the take rate, must be &gt; 0 and &lt;=1
     */
    public LeastCommonVariablesProvider(final double takeRate) {
        super(takeRate);
    }

    @Override
    public SortedSet<Variable> getSplitVars(final SATSolver solver, final Collection<Variable> variables) {
        return chooseVariablesByOccurrences(solver, variables, false);
    }
}