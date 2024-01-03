package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryTest {

    @Test
    fun `Should extract names from treatment history`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(chemotherapy(TREATMENT_1), chemotherapy(TREATMENT_2))
        )
        assertThat(treatmentHistoryEntry.treatmentName()).isEqualTo(TREATMENT_1 + ";" + TREATMENT_2)
    }

    @Test
    fun `Should extract treatment display strings from treatment history`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(chemotherapy(TREATMENT_1), chemotherapy(TREATMENT_2))
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Treatment 1;Treatment 2")
    }

    @Test
    fun `Should display chemoradiation when only components are chemotherapy and radiation`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(chemotherapy("chemotherapy"), RADIOTHERAPY)
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation")
    }

    @Test
    fun `Should display chemoradiation and other treatment when components are chemotherapy and radiation and other treatment`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(
                chemotherapy("chemotherapy"),
                RADIOTHERAPY,
                OtherTreatment(
                    name = "ablation",
                    isSystemic = false,
                    synonyms = emptySet(),
                    categories = setOf(TreatmentCategory.ABLATION)
                )
            )
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation and Ablation")
    }

    @Test
    fun `Should display chemoradiation with chemo drug when components are chemotherapy and radiation and chemo drug treatment`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(chemotherapy("chemotherapy"), RADIOTHERAPY, chemotherapy("chemo drug"))
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation (with Chemo drug)")
    }

    @Test
    fun `Should display normally when components are chemotherapy and radiation and multiple additional treatments`() {
        val treatmentHistoryEntry = TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.copy(
            treatments = setOf(
                chemotherapy("chemotherapy"),
                RADIOTHERAPY,
                chemotherapy("chemo drug"),
                OtherTreatment(
                    name = "ablation",
                    isSystemic = false,
                    synonyms = emptySet(),
                    categories = setOf(TreatmentCategory.ABLATION),
                )
            )
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Ablation;Chemo drug;Chemotherapy;Radiotherapy")
    }

    @Test
    fun `Should include switch and maintenance treatments when present`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.allTreatments()).isEqualTo(
            setOf(
                TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.treatments.first(),
                SWITCH_TREATMENT_STAGE.treatment,
                MAINTENANCE_TREATMENT_STAGE.treatment
            )
        )
    }

    @Test
    fun `Should display base name without switch and maintenance treatments`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.treatmentDisplay()).isEqualTo("Test treatment")
    }

    @Test
    fun `Should return treatment categories`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY)
    }

    @Test
    fun `Should include switch and maintenance treatment categories in treatment categories`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.categories()).containsExactlyInAnyOrder(
            TreatmentCategory.CHEMOTHERAPY,
            TreatmentCategory.TARGETED_THERAPY,
            TreatmentCategory.SUPPORTIVE_TREATMENT
        )
    }

    @Test
    fun `Should return null for type not configured when matching against type`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isNull()
    }

    @Test
    fun `Should return true for matching type`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isTrue()
    }

    @Test
    fun `Should return false for type that does not match`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.ANTIMETABOLITE)).isFalse()
    }

    @Test
    fun `Should return true for type that matches switch treatment`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.ALK_INHIBITOR)).isTrue()
    }

    @Test
    fun `Should return true for type that matches maintenance treatment`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.STEROID)).isTrue()
    }

    @Test
    fun `Should return true when type configured`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.hasTypeConfigured()).isTrue()
    }

    @Test
    fun `Should return false when type not configured`() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.hasTypeConfigured()).isFalse()
    }

    @Test
    fun `Should return null for type not configured when matching against set of types`() {
        assertThat(
            TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.PLATINUM_COMPOUND))
        ).isNull()
    }

    @Test
    fun `Should return true for switch type that matches set of types`() {
        assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.ALK_INHIBITOR))
        ).isTrue()
    }

    @Test
    fun `Should return true for maintenance type that matches set of types`() {
        assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.STEROID))
        ).isTrue()
    }

    @Test
    fun `Should return true for type that matches set of types`() {
        assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.PLATINUM_COMPOUND))
        ).isTrue()
    }

    @Test
    fun `Should return false for type that does not match set of types`() {
        assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.ANTHRACYCLINE))
        ).isFalse()
    }

    companion object {
        private val TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE = treatmentHistoryEntryWithDrugTypes(emptySet())
        private val TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE = treatmentHistoryEntryWithDrugTypes(setOf(DrugType.PLATINUM_COMPOUND))
        val SWITCH_TREATMENT_STAGE = treatmentStage(
            drugTreatment("SWITCH TREATMENT", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
        )
        val MAINTENANCE_TREATMENT_STAGE = treatmentStage(
            drugTreatment("MAINTENANCE TREATMENT", TreatmentCategory.SUPPORTIVE_TREATMENT, setOf(DrugType.STEROID))
        )
        val TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE = treatmentHistoryEntry(
            treatments = setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))),
            switchToTreatments = listOf(SWITCH_TREATMENT_STAGE),
            maintenanceTreatment = MAINTENANCE_TREATMENT_STAGE
        )

        private val RADIOTHERAPY: Radiotherapy = Radiotherapy("radiotherapy")
        private const val TREATMENT_1 = "TREATMENT_1"
        private const val TREATMENT_2 = "TREATMENT_2"

        private fun treatmentHistoryEntryWithDrugTypes(types: Set<DrugType>): TreatmentHistoryEntry {
            return treatmentHistoryEntry(treatments = setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY, types)))
        }

        private fun chemotherapy(name: String): DrugTreatment {
            return drugTreatment(name, TreatmentCategory.CHEMOTHERAPY)
        }
    }
}