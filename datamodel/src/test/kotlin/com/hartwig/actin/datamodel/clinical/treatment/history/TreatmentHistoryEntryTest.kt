package com.hartwig.actin.datamodel.clinical.treatment.history

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryTest {

    private val treatmentHistoryEntryWithoutType = treatmentHistoryEntryWithDrugTypes(emptySet())
    private val treatmentHistoryEntryWithDrugType = treatmentHistoryEntryWithDrugTypes(setOf(DrugType.PLATINUM_COMPOUND))
    private val switchTreatmentStage = treatmentStage(
        drugTreatment("SWITCH TREATMENT", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
    )
    private val maintenanceTreatmentStage = treatmentStage(
        drugTreatment("MAINTENANCE TREATMENT", TreatmentCategory.SUPPORTIVE_TREATMENT, setOf(DrugType.STEROID))
    )
    private val treatmentHistoryEntryWithSwitchAndMaintenance = treatmentHistoryEntry(
        treatments = setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))),
        switchToTreatments = listOf(switchTreatmentStage),
        maintenanceTreatment = maintenanceTreatmentStage
    )

    private val radiotherapy: Radiotherapy = Radiotherapy("radiotherapy")
    private val treatment1 = "TREATMENT_1"
    private val treatment2 = "TREATMENT_2"

    @Test
    fun `Should extract names from treatment history`() {
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(chemotherapy(treatment1), chemotherapy(treatment2))
        )
        assertThat(treatmentHistoryEntry.treatmentName()).isEqualTo("$treatment1;$treatment2")
    }

    @Test
    fun `Should extract treatment display strings from treatment history`() {
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(chemotherapy(treatment1), chemotherapy(treatment2))
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Treatment 1;Treatment 2")
    }

    @Test
    fun `Should display chemoradiation when only components are chemotherapy and radiation`() {
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(chemotherapy("chemotherapy"), radiotherapy)
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation")
    }

    @Test
    fun `Should display chemoradiation and other treatment when components are chemotherapy and radiation and other treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(
                chemotherapy("chemotherapy"),
                radiotherapy,
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
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(chemotherapy("chemotherapy"), radiotherapy, chemotherapy("chemo drug"))
        )
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation (with Chemo drug)")
    }

    @Test
    fun `Should display normally when components are chemotherapy and radiation and multiple additional treatments`() {
        val treatmentHistoryEntry = treatmentHistoryEntryWithDrugType.copy(
            treatments = setOf(
                chemotherapy("chemotherapy"),
                radiotherapy,
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
        assertThat(treatmentHistoryEntryWithSwitchAndMaintenance.allTreatments()).isEqualTo(
            setOf(
                treatmentHistoryEntryWithDrugType.treatments.first(),
                switchTreatmentStage.treatment,
                maintenanceTreatmentStage.treatment
            )
        )
    }

    @Test
    fun `Should display base name without switch and maintenance treatments`() {
        assertThat(treatmentHistoryEntryWithSwitchAndMaintenance.treatmentDisplay()).isEqualTo("Test treatment")
    }

    @Test
    fun `Should return treatment categories`() {
        assertThat(treatmentHistoryEntryWithDrugType.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY)
    }

    @Test
    fun `Should include switch and maintenance treatment categories in treatment categories`() {
        assertThat(treatmentHistoryEntryWithSwitchAndMaintenance.categories()).containsExactlyInAnyOrder(
            TreatmentCategory.CHEMOTHERAPY,
            TreatmentCategory.TARGETED_THERAPY,
            TreatmentCategory.SUPPORTIVE_TREATMENT
        )
    }

    @Test
    fun `Should return null for type not configured when matching against type`() {
        assertThat(treatmentHistoryEntryWithoutType.isOfType(DrugType.PLATINUM_COMPOUND)).isNull()
    }

    @Test
    fun `Should return true for matching type`() {
        assertThat(treatmentHistoryEntryWithDrugType.isOfType(DrugType.PLATINUM_COMPOUND)).isTrue()
    }

    @Test
    fun `Should return false for type that does not match`() {
        assertThat(treatmentHistoryEntryWithDrugType.isOfType(DrugType.ANTIMETABOLITE)).isFalse()
    }

    @Test
    fun `Should return true for type that matches switch treatment`() {
        assertThat(treatmentHistoryEntryWithSwitchAndMaintenance.isOfType(DrugType.ALK_INHIBITOR)).isTrue()
    }

    @Test
    fun `Should return true for type that matches maintenance treatment`() {
        assertThat(treatmentHistoryEntryWithSwitchAndMaintenance.isOfType(DrugType.STEROID)).isTrue()
    }

    @Test
    fun `Should return true when type configured`() {
        assertThat(treatmentHistoryEntryWithDrugType.hasTypeConfigured()).isTrue()
    }

    @Test
    fun `Should return false when type not configured`() {
        assertThat(treatmentHistoryEntryWithoutType.hasTypeConfigured()).isFalse()
    }

    @Test
    fun `Should return null for type not configured when matching against set of types`() {
        assertThat(
            treatmentHistoryEntryWithoutType.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.PLATINUM_COMPOUND))
        ).isNull()
    }

    @Test
    fun `Should return true for switch type that matches set of types`() {
        assertThat(
            treatmentHistoryEntryWithSwitchAndMaintenance.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.ALK_INHIBITOR))
        ).isTrue()
    }

    @Test
    fun `Should return true for maintenance type that matches set of types`() {
        assertThat(
            treatmentHistoryEntryWithSwitchAndMaintenance.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.STEROID))
        ).isTrue()
    }

    @Test
    fun `Should return true for type that matches set of types`() {
        assertThat(
            treatmentHistoryEntryWithDrugType.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.PLATINUM_COMPOUND))
        ).isTrue()
    }

    @Test
    fun `Should return false for type that does not match set of types`() {
        assertThat(
            treatmentHistoryEntryWithDrugType.matchesTypeFromSet(setOf(DrugType.ANTIMETABOLITE, DrugType.ANTHRACYCLINE))
        ).isFalse()
    }

    private fun treatmentHistoryEntryWithDrugTypes(types: Set<DrugType>): TreatmentHistoryEntry {
        return treatmentHistoryEntry(treatments = setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY, types)))
    }

    private fun chemotherapy(name: String): DrugTreatment {
        return drugTreatment(name, TreatmentCategory.CHEMOTHERAPY)
    }
}