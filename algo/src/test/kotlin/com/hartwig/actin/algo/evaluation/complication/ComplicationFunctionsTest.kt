package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.evaluation.complication.ComplicationTestFactory.complication
import com.hartwig.actin.algo.evaluation.general.WHOFunctions.COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComplicationFunctionsTest {

    @Test
    fun `Should return empty for category search when complications are null`() {
        val record: PatientRecord = TestPatientFactory.createMinimalTestPatientRecord()
        val filteredComplicationNames =
            ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertThat(filteredComplicationNames).isEmpty()
        val filteredComplicationCategories =
            ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertThat(filteredComplicationCategories).isEmpty()
    }

    @Test
    fun `Should return list of complications matching category search terms`() {
        val complications = listOf(
            complication(name = "first matching", categories = setOf("X", "Y", "the ascites category", "Pleural Effusions")),
            complication(name = "other", categories = setOf("X", "Y")),
            complication(name = "second matching", categories = setOf("chronic pain issues", "nothing"))
        )

        val base = TestPatientFactory.createMinimalTestPatientRecord()

        val record: PatientRecord = base.copy(
            complications = complications, clinicalStatus = base.clinicalStatus.copy(hasComplications = true)
        )

        val filteredComplicationNames =
            ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertThat(filteredComplicationNames).hasSize(2)
        assertThat(filteredComplicationNames).contains("first matching")
        assertThat(filteredComplicationNames).contains("second matching")
        
        val filteredComplicationCategories =
            ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertThat(filteredComplicationCategories).hasSize(3)
        assertThat(filteredComplicationCategories).contains("the ascites category")
        assertThat(filteredComplicationCategories).contains("Pleural Effusions")
        assertThat(filteredComplicationCategories).contains("chronic pain issues")
    }
}