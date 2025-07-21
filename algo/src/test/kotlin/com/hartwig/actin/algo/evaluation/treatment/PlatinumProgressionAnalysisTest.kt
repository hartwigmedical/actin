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

class PlatinumProgressionAnalysisTest {

    private val referenceDate = LocalDate.of(2025, 2, 5)
    private val recentDate = LocalDate.of(2025, 2, 5).minusMonths(2)
    private val nonRecentDate = LocalDate.of(2025, 2, 5).minusMonths(9)

    private val platinum = DrugTreatment(
        name = "Carboplatin",
        drugs = setOf(Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)))
    )

    @Test
    fun `Should return null if treatment history is empty`() {
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(emptyList()), referenceDate)
        assertThat(base.hasProgressionOnLastPlatinumWithinSixMonths()).isNull()
        assertThat(base.hasProgressionOnFirstPlatinumWithinMonths(6)).isNull()
        assertThat(base.hasProgressionOrUnknownProgressionOnLastPlatinum()).isNull()
        assertThat(base.hasProgressionOrUnknownProgressionOnFirstPlatinum()).isNull()
        assertThat(base.lastPlatinumTreatment).isNull()
        assertThat(base.firstPlatinumTreatment).isNull()
    }

    @Test
    fun `Should return false if treatment history contains platinum but without progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.TOXICITY,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate)
        assertThat(base.hasProgressionOrUnknownProgressionOnLastPlatinum()).isFalse()
        assertThat(base.hasProgressionOrUnknownProgressionOnFirstPlatinum()).isFalse()
    }


    @Test
    fun `Should return true if treatment history contains platinum but unknown if progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate)
        assertThat(base.hasProgressionOrUnknownProgressionOnLastPlatinum()).isTrue()
        assertThat(base.hasProgressionOrUnknownProgressionOnFirstPlatinum()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains platinum with progression but long time ago`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                stopYear = nonRecentDate.year,
                stopMonth = nonRecentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate)
        assertThat(base.hasProgressionOnLastPlatinumWithinSixMonths()).isFalse()
        assertThat(base.hasProgressionOnFirstPlatinumWithinMonths(6)).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum with progression and within 6 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate)
        assertThat(base.hasProgressionOnLastPlatinumWithinSixMonths()).isTrue()
        assertThat(base.hasProgressionOnFirstPlatinumWithinMonths(6)).isTrue()
    }

    @Test
    fun `Should use correct platinum treatment if multiple`() {
        val first = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(platinum),
            stopYear = recentDate.year - 1,
            stopMonth = recentDate.monthValue - 1
        )
        val second = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(platinum),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            stopYear = recentDate.year,
            stopMonth = recentDate.monthValue
        )
        val history = listOf(first, second)
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate)
        assertThat(base.firstPlatinumTreatment?.startYear).isEqualTo(first.startYear)
        assertThat(base.lastPlatinumTreatment?.startYear).isEqualTo(second.startYear)
    }
}