package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
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
                ImmutableTreatmentHistoryEntry.copyOf(entry)
                    .withTreatments(entry.treatments() + switchToTreatment.treatment() + maintenanceTreatment.treatment())
                    .withTreatmentHistoryDetails(
                        ImmutableTreatmentHistoryDetails.copyOf(entry.treatmentHistoryDetails()!!)
                            .withCycles(5)
                            .withSwitchToTreatments(emptyList())
                            .withMaintenanceTreatment(null)
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
                ImmutableTreatmentHistoryEntry.copyOf(entry)
                    .withTreatmentHistoryDetails(
                        ImmutableTreatmentHistoryDetails.copyOf(entry.treatmentHistoryDetails()!!)
                            .withStopYear(switchToTreatment.startYear())
                            .withStopMonth(switchToTreatment.startMonth())
                            .withSwitchToTreatments(emptyList())
                            .withMaintenanceTreatment(null)
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
                ImmutableTreatmentHistoryEntry.copyOf(entry)
                    .withTreatments(entry.treatments() + switchToTreatment.treatment())
                    .withTreatmentHistoryDetails(
                        ImmutableTreatmentHistoryDetails.copyOf(entry.treatmentHistoryDetails()!!)
                            .withCycles(5)
                            .withSwitchToTreatments(emptyList())
                            .withMaintenanceTreatment(null)
                            .withStopYear(maintenanceTreatment.startYear())
                            .withStopMonth(maintenanceTreatment.startMonth())
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
                ImmutableTreatmentHistoryEntry.copyOf(entry)
                    .withTreatments(entry.treatments() + maintenanceTreatment.treatment())
                    .withTreatmentHistoryDetails(
                        ImmutableTreatmentHistoryDetails.copyOf(entry.treatmentHistoryDetails()!!)
                            .withCycles(2)
                            .withSwitchToTreatments(emptyList())
                            .withMaintenanceTreatment(null)
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
                ImmutableTreatmentHistoryEntry.copyOf(entry)
                    .withTreatments(switchToTreatment.treatment())
                    .withStartYear(switchToTreatment.startYear())
                    .withStartMonth(switchToTreatment.startMonth())
                    .withTreatmentHistoryDetails(
                        ImmutableTreatmentHistoryDetails.copyOf(entry.treatmentHistoryDetails()!!)
                            .withCycles(3)
                            .withSwitchToTreatments(emptyList())
                            .withMaintenanceTreatment(null)
                            .withStopYear(maintenanceTreatment.startYear())
                            .withStopMonth(maintenanceTreatment.startMonth())
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