package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryEntryFunctionsTest {
    private val predicate: (Treatment) -> Boolean = { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }

    @Test
    fun `Should return unmodified entry for matching single-stage treatment`() {
        val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)))
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isEqualTo(entry)
    }

    @Test
    fun `Should return single-stage entry with aggregated treatments and cycles for matching multi-stage treatment`() {
        val switchToTreatment = treatmentStage(drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), cycles = 3)
        val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY))
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = entry.treatments + switchToTreatment.treatment + maintenanceTreatment.treatment,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 5, switchToTreatments = emptyList(), maintenanceTreatment = null
                    )
                )
            )
    }

    @Test
    fun `Should override stop date in treatment details when later treatment stages do not match`() {
        val switchToTreatment = treatmentStage(
            drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY), startYear = 2020, startMonth = 1, cycles = 3
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)),
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        stopYear = switchToTreatment.startYear,
                        stopMonth = switchToTreatment.startMonth,
                        switchToTreatments = emptyList(),
                        maintenanceTreatment = null
                    )
                )
            )
    }

    @Test
    fun `Should return single-stage entry with aggregated treatments and cycles for partially matching multi-stage treatment`() {
        val switchToTreatment = treatmentStage(
            drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2020, startMonth = 1, cycles = 3
        )
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = entry.treatments + switchToTreatment.treatment,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 5,
                        switchToTreatments = emptyList(),
                        maintenanceTreatment = null,
                        stopYear = maintenanceTreatment.startYear,
                        stopMonth = maintenanceTreatment.startMonth
                    )
                )
            )
    }

    @Test
    fun `Should not alter stop date when intermediate stage does not match`() {
        val switchToTreatment = treatmentStage(
            drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY), startYear = 2020, startMonth = 1, cycles = 3
        )
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = entry.treatments + maintenanceTreatment.treatment,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 2,
                        switchToTreatments = emptyList(),
                        maintenanceTreatment = null
                    )
                )
            )
    }

    @Test
    fun `Should return entry representing matching stages when base treatment does not match`() {
        val switchToTreatment = treatmentStage(
            drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), startYear = 2020, startMonth = 1, cycles = 3
        )
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT), startYear = 2021, startMonth = 4
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            startYear = 2019,
            startMonth = 8,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate))
            .isEqualTo(
                entry.copy(
                    treatments = setOf(switchToTreatment.treatment),
                    startYear = switchToTreatment.startYear,
                    startMonth = switchToTreatment.startMonth,
                    treatmentHistoryDetails = entry.treatmentHistoryDetails!!.copy(
                        cycles = 3,
                        switchToTreatments = emptyList(),
                        maintenanceTreatment = null,
                        stopYear = maintenanceTreatment.startYear,
                        stopMonth = maintenanceTreatment.startMonth,
                    )
                )
            )
    }

    @Test
    fun `Should return null when no stages of entry match`() {
        val switchToTreatment = treatmentStage(
            drugTreatment("switch treatment", TreatmentCategory.TARGETED_THERAPY)
        )
        val maintenanceTreatment = treatmentStage(
            drugTreatment("maintenance treatment", TreatmentCategory.SUPPORTIVE_TREATMENT)
        )
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.TARGETED_THERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2,
            stopYear = 2021,
            stopMonth = 10
        )
        assertThat(TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry, predicate)).isNull()
    }

    @Test
    fun `Should display switch and maintenance treatments when present`() {
        val switchToTreatment = treatmentStage(drugTreatment("switch treatment", TreatmentCategory.CHEMOTHERAPY), cycles = 3)
        val maintenanceTreatment = treatmentStage(drugTreatment("maintenance treatment", TreatmentCategory.CHEMOTHERAPY))
        val entry = treatmentHistoryEntry(
            setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)),
            switchToTreatments = listOf(switchToTreatment),
            maintenanceTreatment = maintenanceTreatment,
            numCycles = 2
        )
        assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo(
            "Test treatment with switch to Switch treatment continued with Maintenance treatment maintenance"
        )
    }

    @Test
    fun `Should display base treatment when no switch and maintenance treatments present`() {
        val entry = treatmentHistoryEntry(setOf(drugTreatment("test treatment", TreatmentCategory.CHEMOTHERAPY)), numCycles = 2)
        assertThat(TreatmentHistoryEntryFunctions.fullTreatmentDisplay(entry)).isEqualTo("Test treatment")
    }
}