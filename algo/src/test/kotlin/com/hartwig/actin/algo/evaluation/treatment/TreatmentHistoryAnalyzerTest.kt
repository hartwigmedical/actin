package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentHistoryAnalyzerTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild(
        DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID,
        DoidConstants.LUNG_ADENOCARCINOMA_DOID
    )
    private val platinumDoublet =
        DrugTreatment(
            name = "Carboplatin+Pemetrexed",
            drugs = setOf(
                Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
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

    @Test
    fun `Should return false if treatment history is empty`() {
        val base = TreatmentHistoryAnalyzer(TreatmentTestFactory.withTreatmentHistory(emptyList()), doidModel)
        assertThat(base.receivedPlatinumDoublet()).isFalse()
        assertThat(base.receivedPlatinumTripletOrAbove()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum doublet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet)))
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedPlatinumDoublet()).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum doublet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedPlatinumDoublet()).isFalse()
    }

    @Test
    fun `Should return true if treatment history contains platinum triplet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumTriplet)))
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedPlatinumTripletOrAbove()).isTrue()
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
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedPlatinumTripletOrAbove()).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum triplet therapy`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedPlatinumTripletOrAbove()).isFalse()
    }

    @Test
    fun `Should return true if tumor type is NSCLC`() {
        val record = TumorTestFactory.withDoids(setOf(DoidConstants.LUNG_ADENOCARCINOMA_DOID))
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).isNsclc).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains undefined chemoradiation`() {
        val undefinedChemo = TreatmentTestFactory.drugTreatment("CHEMOTHERAPY", TreatmentCategory.CHEMOTHERAPY)
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(undefinedChemo, radiotherapy)))
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedUndefinedChemoradiation()).isTrue()
    }

    @Test
    fun `Should return false if treatment history contains chemoradiation but with chemotherapy type defined`() {
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet, radiotherapy)))
        )
        assertThat(TreatmentHistoryAnalyzer(record, doidModel).receivedUndefinedChemoradiation()).isFalse()
    }
}