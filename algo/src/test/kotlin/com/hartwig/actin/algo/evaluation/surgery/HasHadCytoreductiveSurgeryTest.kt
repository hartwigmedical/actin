package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withSurgeriesAndOncologicalHistory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test


class HasHadCytoreductiveSurgeryTest {

    private val function = HasHadCytoreductiveSurgery()

    @Test
    fun `Should fail with no surgeries in history`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                createPatientRecord(
                    treatmentName = "Hormone therapy",
                    categories = setOf(TreatmentCategory.HORMONE_THERAPY))
            ),
            "Has not received cytoreductive surgery"
        )
    }

    @Test
    fun `Should fail with non cytoreductive surgery in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(
            createPatientRecord(
                treatmentName = "Nephrectomy",
                categories = setOf(TreatmentCategory.SURGERY)
            )
        ),
            "Has not received cytoreductive surgery")
    }

    @Test
    fun `Should pass with history of cytoreductive surgery or HIPEC`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(
            createPatientRecord(
                treatmentName = "HIPEC",
                categories = setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ),
            "Has had cytoreductive surgery")
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createPatientRecord(
                    treatmentName = "Cytoreductive surgery",
                    categories = setOf(TreatmentCategory.SURGERY),
                    types = setOf(OtherTreatmentType.CYTOREDUCTIVE_SURGERY)
                )
            ),
            "Has had cytoreductive surgery"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createPatientRecord(
                    treatmentName = "Colorectal cancer cytoreduction",
                    categories = setOf(TreatmentCategory.SURGERY),
                    types = setOf(OtherTreatmentType.CYTOREDUCTIVE_SURGERY)
                )
            ),
            "Has had cytoreductive surgery"
        )

    }

    @Test
    fun `Should return undetermined if surgery not specified in history`() {
        val record = createPatientRecord(
            treatmentName = "Surgery",
            categories = setOf(TreatmentCategory.SURGERY),
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record), "Undetermined if performed surgery was cytoreductive")
    }

    @Test
    fun `Should return undetermined if debulking surgery is performed in oncological history and and irrelevant surgery in record`() {
        val record = createPatientRecord(
            treatmentName = "Debulking",
            categories = setOf(TreatmentCategory.SURGERY),
            types = setOf(OtherTreatmentType.DEBULKING_SURGERY),
            surgeryName = "surgery",
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record),
            "Undetermined if performed debulking surgery meets the criteria of cytoreductive surgery"
        )

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                createPatientRecord(
                    "complete debulking",
                    setOf(TreatmentCategory.SURGERY),
                    setOf(OtherTreatmentType.DEBULKING_SURGERY)
                )
            ),
            "Undetermined if performed debulking surgery meets the criteria of cytoreductive surgery"
        )
    }

    @Test
    fun `Should fail with irrelevant surgery in record and hormone therapy`() {
        val record = createPatientRecord(
            treatmentName = "Hormone therapy",
            categories = setOf(TreatmentCategory.HORMONE_THERAPY),
            surgeryName = "surgery",
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record),
            "Has not received cytoreductive surgery"
        )
    }

    @Test
    fun `Should pass with cytoreductive surgery in record with irrelevant oncological history`() {
        val record = createPatientRecord(
            treatmentName = "Hormone therapy",
            categories = setOf(TreatmentCategory.HORMONE_THERAPY),
            surgeryName = "surgery",
            treatmentType =  OtherTreatmentType.CYTOREDUCTIVE_SURGERY,
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "Has had cytoreductive surgery"
        )
    }

    @Test
    fun `Should return undetermined with debulking surgery in record`() {
        val record = createPatientRecord(
            treatmentName = "Hormone therapy",
            categories = setOf(TreatmentCategory.HORMONE_THERAPY),
            surgeryName = "surgery",
            treatmentType =  OtherTreatmentType.DEBULKING_SURGERY,
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record),
            "Undetermined if performed debulking surgery meets the criteria of cytoreductive surgery"
        )
    }

    private fun createPatientRecord(
        treatmentName: String? = null,
        categories: Set<TreatmentCategory> = emptySet(),
        types: Set<OtherTreatmentType> = emptySet(),
        surgeryName: String? = null,
        treatmentType: OtherTreatmentType = OtherTreatmentType.OTHER_SURGERY,
    ): PatientRecord {
        val treatments = listOfNotNull(treatmentName?.let {
            treatmentHistoryEntry(treatments = setOf(treatment(name = it, isSystemic = false, categories = categories, types = types)))
        })
        val surgeries = listOfNotNull(surgeryName?.let {
            Surgery(name = it, status = SurgeryStatus.FINISHED, endDate = null, treatmentType = treatmentType)
        })
        return withSurgeriesAndOncologicalHistory(treatments, surgeries)
    }
}