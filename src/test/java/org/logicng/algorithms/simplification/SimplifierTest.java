package org.logicng.algorithms.simplification;

import org.junit.jupiter.api.Test;
import org.logicng.RandomTag;
import org.logicng.formulas.F;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.predicates.satisfiability.TautologyPredicate;
import org.logicng.util.FormulaCornerCases;
import org.logicng.util.FormulaRandomizer;
import org.logicng.util.FormulaRandomizerConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit Tests for the class {@link Simplifier}.
 * @version 2.0.0
 * @since 2.0.0
 */
public class SimplifierTest {

    private final FormulaFactory f = F.f;
    private final Simplifier simplifier = new Simplifier(new DefaultRatingFunction());

    @Test
    public void testConstants() {
        assertThat(this.f.falsum().transform(this.simplifier)).isEqualTo(this.f.falsum());
        assertThat(this.f.verum().transform(this.simplifier)).isEqualTo(this.f.verum());
    }

    @Test
    public void testCornerCases() {
        final FormulaFactory f = new FormulaFactory();
        final FormulaCornerCases cornerCases = new FormulaCornerCases(f);
        cornerCases.cornerCases().forEach(this::computeAndVerify);
    }

    @Test
    @RandomTag
    public void testRandomized() {
        for (int i = 0; i < 100; i++) {
            final FormulaFactory f = new FormulaFactory();
            final FormulaRandomizer randomizer = new FormulaRandomizer(f, FormulaRandomizerConfig.builder().numVars(8).weightPbc(2).seed(42).build());
            final Formula formula = randomizer.formula(5);
            computeAndVerify(formula);
        }
    }

    private void computeAndVerify(final Formula formula) {
        final Formula simplified = formula.transform(this.simplifier);
        assertThat(this.f.equivalence(formula, simplified).holds(new TautologyPredicate(this.f)))
                .as("Minimized formula is equivalent to original Formula")
                .isTrue();
    }
}