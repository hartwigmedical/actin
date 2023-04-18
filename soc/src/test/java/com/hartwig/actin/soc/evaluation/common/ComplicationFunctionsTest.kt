package com.hartwig.actin.soc.evaluation.common

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.soc.evaluation.general.WHOFunctions.COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplicationFunctionsTest {
    @Test
    fun shouldReturnEmptyForCategorySearchWhenComplicationsAreNull() {
        val record: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        val filteredComplicationNames = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertTrue(filteredComplicationNames.isEmpty())
        val filteredComplicationCategories = ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertTrue(filteredComplicationCategories.isEmpty())
    }

    @Test
    fun shouldReturnListOfComplicationsMatchingCategorySearchTerms() {
        val complications = listOf(ImmutableComplication.builder()
                .name("first matching")
                .addCategories("X", "Y", "the ascites category", "Pleural Effusions")
                .build(),
                ImmutableComplication.builder().name("other").addCategories("X", "Y").build(),
                ImmutableComplication.builder().name("second matching").addCategories("chronic pain issues", "nothing").build())

        val base = TestDataFactory.createMinimalTestPatientRecord()

        val record: PatientRecord = ImmutablePatientRecord.copyOf(base)
                .withClinical(ImmutableClinicalRecord.copyOf(base.clinical())
                        .withComplications(complications)
                        .withClinicalStatus(ImmutableClinicalStatus.copyOf(base.clinical().clinicalStatus())
                                .withHasComplications(true)))

        val filteredComplicationNames = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertEquals(2, filteredComplicationNames.size.toLong())
        assertTrue(filteredComplicationNames.contains("first matching"))
        assertTrue(filteredComplicationNames.contains("second matching"))
        val filteredComplicationCategories = ComplicationFunctions.findComplicationCategoriesMatchingAnyCategory(record, COMPLICATION_CATEGORIES_AFFECTING_WHO_STATUS)
        assertEquals(3, filteredComplicationCategories.size.toLong())
        assertTrue(filteredComplicationCategories.contains("the ascites category"))
        assertTrue(filteredComplicationCategories.contains("Pleural Effusions"))
        assertTrue(filteredComplicationCategories.contains("chronic pain issues"))
    }
}