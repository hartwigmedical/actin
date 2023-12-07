package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.ValidatedCurationConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.Translation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val PATIENT_ID = "patient1"

class CurationResponseTest {

    @Test
    fun `Should create curation response with no warning for unique config`() {
        val configs = setOf(ValidatedCurationConfig(MedicationNameConfig("input", false, "test")))
        val response = CurationResponse.createFromConfigs(
            configs, PATIENT_ID, CurationCategory.MEDICATION_NAME, "input", "medication name", true
        )
        assertThat(response).isEqualTo(CurationResponse(configs, ExtractionEvaluation(medicationNameEvaluatedInputs = setOf("input"))))
    }

    @Test
    fun `Should create curation response with multiple result warning for config that should be unique`() {
        val configs = setOf(
            ValidatedCurationConfig(MedicationNameConfig("input", false, "test")),
            ValidatedCurationConfig(MedicationNameConfig("input", false, "another"))
        )
        val response = CurationResponse.createFromConfigs(
            configs, PATIENT_ID, CurationCategory.MEDICATION_NAME, "input", "medication name", true
        )
        val expectedEvaluation = ExtractionEvaluation(
            medicationNameEvaluatedInputs = setOf("input"),
            warnings = setOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.MEDICATION_NAME,
                    "input",
                    "Multiple medication name configs found for input 'input'"
                )
            )
        )
        assertThat(response).isEqualTo(CurationResponse(configs, expectedEvaluation))
    }

    @Test
    fun `Should create curation response without multiple result warning for config that allows multiple results`() {
        val configs = setOf(
            ValidatedCurationConfig(LesionLocationConfig("input", false, "test", LesionLocationCategory.LYMPH_NODE)),
            ValidatedCurationConfig(LesionLocationConfig("input", false, "another", LesionLocationCategory.LYMPH_NODE))
        )
        val response = CurationResponse.createFromConfigs(
            configs, PATIENT_ID, CurationCategory.LESION_LOCATION, "input", "lesion location", false
        )
        assertThat(response).isEqualTo(CurationResponse(configs, ExtractionEvaluation(lesionLocationEvaluatedInputs = setOf("input"))))
    }

    @Test
    fun `Should create curation response with config not found warning when no configs found`() {
        val configs = emptySet<ValidatedCurationConfig<LesionLocationConfig>>()
        val response = CurationResponse.createFromConfigs(
            configs, PATIENT_ID, CurationCategory.LESION_LOCATION, "input", "lesion location"
        )
        val expectedEvaluation = ExtractionEvaluation(
            lesionLocationEvaluatedInputs = setOf("input"),
            warnings = setOf(
                CurationWarning(
                    PATIENT_ID,
                    CurationCategory.LESION_LOCATION,
                    "input",
                    "Could not find lesion location config for input 'input'"
                )
            )
        )
        assertThat(response).isEqualTo(CurationResponse(configs, expectedEvaluation))
    }

    @Test
    fun `Should create curation response without warnings for found translation`() {
        val translation = Translation("input", "translated")
        val response = CurationResponse.createFromTranslation(
            translation, PATIENT_ID, CurationCategory.DOSAGE_UNIT_TRANSLATION, "input", "dosage unit"
        )
        assertThat(response).isEqualTo(
            CurationResponse(setOf(translation), ExtractionEvaluation(dosageUnitEvaluatedInputs = setOf(translation)))
        )
    }

    @Test
    fun `Should create curation response with warning for failed translation`() {
        val response = CurationResponse.createFromTranslation(
            null, PATIENT_ID, CurationCategory.DOSAGE_UNIT_TRANSLATION, "input", "dosage unit"
        )
        val expectedEvaluation = ExtractionEvaluation(
            warnings = setOf(
                CurationWarning(
                    PATIENT_ID, CurationCategory.DOSAGE_UNIT_TRANSLATION, "input", "No translation found for dosage unit: 'input'"
                )
            )
        )
        assertThat(response).isEqualTo(CurationResponse(emptySet<Translation<String>>(), expectedEvaluation))
    }
}