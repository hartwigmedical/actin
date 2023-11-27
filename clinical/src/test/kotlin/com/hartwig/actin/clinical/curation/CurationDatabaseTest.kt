package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.logging.log4j.Logger
import org.junit.Test

class CurationDatabaseTest {

    @Test
    fun `Should log warnings for unused curation inputs`() {
        val database = TestCurationFactory.createMinimalTestCurationDatabase().copy(
            primaryTumorConfigs = mapOf("pt1" to setOf(mockk()), "pt2" to setOf(mockk())),
            administrationRouteTranslations = mapOf("ar1" to mockk(), "ar2" to mockk()),
            laboratoryTranslations = mapOf(Pair("code1", "name1") to mockk(), Pair("code2", "name2") to mockk())
        )
        val evaluatedInputs = ExtractionEvaluation(
            primaryTumorEvaluatedInputs = setOf("pt1", "pt2", "unknown"),
            administrationRouteEvaluatedInputs = setOf("ar1", "another"),
            laboratoryEvaluatedInputs = setOf("code1|name1")
        )
        val logger = mockk<Logger>(relaxed = true)
        database.evaluate(evaluatedInputs, logger)

        listOf(
            "ar2" to CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION.categoryName,
            "code2|name2" to CurationCategory.LABORATORY_TRANSLATION.categoryName
        )
            .forEach { (input, category) ->
                verify { logger.warn(" Curation key '{}' not used for {} curation", input, category) }
            }
        confirmVerified(logger)
    }
}