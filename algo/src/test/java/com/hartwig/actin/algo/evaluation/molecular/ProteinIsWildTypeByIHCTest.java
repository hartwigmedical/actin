package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.junit.Test;

public class ProteinIsWildTypeByIHCTest {

    public static final String PROTEIN = "p53";

    @Test
    public void shouldReturnUndeterminedForEmptyListOfTests() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(Collections.emptyList())));
    }

    @Test
    public void shouldReturnUndeterminedForTestsThatDoNotMeetCriteria() {
        List<PriorMolecularTest> priorTests = List.of(builder().test("other").build(), builder().item("other").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)));
    }

    @Test
    public void shouldReturnPassWhenAllMatchingTestsIndicateWildType() {
        List<PriorMolecularTest> priorTests = List.of(builder().test("other").build(),
                builder().item("other").build(),
                builder().build(),
                builder().scoreText("WILD TYPE").build(),
                builder().scoreText("WILD-type").build());
        assertEvaluation(EvaluationResult.PASS, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)));
    }

    @Test
    public void shouldReturnUndeterminedWhenSomeMatchingTestsDoNotIndicateWildType() {
        List<PriorMolecularTest> priorTests = List.of(builder().build(),
                builder().scoreText("WILD TYPE").build(),
                builder().scoreText("WILD-type").build(),
                builder().scoreText("other").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)));
    }

    private static ProteinIsWildTypeByIHC function() {
        return new ProteinIsWildTypeByIHC(PROTEIN);
    }

    private static ImmutablePriorMolecularTest.Builder builder() {
        return MolecularTestFactory.priorBuilder().test("IHC").item(PROTEIN).scoreText("WildType");
    }
}