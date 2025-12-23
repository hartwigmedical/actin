package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryAnalysisTest {

    private val platinumDrug =
        Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND))

    private val platinumDoublet =
        DrugTreatment(
            name = "Carboplatin+Pemetrexed",
            drugs = setOf(
                platinumDrug,
                Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
            )
        )

    private val platinumTriplet =
        platinumDoublet.copy(
            name = platinumDoublet.name.plus("+Paclitaxel"),
            drugs = platinumDoublet.drugs
                .plus(Drug(name = "Paclitaxel", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.TAXANE)))
        )

    private val platinumSinglet =
        TreatmentTestFactory.drugTreatment("Carboplatin", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))

    private val nonPlatinumDoublet =
        DrugTreatment(
            name = "Doxorubicin+Pemetrexed",
            drugs = setOf(
                Drug(name = "Doxorubicin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTHRACYCLINE)),
                Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
            )
        )

    private val radiotherapy = TreatmentTestFactory.treatment("RADIOTHERAPY", false)
    private val immunotherapy = TreatmentTestFactory.treatment("IMMUNOTHERAPY", true)
    private val undefinedChemo = TreatmentTestFactory.drugTreatment("CHEMOTHERAPY", TreatmentCategory.CHEMOTHERAPY)

    @Test
    fun `Should return false if treatment history only contains curative, neoadjuvant and adjuvant entries and flag to ignore is set to true`() {
        val history = listOf(Intent.CURATIVE, Intent.ADJUVANT, Intent.NEOADJUVANT).map { intent ->
            TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet), intents = setOf(intent))
        }
        val analysis =
            TreatmentHistoryAnalysis.create(TreatmentTestFactory.withTreatmentHistory(history), ignoreCurativeNeoAdjuvantOrAdjuvant = true)
        assertThat(analysis.receivedPlatinumDoublet()).isFalse()
        assertThat(analysis.receivedPlatinumTripletOrAbove()).isFalse()
    }

    @Test
    fun `Should return false if treatment history is empty`() {
        val base = TreatmentHistoryAnalysis.create(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        assertThat(base.receivedPlatinumDoublet()).isFalse()
        assertThat(base.receivedPlatinumTripletOrAbove()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum doublet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedPlatinumDoublet()).isTrue()
    }

    @Test
    fun `Should include maintenance therapy in platinum doublet count`() {
        val record = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(nonPlatinumDoublet),
            maintenanceTreatment = TreatmentStage(platinumDoublet, null, null, null)
        )
        assertThat(
            TreatmentHistoryAnalysis.create(TreatmentTestFactory.withTreatmentHistoryEntry(record))
                .receivedPlatinumDoublet()
        ).isTrue()
    }

    @Test
    fun `Should include switch to treatments in platinum doublet count`() {
        val record = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(nonPlatinumDoublet),
            switchToTreatments = listOf(TreatmentStage(platinumDoublet, null, null, null))
        )
        assertThat(
            TreatmentHistoryAnalysis.create(TreatmentTestFactory.withTreatmentHistoryEntry(record))
                .receivedPlatinumDoublet()
        ).isTrue()
    }

    @Test
    fun `Should consider only one switch to treatment stage in platinum combination count`() {
        val treatmentStage = TreatmentStage(platinumDoublet, null, null, null)
        val record = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(nonPlatinumDoublet),
            switchToTreatments = listOf(
                treatmentStage,
                treatmentStage,
                treatmentStage.copy(treatment = nonPlatinumDoublet)
            )
        )
        assertThat(
            TreatmentHistoryAnalysis.create(TreatmentTestFactory.withTreatmentHistoryEntry(record))
                .receivedPlatinumDoublet()
        ).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum doublet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedPlatinumDoublet()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum triplet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumTriplet)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedPlatinumTripletOrAbove()).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum compound therapy of more than 3 drugs`() {
        val drugs =
            platinumTriplet.copy(
                drugs = platinumTriplet.drugs
                    .plus(Drug(name = "Doxorubicin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTHRACYCLINE)))
            )

        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(drugs)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedPlatinumTripletOrAbove()).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum triplet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedPlatinumTripletOrAbove()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains undefined chemoradiation`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(undefinedChemo, radiotherapy)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemoradiation()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains chemoradiation but with chemotherapy type defined`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet, radiotherapy)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemoradiation()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains undefined chemo-immunotherapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        TreatmentTestFactory.drugTreatment(
                            "CHEMOTHERAPY+IMMUNOTHERAPY",
                            TreatmentCategory.CHEMOTHERAPY,
                            emptySet()
                        )
                    )
                )
            )
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemoImmunotherapy()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains chemo-immunotherapy but with chemotherapy type defined`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet, immunotherapy)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemoImmunotherapy()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains undefined chemotherapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(undefinedChemo, platinumDoublet)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemotherapy()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains chemotherapy but with chemotherapy type defined`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet)))
        )
        assertThat(TreatmentHistoryAnalysis.create(record).receivedUndefinedChemotherapy()).isFalse()
    }
}