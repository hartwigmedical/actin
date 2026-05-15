package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlatinumProgressionAnalysisTest {

    private val referenceDate = LocalDate.of(2025, 2, 5)
    private val recentDate = LocalDate.of(2025, 2, 5).minusMonths(2)

    private val platinum = DrugTreatment(
        name = "Carboplatin",
        drugs = setOf(Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)))
    )
    private val otherTreatment = DrugTreatment(name = "other treatment", drugs = setOf())

    @Test
    fun `Should return null if treatment history is empty`() {
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(emptyList()), referenceDate, 6)
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.firstPlatinumTreatment)).isNull()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isNull()
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.lastPlatinumTreatment)).isNull()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isNull()
        assertThat(base.firstPlatinumTreatment).isNull()
        assertThat(base.lastPlatinumTreatment).isNull()
    }

    @Test
    fun `Should return true if treatment history contains platinum with progression and next treatment started within 6 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue - 3
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(otherTreatment),
                startYear = recentDate.year,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum with stop reason progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains platinum with progression but next treatment started after 6 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(otherTreatment),
                startYear = recentDate.year + 1,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.firstPlatinumTreatment)).isFalse()
        assertThat(base.hasProgressionDuringPlatinumOrWithinMonths(base.lastPlatinumTreatment)).isFalse()
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
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum but without progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.TOXICITY,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue - 7
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(otherTreatment),
                startYear = recentDate.year,
                startMonth = recentDate.monthValue
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum with unknown stop reason`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = null
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum but unknown if next treatment started within 6 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
                stopYear = recentDate.year
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(otherTreatment),
                startYear = recentDate.year
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isTrue()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains platinum but without progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                bestResponse = TreatmentResponse.MIXED,
                stopReason = StopReason.TOXICITY
            )
        )
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.firstPlatinumTreatment)).isFalse()
        assertThat(base.hasProgressionOrUnknownProgressionOnPlatinum(base.lastPlatinumTreatment)).isFalse()
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
            stopYear = recentDate.year,
            stopMonth = recentDate.monthValue
        )
        val history = listOf(first, second)
        val base = PlatinumProgressionAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), referenceDate, 6)
        assertThat(base.firstPlatinumTreatment?.entry?.startYear).isEqualTo(first.startYear)
        assertThat(base.lastPlatinumTreatment?.entry?.startYear).isEqualTo(second.startYear)
    }
}