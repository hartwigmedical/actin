package com.hartwig.actin.treatment

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.exp

data class TreatmentRankResult(val treatment: String, val event: String, val scores: List<EvidenceScore>) :
    Comparable<TreatmentRankResult> {

    val score = scores.sumOf { it.score }

    override fun compareTo(other: TreatmentRankResult): Int {
        return Comparator.comparingDouble<TreatmentRankResult> { it.score }.reversed().compare(this, other)
    }
}

data class TreatmentEvidenceWithTarget(val treatmentEvidence: TreatmentEvidence, val target: String, val event: String)
data class DuplicateEvidenceGrouping(val treatment: String, val gene: String?, val tumorMatch: TumorMatch, val benefit: Boolean)

class TreatmentRankingModel(
    private val scoringModel: EvidenceScoringModel,
) {

    fun rank(record: PatientRecord): TreatmentEvidenceRanking {
        val rankingResults = computeRankResults(record)

        return TreatmentEvidenceRanking(
            rankingResults.map {
                RankedTreatment(
                    //it.scores.map { s -> s.event }.toSet(),
                    it.treatment,
                    it.event, // Extract only relevant variants,
                    it.scores.sumOf { s -> s.score }
                )
            })
    }

    private fun extractEventForTreatment(record: PatientRecord, treatment: String): String? {

        // Iterate through molecular tests in the patient record


        record.molecularTests.forEach { molecularTest ->
            // 1. Check and return the first matching patient variant
            molecularTest.drivers.variants.find { variant ->
                variant.evidence.treatmentEvidence.any { it.treatment == treatment }
            }?.let { variant ->
                return variant.event // Return the matching variant's event
            }

            // 2. Check and return the TMB value if it matches the treatment
            molecularTest.characteristics.tumorMutationalBurden?.let { tmb ->
                if (tmb.evidence.treatmentEvidence.any { it.treatment == treatment }) {
                    return "TMB ${tmb.score}" // Return the TMB value
                }
            }

            // 3. Check and return the MSI status if it matches the treatment
            molecularTest.characteristics.microsatelliteStability?.let { msi ->
                if (msi.evidence.treatmentEvidence.any { it.treatment == treatment }) {
                    return if (msi.isUnstable) "MSI Unstable" else "MSI Stable" // Return the MSI string
                }
            }
        }

        // Return null if no match is found
        return null
    }

    /*
    // Add a helper function to extract the patient-specific variant
    private fun extractEventForTreatment(record: PatientRecord, treatment: String): Set<String> {
        // Logic to fetch and return the patient's variant/mutation
        // For example: Use molecularTests to locate the specific variant along with the treatment
        val variants = record.molecularTests.flatMap { molecularTest ->
            molecularTest.drivers.variants.filter { variant ->
                variant.evidence.treatmentEvidence.any { it.treatment == treatment }
            }.map { it.event }
        }
        // Add molecular characteristics (MSI, TMB, TML)
        val molecularCharacteristics = record.molecularTests.flatMap { molecularTest ->
            listOfNotNull(
                // Match MSI if evidence links it to this treatment
                molecularTest.characteristics.microsatelliteStability?.let { msi ->
                    if (msi.evidence.treatmentEvidence.any { it.treatment == treatment }) {
                        if (msi.isUnstable == true) "MSI High" else "MSI Low/Neg"
                    } else null
                },
                // Match TMB if evidence links it to this treatment
                molecularTest.characteristics.tumorMutationalBurden?.let { tmb ->
                    if (tmb.evidence.treatmentEvidence.any { it.treatment == treatment }) {
                        if (tmb.isHigh == true) "TMB High" else "TMB Low"
                    } else null
                },
                // Match TML if evidence links it to this treatment
                molecularTest.characteristics.tumorMutationalLoad?.let { tml ->
                    if (tml.evidence.treatmentEvidence.any { it.treatment == treatment }) {
                        if (tml.isHigh == true) "TML High" else "TML Low"
                    } else null
                }
            )
        }

        // Combine variants and molecular characteristics, ensuring no duplicates
        return (variants + molecularCharacteristics).toSet()
    }
*/
    private fun computeRankResults(record: PatientRecord): List<TreatmentRankResult> {
        val actionables = record.molecularTests.asSequence().flatMap {
            it.drivers.fusions + it.drivers.variants + it.drivers.copyNumbers +
                    it.drivers.homozygousDisruptions + it.drivers.disruptions + it.drivers.viruses +
                    it.characteristics.microsatelliteStability + it.characteristics.homologousRecombination +
                    it.characteristics.tumorMutationalLoad + it.characteristics.tumorMutationalBurden
        }.filterNotNull()

        val treatmentEvidencesWithTarget = treatmentEvidencesWithTargets(actionables)
        val scoredTreatments = treatmentEvidencesWithTarget.map { evidenceWithTarget ->
            evidenceWithTarget to scoringModel.score(evidenceWithTarget.treatmentEvidence)
        }
        val scoredTreatmentsWithDuplicatesDiminished = groupEvidenceForDuplicationAndDiminishScores(scoredTreatments)

        return scoredTreatmentsWithDuplicatesDiminished
            .map { it.key.treatment to it.value }
            .groupBy { it.first }
            .map { TreatmentRankResult(it.key, it.value.flatMap { t -> t.second }) }
            .sorted()
    }

    private fun groupEvidenceForDuplicationAndDiminishScores(scoredTreatmentEntries: Sequence<Pair<TreatmentEvidenceWithTarget, EvidenceScore>>) =
        scoredTreatmentEntries.groupBy {
            DuplicateEvidenceGrouping(
                it.first.treatmentEvidence.treatment,
                it.first.target,
                it.second.scoringMatch.tumorMatch,
                (it.second.score > 0)
            )
        }.mapValues { entry ->
            saturatingDiminishingReturnsScore(entry.value.map { it.second })
        }

    private fun treatmentEvidencesWithTargets(actionables: Sequence<Actionable>) =
        actionables.flatMap { actionable ->
            actionable.evidence.treatmentEvidence.asSequence().map { treatmentEvidence ->
                TreatmentEvidenceWithTarget(
                    treatmentEvidence,
                    resolveTarget(actionable, treatmentEvidence),
                    actionable.event
                )
            }
        }

    private fun resolveTarget(actionable: Actionable, treatmentEvidence: TreatmentEvidence): String =
        when (actionable) {
            is GeneAlteration -> actionable.gene
            is MicrosatelliteStability -> "MSI"
            is TumorMutationalLoad -> "TML"
            is TumorMutationalBurden -> "TMB"
            is Fusion -> "${actionable.geneStart}::${actionable.geneEnd}"
            is Virus -> actionable.name
            is HomologousRecombination -> actionable.type.toString()
            else -> treatmentEvidence.molecularMatch.sourceEvent
        }

    private fun saturatingDiminishingReturnsScore(
        evidenceScores: List<EvidenceScore>, slope: Double = 1.5, midpoint: Double = 1.0
    ) = evidenceScores.sortedDescending().withIndex().map { (index, score) ->
        score to if (index >= 1) score.score * (1.0 / (1.0 + exp(slope * (index - midpoint)))) else score.score
    }.map { it.first.copy(score = it.second) }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                println("Usage: <patient_json_path> <output_tsv_path>")
                return
            }
            val patientRecordPath = args[0]
            val outputTsvPath = args[1]
            val patientRecord = PatientRecordJson.fromJson(Files.readString(Path.of(patientRecordPath)))
            val records = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig())).computeRankResults(patientRecord)

            val delimiter = "\t"
            val newline = "\n"
            val headerColumns = listOf(
                "Treatment",
                "Variant",
                "Variant Match",
                "Tumor Match",
                "Approval",
                "Evidence Level",
                "Cancer Type",
                "Score",
                "Description"
            )
            val stringBuilder = StringBuilder()

            stringBuilder.append(headerColumns.joinToString(delimiter)).append(newline)

            for (record in records) {
                val evidenceRows = mutableListOf<String>()
                var scoreSum = 0.0

                for (evidenceScore in record.scores.sortedBy { it.score }.reversed()) {
                    with(evidenceScore) {
                        val row = listOf(
                            "",
                            event,
                            scoringMatch.variantMatch.toString(),
                            scoringMatch.tumorMatch.toString(),
                            evidenceLevelDetails.toString(),
                            evidenceLevel.name,
                            cancerType.matchedCancerType,
                            score.toString(),
                            evidenceDescription
                        ).joinToString(delimiter)
                        evidenceRows += row
                    }
                    scoreSum += evidenceScore.score
                }

                val summaryRow = listOf(
                    record.treatment,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    scoreSum.toString(),
                    ""
                ).joinToString(delimiter)
                stringBuilder.append(summaryRow).append(newline)
                evidenceRows.forEach { stringBuilder.append(it).append(newline) }
            }

            Files.writeString(Path.of(outputTsvPath), stringBuilder.toString())
        }
    }
}