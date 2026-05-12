package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.extractDifferences
import com.hartwig.actin.algo.matchcomparison.MatchOutputComparisonApplication.Companion.logger
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class MatchOutputComparisonApplication {

    companion object {
        val logger = KotlinLogging.logger {}

        fun run(oldFile: String, newFile: String): Int {
            logger.info { "Running ACTIN Test Match Output Comparison Application with $oldFile and $newFile" }

            val oldMatches = TreatmentMatchJson.read(oldFile)
            val newMatches = TreatmentMatchJson.read(newFile)

            extractDifferences(oldMatches, newMatches, mapOf(
                "patientId" to TreatmentMatch::patientId,
                "referenceDate" to TreatmentMatch::referenceDate
            )).forEach { logger.info { it } }

            val matchDifferences = TreatmentMatchComparison.determineTreatmentMatchDifferences(oldMatches, newMatches)
            matchDifferences.uniqueDifferences().forEach { logger.info { it } }

            return 0
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        logger.error { "Please provide 2 treatment match json files as arguments" }
        exitProcess(1)
    }
    exitProcess(MatchOutputComparisonApplication.run(args[0], args[1]))
}
