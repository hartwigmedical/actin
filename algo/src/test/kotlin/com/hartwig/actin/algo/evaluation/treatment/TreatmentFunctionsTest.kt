package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.TreatmentFunctions.receivedPlatinumDoublet
import com.hartwig.actin.algo.evaluation.treatment.TreatmentFunctions.receivedPlatinumTripletOrAbove
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentFunctionsTest {

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

    @Test
    fun `Should return false if treatment history is empty`() {
        assertThat(receivedPlatinumDoublet(TreatmentTestFactory.withTreatmentHistory(emptyList()))).isFalse()
        assertThat(receivedPlatinumTripletOrAbove(TreatmentTestFactory.withTreatmentHistory(emptyList()))).isFalse()
    }

    // Tests for fun receivedPlatinumDoublet
    @Test
    fun `Should return true if treatment history contains platinum doublet therapy`(){
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet)))
        )
        assertThat(receivedPlatinumDoublet(record)).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum doublet therapy`(){
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(receivedPlatinumDoublet(record)).isFalse()
    }

    // Tests for fun receivedPlatinumTripletOrAbove
    @Test
    fun `Should return true if treatment history contains platinum triplet therapy`(){
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumTriplet)))
        )
        assertThat(receivedPlatinumTripletOrAbove(record)).isTrue()
    }

    @Test
    fun `Should return true if treatment history contains platinum compound therapy of more than 3 drugs`(){
        val drugs =
            platinumTriplet.copy(
                drugs = platinumTriplet.drugs
                    .plus(Drug(name = "Doxorubicin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTHRACYCLINE)))
            )

        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(drugs)))
        )
        assertThat(receivedPlatinumTripletOrAbove(record)).isTrue()
    }

    @Test
    fun `Should return false if treatment history not empty but does not contain platinum triplet therapy`(){
        val record = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumSinglet)),
                TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(nonPlatinumDoublet))
            )
        )
        assertThat(receivedPlatinumTripletOrAbove(record)).isFalse()
    }
}