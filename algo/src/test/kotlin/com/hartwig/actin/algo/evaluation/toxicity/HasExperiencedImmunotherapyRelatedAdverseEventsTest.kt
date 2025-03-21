package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory.createMinimalTestWGSPatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val IMMUNO_ICD_EXTENSION = IcdConstants.IMMUNOTHERAPY_DRUG_SET.first()
private val IMMUNOTHERAPY_TOX_ENTRY = TreatmentTestFactory.treatmentHistoryEntry(
    treatments = setOf(TreatmentTestFactory.drugTreatment("immunoName", TreatmentCategory.IMMUNOTHERAPY)), stopReason = StopReason.TOXICITY
)
private val IMMUNOTHERAPY_PD_ENTRY =
    IMMUNOTHERAPY_TOX_ENTRY.copy(treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = StopReason.PROGRESSIVE_DISEASE))
private val IMMUNOTHERAPY_ALLERGY_OTHER_CONDITION =
    OtherCondition(name = "Nivolumab induced pneumonitis", icdCodes = setOf(IcdCode(IcdConstants.DRUG_ALLERGY_CODE, IMMUNO_ICD_EXTENSION)))
private val IMMUNOTHERAPY_INTOLERANCE =
    Intolerance(name = "Nivolumab intolerance", icdCodes = setOf(IcdCode("random main code", IMMUNO_ICD_EXTENSION)))
private val DATE = LocalDate.of(2025, 3, 1)

class HasExperiencedImmunotherapyRelatedAdverseEventsTest {
    private val icdModel = IcdModel.create(
        listOf(
            IcdNode(
                IcdConstants.DRUG_ALLERGY_CODE,
                listOf(IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_BLOCK),
                "Drug allergy"
            ),
            IcdNode(
                IMMUNO_ICD_EXTENSION,
                listOf(IcdConstants.MONOCLONAL_ANTIBODY_BLOCK),
                "Immunotherapy"
            )
        )
    )
    private val function = HasExperiencedImmunotherapyRelatedAdverseEvents(icdModel)

    @Test
    fun `Should fail with no treatmentHistory`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with no immunotherapy in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(IMMUNOTHERAPY_ALLERGY_OTHER_CONDITION),
            oncologicalHistory = emptyList()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail for immunotherapy without toxicity in history and no comorbidities`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = emptyList(),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail for immunotherapy without toxicity in history and no immunotherapy intolerance or toxicity or drug allergy comorbidity`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(
                OtherCondition("name", setOf(IcdCode(IcdConstants.HAND_FRACTURE_CODE, IMMUNO_ICD_EXTENSION))),
                Intolerance("name", setOf(IcdCode(IcdConstants.DRUG_ALLERGY_CODE, IcdConstants.TAXANE_CODE))),
                Toxicity("name", setOf(IcdCode(IcdConstants.HAND_FRACTURE_CODE, null)), DATE, ToxicitySource.EHR, 3)
            ),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should warn with prior immunotherapy treatment and stop reason toxicity`() {
        val treatments = listOf(IMMUNOTHERAPY_TOX_ENTRY)
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (prior immunotherapy with stop reason toxicity)"
        )
    }

    @Test
    fun `Should warn for prior immunotherapy treatment and comorbidity with drug allergy ICD main code and immunotherapy extension in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(IMMUNOTHERAPY_ALLERGY_OTHER_CONDITION),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (Nivolumab induced pneumonitis)"
        )
    }

    @Test
    fun `Should warn for prior immunotherapy treatment and intolerance with immunotherapy extension in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(IMMUNOTHERAPY_INTOLERANCE),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (Nivolumab intolerance)"
        )
    }

    @Test
    fun `Should warn for prior immunotherapy treatment and toxicity with immunotherapy extension in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(
                Toxicity(
                    "Nivolumab induced pneumonitis",
                    setOf(IcdCode(IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK, IcdConstants.IMMUNOTHERAPY_DRUG_SET.first())),
                    DATE,
                    ToxicitySource.EHR,
                    3
                )
            ),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (Nivolumab induced pneumonitis)"
        )
    }

    @Test
    fun `Should evaluate to undetermined for prior immunotherapy treatment and drug allergy comorbidity entry with unknown extension code`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(OtherCondition("Drug allergy", icdCodes = setOf(IcdCode(IcdConstants.DRUG_ALLERGY_CODE, null)))),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined for prior immunotherapy treatment and intolerance entry with drug allergy ICD main code and unknown extension code`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(Intolerance("Drug allergy", icdCodes = setOf(IcdCode(IcdConstants.DRUG_ALLERGY_CODE, null)))),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined with prior immunotherapy treatment with unknown stop reason`() {
        val treatment = IMMUNOTHERAPY_TOX_ENTRY.copy(treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatment))))
    }
}