package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PlatinumProgressionFunctionsTest {

    private val referenceDate = LocalDate.of(2025, 2, 5)
    private val recentDate = LocalDate.of(2025, 2, 5).minusMonths(2)
    private val nonRecentDate = LocalDate.of(2025, 2, 5).minusMonths(9)

    private val platinum = DrugTreatment(
        name = "Carboplatin",
        drugs = setOf(Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)))
    )

    @Test
    fun `Should return false if treatment history is empty`() {
        val base = PlatinumProgressionFunctions.create(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        assertThat(base.hasProgressionOnPlatinumWithinSixMonths(referenceDate)).isFalse()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum()).isFalse()
        assertThat(base.platinumTreatment).isNull()
    }

    @Test
    fun `Should return false if treatment history contains platinum but without progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.TOXICITY,
                startYear = recentDate.year,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionFunctions.create(TreatmentTestFactory.withTreatmentHistory(history))
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum()).isFalse()
    }


    @Test
    fun `Should return true if treatment history contains platinum but unknown if progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                startYear = recentDate.year,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionFunctions.create(TreatmentTestFactory.withTreatmentHistory(history))
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains platinum with progression but long time ago`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                startYear = nonRecentDate.year,
                startMonth = nonRecentDate.monthValue
            )
        )
        val base = PlatinumProgressionFunctions.create(TreatmentTestFactory.withTreatmentHistory(history))
        assertThat(base.hasProgressionOnPlatinumWithinSixMonths(referenceDate)).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum with progression and within 6 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                startYear = recentDate.year,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionFunctions.create(TreatmentTestFactory.withTreatmentHistory(history))
        assertThat(base.hasProgressionOnPlatinumWithinSixMonths(referenceDate)).isTrue()
    }
}