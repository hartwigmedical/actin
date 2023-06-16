package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLimitedCumulativeAnthracyclineExposureTest {
    @Test
    fun canEvaluate() {
        val function = HasLimitedCumulativeAnthracyclineExposure(TestDoidModelFactory.createMinimalTestDoidModel())

        // PASS when no information relevant to anthracycline is provided.
        assertEvaluation(EvaluationResult.PASS, function.evaluate(create(null, emptyList(), emptyList())))

        // PASS with one generic chemo for non-suspicious cancer type
        val genericChemo: PriorTumorTreatment = TreatmentTestFactory.builder().addCategories(
            TreatmentCategory.CHEMOTHERAPY
        ).build()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(create(setOf("other cancer type"), emptyList(), listOf(genericChemo)))
        )
        val firstSuspiciousCancerType = HasLimitedCumulativeAnthracyclineExposure.CANCER_DOIDS_FOR_ANTHRACYCLINE.iterator().next()
        // PASS when pt has prior second primary with different treatment history
        val suspectTumorTypeWithOther: PriorSecondPrimary =
            TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).treatmentHistory("other").build()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(create(null, listOf(suspectTumorTypeWithOther), emptyList()))
        )

        // UNDETERMINED in case the patient had prior second primary with suspicious prior treatment
        val firstSuspiciousTreatment = HasLimitedCumulativeAnthracyclineExposure.PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.iterator().next()
        val suspectTumorTypeWithSuspectTreatment: PriorSecondPrimary = TreatmentTestFactory.priorSecondPrimaryBuilder()
            .addDoids(firstSuspiciousCancerType)
            .treatmentHistory(firstSuspiciousTreatment)
            .build()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(create(null, listOf(suspectTumorTypeWithSuspectTreatment), emptyList()))
        )

        // UNDETERMINED in case the patient had prior second primary with no prior treatment recorded
        val suspectTumorTypeWithoutKnownTreatment: PriorSecondPrimary =
            TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).build()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(create(null, listOf(suspectTumorTypeWithoutKnownTreatment), emptyList()))
        )

        // UNDETERMINED when chemo with no type is provided and tumor type is suspicious.
        val priorChemoWithoutType: PriorTumorTreatment =
            TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                create(
                    setOf(firstSuspiciousCancerType),
                    emptyList(),
                    listOf(priorChemoWithoutType)
                )
            )
        )

        // UNDETERMINED when actual anthracycline is provided regardless of tumor type
        val priorAnthracycline: PriorTumorTreatment = TreatmentTestFactory.builder()
            .addCategories(TreatmentCategory.CHEMOTHERAPY)
            .chemoType(HasLimitedCumulativeAnthracyclineExposure.ANTHRACYCLINE_CHEMO_TYPE)
            .build()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(create(null, emptyList(), listOf(priorAnthracycline)))
        )
    }

    companion object {
        private fun create(
            tumorDoids: Set<String>?, priorSecondPrimaries: List<PriorSecondPrimary>,
            priorTumorTreatments: List<PriorTumorTreatment>
        ): PatientRecord {
            val base = TestDataFactory.createMinimalTestPatientRecord()
            return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .tumor(ImmutableTumorDetails.builder().from(base.clinical().tumor()).doids(tumorDoids).build())
                        .priorTumorTreatments(priorTumorTreatments)
                        .priorSecondPrimaries(priorSecondPrimaries)
                        .build()
                )
                .build()
        }
    }
}