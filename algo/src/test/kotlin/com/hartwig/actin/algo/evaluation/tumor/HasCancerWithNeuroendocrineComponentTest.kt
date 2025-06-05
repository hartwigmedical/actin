package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithNeuroendocrineComponentTest {
    @Test
    fun canEvaluate() {
        val matchDoid = "matching doid"
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(
            matchDoid, NEUROENDOCRINE_TERMS.iterator().next()
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
            function.evaluate(TumorTestFactory.withDoids(NEUROENDOCRINE_DOIDS.iterator().next()))
        )

        // Pass when tumor has been annotated as neuroendocrine
        val annotation = NEUROENDOCRINE_EXTRA_DETAILS.first() + " tumor"
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorDetails(TumorDetails(primaryTumorExtraDetails = annotation)))
        )
    }

    private fun createWithNeuroendocrineProfile(): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()
        return base.copy(
            molecularHistory = MolecularHistory(
                listOf(
                    baseMolecular.copy(
                        drivers = baseMolecular.drivers.copy(
                            copyNumbers = listOf(
                                TestCopyNumberFactory.createMinimal().copy(
                                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                                    isReportable = true,
                                    gene = "TP53"
                                )
                            ),
                            homozygousDisruptions = listOf(
                                TestHomozygousDisruptionFactory.createMinimal().copy(isReportable = true, gene = "RB1")
                            )
                        )
                    )
                )
            )
        )
    }
}