package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorTypeEvaluationFunctionsTest {
    @Test
    fun canDetermineIfTumorHasType() {
        val validTypes = setOf("Valid")
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(), validTypes)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorType = "wrong"), validTypes)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorType = "valid type"), validTypes)).isTrue
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithType(TumorDetails(primaryTumorSubType = "valid sub-type"), validTypes)).isTrue
    }

    @Test
    fun canDetermineIfTumorHasDetails() {
        val validDetails = setOf("Valid")
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(), validDetails)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(primaryTumorExtraDetails = "wrong"), validDetails)).isFalse
        assertThat(TumorTypeEvaluationFunctions.hasTumorWithDetails(TumorDetails(primaryTumorExtraDetails = "valid details"), validDetails))
            .isTrue
    }

    @Test
    fun `Should return false if patient does not have peritoneal metastases`() {
        listOf("retroperitoneal lesions", "metastases in subperitoneal region", "Lymph node").forEach { lesion ->
            val tumor = TumorTestFactory.withOtherLesions(listOf(lesion)).tumor
            assertThat(TumorTypeEvaluationFunctions.hasPeritonealMetastases(tumor)).isFalse
        }
    }

    @Test
    fun `Should return true if patient does have peritoneal metastases`() {
        listOf(
            "Abdominal lesion located in Peritoneum", "Multiple depositions abdominal and peritoneal", "intraperitoneal"
        ).forEach { lesion ->
            val tumor = TumorTestFactory.withOtherLesions(listOf(lesion)).tumor
            assertThat(TumorTypeEvaluationFunctions.hasPeritonealMetastases(tumor)).isTrue
        }
    }

    @Test
    fun `Should return null if patient tumor details are unknown`() {
        assertThat(TumorTypeEvaluationFunctions.hasPeritonealMetastases(TumorDetails())).isNull()
    }
}