package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.Translation
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.logging.log4j.Logger
import org.junit.Test

class CurationDatabaseTest {

    @Test
    fun `Should log warnings for unused curation inputs`() {
        val arTranslation = Translation("ar1", "t1")
        val labTranslation = LaboratoryTranslation("code1", "newCode", "name1", "newName")
        val database = TestCurationFactory.createMinimalTestCurationDatabase().copy(
            primaryTumorConfigs = mapOf("pt1" to setOf(mockk()), "pt2" to setOf(mockk())),
            administrationRouteTranslations = listOf(arTranslation, Translation("ar2", "t2")).associateBy(Translation::input),
            laboratoryTranslations = listOf(labTranslation, LaboratoryTranslation("code2", "another", "name2", "someName"))
                .associateBy { it.code to it.name }
        )
        val evaluatedInputs = ExtractionEvaluation(
            primaryTumorEvaluatedInputs = setOf("pt1", "pt2", "unknown"),
            administrationRouteEvaluatedInputs = setOf(arTranslation, Translation("ar3", "t3")),
            laboratoryEvaluatedInputs = setOf(labTranslation)
        )
        val logger = mockk<Logger>(relaxed = true)
        database.evaluate(evaluatedInputs, logger)

        verify {
            logger.warn(
                " Curation key '{}' not used for {} translation", "ar2", CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION.categoryName
            )
            logger.warn(
                " Curation key '{}|{}' not used for {} translation", "code2", "name2", CurationCategory.LABORATORY_TRANSLATION.categoryName
            )
        }
        confirmVerified(logger)
    }
}