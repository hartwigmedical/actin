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

data class TreatmentRankResult(val treatment: String, val scores: List<EvidenceScore>) : Comparable<TreatmentRankResult> {

    val score = scores.sumOf { it.score }

    override fun compareTo(other: TreatmentRankResult): Int {
        return Comparator.comparingDouble<TreatmentRankResult> { it.score }.reversed().compare(this, other)
    }
}

data class TreatmentEvidenceWithTarget(val treatmentEvidence: TreatmentEvidence, val target: String, val isIndirect: Boolean)
data class DuplicateEvidenceGrouping(val treatment: String, val gene: String?, val tumorMatch: TumorMatch, val benefit: Boolean)

class TreatmentRankingModel(
    private val scoringModel: EvidenceScoringModel,
    private val includeIndirectTreatmentEvidence: Boolean = true
) {

    fun rank(record: PatientRecord): TreatmentEvidenceRanking {
        val rankingResults = computeRankResults(record)

        return TreatmentEvidenceRanking(rankingResults.map {
            RankedTreatment(
                it.treatment,
                it.scores.map { s -> s.event }.toSet(),
                it.scores.sumOf { s -> s.score }
            )
        })
    }

    fun computeRankResults(record: PatientRecord): List<TreatmentRankResult> {
        val actionables = record.molecularTests.asSequence().flatMap {
            it.drivers.fusions + it.drivers.variants + it.drivers.copyNumbers +
                    it.drivers.homozygousDisruptions + it.drivers.disruptions + it.drivers.viruses +
                    it.characteristics.microsatelliteStability + it.characteristics.homologousRecombination +
                    it.characteristics.tumorMutationalLoad + it.characteristics.tumorMutationalBurden
        }.filterNotNull()

        val treatmentEvidencesWithTarget = treatmentEvidencesWithTargets(actionables)
        val scoredTreatments = treatmentEvidencesWithTarget.map { evidenceWithTarget ->
            evidenceWithTarget to scoringModel.score(
                evidenceWithTarget.treatmentEvidence,
                isIndirect = evidenceWithTarget.isIndirect
            )
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
            val directEvidences = actionable.evidence.treatmentEvidence.asSequence().map { treatmentEvidence ->
                TreatmentEvidenceWithTarget(
                    treatmentEvidence,
                    resolveTarget(actionable, treatmentEvidence),
                    isIndirect = false
                )
            }
            val indirectEvidences = actionable.evidence.indirectTreatmentEvidence.asSequence().map { treatmentEvidence ->
                TreatmentEvidenceWithTarget(
                    treatmentEvidence,
                    resolveTarget(actionable, treatmentEvidence),
                    isIndirect = true
                )
            }
            directEvidences + if (includeIndirectTreatmentEvidence) indirectEvidences else emptySequence()
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
}

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
    val headerColumns = listOf("Treatment", "Variant", "Variant Match", "Tumor Match", "Approval", "Score", "Description")
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
            scoreSum.toString(),
            ""
        ).joinToString(delimiter)
        stringBuilder.append(summaryRow).append(newline)
        evidenceRows.forEach { stringBuilder.append(it).append(newline) }
    }

    Files.writeString(Path.of(outputTsvPath), stringBuilder.toString())
}
