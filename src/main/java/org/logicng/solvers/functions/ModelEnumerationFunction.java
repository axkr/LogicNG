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
//  Copyright 2015-20xx Christoph Zengler                                //
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

package org.logicng.solvers.functions;

import static org.logicng.datastructures.Tristate.TRUE;
import static org.logicng.datastructures.Tristate.UNDEF;
import static org.logicng.formulas.FormulaFactory.CC_PREFIX;
import static org.logicng.formulas.FormulaFactory.CNF_PREFIX;
import static org.logicng.formulas.FormulaFactory.PB_PREFIX;
import static org.logicng.handlers.Handler.start;

import org.logicng.collections.LNGBooleanVector;
import org.logicng.collections.LNGIntVector;
import org.logicng.datastructures.Assignment;
import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.Variable;
import org.logicng.handlers.ModelEnumerationHandler;
import org.logicng.handlers.SATHandler;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SolverState;
import org.logicng.solvers.functions.splitVariableProvider.RandomSplitVariableProvider;
import org.logicng.solvers.functions.splitVariableProvider.SplitVariableProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A solver function for enumerating models on the solver.
 * <p>
 * Model enumeration functions are instantiated via their builder {@link #builder()}.
 * @version 2.3.0
 * @since 2.0.0
 */
public class ModelEnumerationFunction implements SolverFunction<List<Assignment>> {

    protected final ModelEnumerationHandler handler;
    protected final Collection<Variable> variables;
    protected final Collection<Variable> additionalVariables;
    protected final boolean fastEvaluable;
    protected final SplitVariableProvider splitVariableProvider;
    protected final boolean doubleSplit;

    ModelEnumerationFunction(final ModelEnumerationHandler handler, final Collection<Variable> variables, final Collection<Variable> additionalVariables,
                             final boolean fastEvaluable, final SplitVariableProvider splitVariableProvider, final boolean doubleSplit) {
        this.handler = handler;
        this.variables = variables;
        this.additionalVariables = additionalVariables;
        this.fastEvaluable = fastEvaluable;
        this.splitVariableProvider = splitVariableProvider;
        this.doubleSplit = doubleSplit;
    }

    /**
     * Returns the builder for this function.
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<Assignment> apply(final MiniSat solver, final Consumer<Tristate> resultSetter) {
        start(this.handler);
        if (this.splitVariableProvider == null) {
            return enumerate(solver, resultSetter, this.variables, this.additionalVariables);
        }
        final Set<Formula> formulasOnSolver = solver.execute(FormulaOnSolverFunction.get());
        if (formulasOnSolver.isEmpty()) {
            return Collections.singletonList(new Assignment());
        }
        final SortedSet<Variable> relevantVars = getVarsForEnumeration(solver.knownVariables());
        return splitModelEnumeration(solver, resultSetter, formulasOnSolver, relevantVars, this.additionalVariables);
    }

    protected List<Assignment> splitModelEnumeration(final MiniSat solver, final Consumer<Tristate> resultSetter, final Collection<Formula> formulasOnSolver,
                                                     final SortedSet<Variable> relevantVars, final Collection<Variable> additionalVariables) {
        final SolverState initialState = solver.saveState();
        final SortedSet<Variable> splitVars = this.splitVariableProvider.getSplitVars(formulasOnSolver, relevantVars);
        final List<Assignment> splitAssignments = enumerate(solver, resultSetter, splitVars, Collections.emptyList());
        final List<Assignment> models = new ArrayList<>();
        for (final Assignment splitAssignment : splitAssignments) {
            if (splitAssignment.size() >= 10) {
                // split again
                final SortedSet<Variable> leftOverRelevant = new TreeSet<>(relevantVars);
                leftOverRelevant.removeAll(splitVars);
                models.addAll(splitAgain(solver, resultSetter, relevantVars, additionalVariables, leftOverRelevant, splitAssignment, 0));
            } else {
                solver.add(splitAssignment.formula(solver.factory()));
                final List<Assignment> assignments = enumerate(solver, resultSetter, relevantVars, additionalVariables);
                models.addAll(assignments);
                solver.loadState(initialState);
            }
            solver.loadState(initialState);
        }
        return models;
    }

    private List<Assignment> splitAgain(final MiniSat solver, final Consumer<Tristate> resultSetter, final SortedSet<Variable> relevantVars,
                                        final Collection<Variable> additionalVariables, final SortedSet<Variable> leftOverRelevant,
                                        final Assignment splitAssignment, final int depth) {
        final List<Assignment> models = new ArrayList<>();
        solver.add(splitAssignment.formula(solver.factory()));
        final SolverState initialState = solver.saveState();
        final SortedSet<Variable> thisSplitVars = new RandomSplitVariableProvider(solver.factory(), 5, 50, 1).getSplitVars(null, leftOverRelevant);
        final List<Assignment> thisSplitAssignments = enumerate(solver, resultSetter, thisSplitVars, Collections.emptyList());
        final List<Assignment> thisModels = new ArrayList<>();
        for (final Assignment splitAssignment1 : thisSplitAssignments) {
            if (splitAssignment1.size() > 4) {
                System.out.println("big " + splitAssignment1.size());
                final SortedSet<Variable> newLeftOverRelevant = new TreeSet<>(leftOverRelevant);
                newLeftOverRelevant.removeAll(thisSplitVars);
                models.addAll(splitAgain(solver, resultSetter, relevantVars, additionalVariables, newLeftOverRelevant, splitAssignment1, depth + 1));
            } else {
                final Formula thisFormula = splitAssignment1.formula(solver.factory());
                solver.add(thisFormula);
                final List<Assignment> thisAssignments = enumerate(solver, resultSetter, relevantVars, additionalVariables);
                thisModels.addAll(thisAssignments);
                solver.loadState(initialState);
            }
        }
        models.addAll(thisModels);
        return models;
    }

    protected List<Assignment> enumerate(final MiniSat solver, final Consumer<Tristate> resultSetter, final Collection<Variable> variables,
                                         final Collection<Variable> additionalVariables) {
        final List<Assignment> models = new ArrayList<>();
        SolverState stateBeforeEnumeration = null;
        if (solver.getStyle() == MiniSat.SolverStyle.MINISAT && solver.isIncremental()) {
            stateBeforeEnumeration = solver.saveState();
        }
        boolean proceed = true;
        final LNGIntVector relevantIndices;
        if (variables == null) {
            if (!solver.getConfig().isAuxiliaryVariablesInModels()) {
                relevantIndices = new LNGIntVector();
                for (final Map.Entry<String, Integer> entry : solver.underlyingSolver().getName2idx().entrySet()) {
                    if (solver.isRelevantVariable(entry.getKey())) {
                        relevantIndices.push(entry.getValue());
                    }
                }
            } else {
                relevantIndices = null;
            }
        } else {
            relevantIndices = new LNGIntVector(variables.size());
            for (final Variable var : variables) {
                relevantIndices.push(solver.underlyingSolver().idxForName(var.name()));
            }
        }
        LNGIntVector relevantAllIndices = null;
        final SortedSet<Variable> uniqueAdditionalVariables =
                new TreeSet<>(additionalVariables == null ? Collections.emptyList() : additionalVariables);
        if (variables != null) {
            uniqueAdditionalVariables.removeAll(variables);
        }
        if (relevantIndices != null) {
            if (uniqueAdditionalVariables.isEmpty()) {
                relevantAllIndices = relevantIndices;
            } else {
                relevantAllIndices = new LNGIntVector(relevantIndices.size() + uniqueAdditionalVariables.size());
                for (int i = 0; i < relevantIndices.size(); ++i) {
                    relevantAllIndices.push(relevantIndices.get(i));
                }
                for (final Variable var : uniqueAdditionalVariables) {
                    relevantAllIndices.push(solver.underlyingSolver().idxForName(var.name()));
                }
            }
        }
        while (proceed && modelEnumerationSATCall(solver, this.handler)) {
            final LNGBooleanVector modelFromSolver = solver.underlyingSolver().model();
            final Assignment model = solver.createAssignment(modelFromSolver, relevantAllIndices, this.fastEvaluable);
            models.add(model);
            proceed = this.handler == null || this.handler.foundModel(model);
            if (model.size() > 0) {
                final LNGIntVector blockingClause = generateBlockingClause(modelFromSolver, relevantIndices);
                solver.underlyingSolver().addClause(blockingClause, null);
                resultSetter.accept(UNDEF);
            } else {
                break;
            }
        }
        if (solver.getStyle() == MiniSat.SolverStyle.MINISAT && solver.isIncremental()) {
            solver.loadState(stateBeforeEnumeration);
        }
        return models;
    }

    protected SortedSet<Variable> getVarsForEnumeration(final Collection<Variable> knownVariables) {
        final SortedSet<Variable> relevantVars = knownVariables.stream().filter(this::isNotAuxiliaryVariable).collect(Collectors.toCollection(TreeSet::new));
        return this.variables == null ? relevantVars : relevantVars.stream().filter(this.variables::contains).collect(Collectors.toCollection(TreeSet::new));
    }

    private boolean isNotAuxiliaryVariable(final Variable var) {
        return !var.name().startsWith(CC_PREFIX) && !var.name().startsWith(PB_PREFIX) && !var.name().startsWith(CNF_PREFIX);
    }

    private boolean modelEnumerationSATCall(final MiniSat solver, final ModelEnumerationHandler handler) {
        if (handler == null) {
            return solver.sat((SATHandler) null) == TRUE;
        }
        final Tristate tristate = solver.sat(handler.satHandler());
        return !handler.aborted() && tristate == TRUE;
    }

    /**
     * Generates a blocking clause from a given model and a set of relevant variables.
     * @param modelFromSolver the current model for which the blocking clause should be generated
     * @param relevantVars    the indices of the relevant variables.  If {@code null} all variables are relevant.
     * @return the blocking clause for the given model and relevant variables
     */
    private LNGIntVector generateBlockingClause(final LNGBooleanVector modelFromSolver, final LNGIntVector relevantVars) {
        final LNGIntVector blockingClause;
        if (relevantVars != null) {
            blockingClause = new LNGIntVector(relevantVars.size());
            for (int i = 0; i < relevantVars.size(); i++) {
                final int varIndex = relevantVars.get(i);
                if (varIndex != -1) {
                    final boolean varAssignment = modelFromSolver.get(varIndex);
                    blockingClause.push(varAssignment ? (varIndex * 2) ^ 1 : varIndex * 2);
                }
            }
        } else {
            blockingClause = new LNGIntVector(modelFromSolver.size());
            for (int i = 0; i < modelFromSolver.size(); i++) {
                final boolean varAssignment = modelFromSolver.get(i);
                blockingClause.push(varAssignment ? (i * 2) ^ 1 : i * 2);
            }
        }
        return blockingClause;
    }

    /**
     * The builder for a model enumeration function.
     */
    public static class Builder {
        protected ModelEnumerationHandler handler;
        protected Collection<Variable> variables;
        protected Collection<Variable> additionalVariables;
        protected boolean fastEvaluable = false;
        protected SplitVariableProvider splitVariableProvider = null;
        protected boolean multipleSplits = false;

        Builder() {
            // Initialize only via factory
        }

        /**
         * Sets the model enumeration handler for this function
         * @param handler the handler
         * @return the current builder
         */
        public Builder handler(final ModelEnumerationHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets the set of variables over which the model enumeration should iterate.
         * @param variables the set of variables
         * @return the current builder
         */
        public Builder variables(final Collection<Variable> variables) {
            this.variables = variables;
            return this;
        }

        /**
         * Sets the set of variables over which the model enumeration should iterate.
         * @param variables the set of variables
         * @return the current builder
         */
        public Builder variables(final Variable... variables) {
            this.variables = Arrays.asList(variables);
            return this;
        }

        /**
         * Sets an additional set of variables which should occur in every model. Only set this field if 'variables' is non-empty.
         * @param variables the additional variables for each model
         * @return the current builder
         */
        public Builder additionalVariables(final Collection<Variable> variables) {
            this.additionalVariables = variables;
            return this;
        }

        /**
         * Sets an additional set of variables which should occur in every model. Only set this field if 'variables' is non-empty.
         * @param variables the additional variables for each model
         * @return the current builder
         */
        public Builder additionalVariables(final Variable... variables) {
            this.additionalVariables = Arrays.asList(variables);
            return this;
        }

        /**
         * Sets the flag whether the created assignment should be {@link Assignment#fastEvaluable() fast evaluable} assignments.
         * @param fastEvaluable {@code true} if the created assignment should be fast evaluable, otherwise {@code false}
         * @return the builder
         */
        public Builder fastEvaluable(final boolean fastEvaluable) {
            this.fastEvaluable = fastEvaluable;
            return this;
        }

        /**
         * Sets the split variable provider. If no split variable provider is given, enumeration is performed without splits. Else the enumeration is
         * performed with the split variables provided by the {@link SplitVariableProvider}.
         * @param splitVariableProvider the given split variable provider
         * @return the builder
         */
        public Builder splitVariableProvider(final SplitVariableProvider splitVariableProvider) {
            this.splitVariableProvider = splitVariableProvider;
            return this;
        }

        public Builder multipleSplits(final boolean multipleSplits) {
            this.multipleSplits = multipleSplits;
            return this;
        }

        /**
         * Builds the model enumeration function with the current builder's configuration.
         * @return the model enumeration function
         */
        public ModelEnumerationFunction build() {
            return new ModelEnumerationFunction(this.handler, this.variables, this.additionalVariables, this.fastEvaluable, this.splitVariableProvider,
                    multipleSplits);
        }
    }
}
