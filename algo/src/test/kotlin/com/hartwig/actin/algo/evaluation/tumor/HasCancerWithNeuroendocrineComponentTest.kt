package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import org.junit.Test

class HasCancerWithNeuroendocrineComponentTest {
    @Test
    fun canEvaluate() {
        val matchDoid = "matching doid"
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(
            matchDoid, HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_TERMS.iterator().next()
        )
        val function = HasCancerWithNeuroendocrineComponent(doidModel)

        // Can't determine when nothing known about tumor
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorDetails(TumorDetails())))

        // Fail when tumor is of non-neuroendocrine type.
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("other")))

        // Can't be sure when tumor has a small cell component.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.SMALL_CELL_DOID_SET.iterator().next()))
        )

        // Can't be sure if tumor has a neuroendocrine profile
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(createWithNeuroendocrineProfile()))

        // Pass when tumor has a doid with a neuroendocrine term
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(matchDoid)))

        // Pass when tumor has a doid that is configured as neuroendocrine
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_DOIDS.iterator().next()))
        )

        // Pass when tumor has been annotated as neuroendocrine
        val annotation = HasCancerWithNeuroendocrineComponent.NEUROENDOCRINE_EXTRA_DETAILS.first() + " tumor"
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorDetails(TumorDetails(primaryTumorExtraDetails = annotation)))
        )
    }

    private fun createWithNeuroendocrineProfile(): PatientRecord {
        val base = TestPatientFactory.createMinimalTestPatientRecord()
        val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()
        return base.copy(
            molecularHistory = MolecularHistory.fromWGSandIHC(
                baseMolecular.copy(
                    drivers = baseMolecular.drivers.copy(
                        copyNumbers = setOf(
                            TestCopyNumberFactory.createMinimal().copy(type = CopyNumberType.LOSS, isReportable = true, gene = "TP53")
                        ),
                        homozygousDisruptions = setOf(TestHomozygousDisruptionFactory.createMinimal().copy(isReportable = true, gene = "RB1"))
                    )
                ),
                emptyList())
        )
    }
}