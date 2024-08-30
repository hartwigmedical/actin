package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.extractDifferences
import com.hartwig.actin.algo.matchcomparison.MatchOutputComparisonApplication.Companion.LOGGER
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class MatchOutputComparisonApplication {

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MatchOutputComparisonApplication::class.java)

        fun run(oldFile: String, newFile: String): Int {
            LOGGER.info("Running ACTIN Test Match Output Comparison Application with $oldFile and $newFile")

            val oldMatches = TreatmentMatchJson.read(oldFile)
            val newMatches = TreatmentMatchJson.read(newFile)

            extractDifferences(oldMatches, newMatches, mapOf(
                "patientId" to TreatmentMatch::patientId,
                "sampleId" to TreatmentMatch::sampleId,
                "referenceDate" to TreatmentMatch::referenceDate
            )).forEach(LOGGER::info)

            val matchDifferences = TreatmentMatchComparison.determineTreatmentMatchDifferences(oldMatches, newMatches)
            matchDifferences.uniqueDifferences().forEach(LOGGER::info)

            return 0
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        LOGGER.error("Please provide 2 treatment match json files as arguments")
        exitProcess(1)
    }
    exitProcess(MatchOutputComparisonApplication.run(args[0], args[1]))
}
