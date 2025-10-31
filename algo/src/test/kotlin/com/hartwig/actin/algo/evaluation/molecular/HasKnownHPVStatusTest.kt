package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import org.junit.Test

class HasKnownHPVStatusTest {

    private val function = HasKnownHPVStatus()

    @Test
    fun `Should pass when WGS test contains sufficient tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_WHOLE_GENOME, true
                )
            )
        )
    }

    @Test
    fun `Should pass when molecular test contains HPV`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndVirus(
                    type = ExperimentType.PANEL,
                    virus = TestMolecularFactory.createMinimalVirus().copy(type = VirusType.HPV)
                )
            )
        )
    }

    @Test
    fun `Should pass if no WGS performed but correct test is in molecularTest`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            ihcTests = listOf(MolecularTestFactory.ihcTest(item = "HPV", impliesIndeterminate = false))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }


    @Test
    fun `Should pass if WGS does not contain enough tumor cells but correct test is in molecularTest`() {
        val record = MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            ihcTests = listOf(MolecularTestFactory.ihcTest(item = "HPV", impliesIndeterminate = false))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should warn if no WGS has been performed and correct test is in molecularTest with indeterminate status`() {
        val record = MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            ihcTests =
            listOf(
                MolecularTestFactory.ihcTest(
                    item = "HPV", impliesIndeterminate = true
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
    }

    @Test
    fun `Should fail if WGS does not contain enough tumor cells and no correct test in prior molecular tests `() {
        val record = MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            ihcTests = listOf(MolecularTestFactory.ihcTest(item = "Something"))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail if no WGS performed and correct item not in prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                    ihcTests = listOf(
                        MolecularTestFactory.ihcTest(
                            item = "Something",
                            impliesIndeterminate = false
                        )
                    )
                )
            )
        )
    }
}