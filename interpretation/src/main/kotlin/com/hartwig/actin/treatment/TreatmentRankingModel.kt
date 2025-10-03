package com.hartwig.actin.treatment

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
import kotlin.math.exp

data class TreatmentRankResult(val treatment: String, val scores: List<EvidenceScore>) : Comparable<TreatmentRankResult> {

    val score = scores.sumOf { it.score }

    override fun compareTo(other: TreatmentRankResult): Int {
        return Comparator.comparingDouble<TreatmentRankResult> { it.score }.reversed().compare(this, other)
    }
}

data class TreatmentEvidenceWithTarget(val treatmentEvidence: TreatmentEvidence, val target: String)
data class DuplicateEvidenceGrouping(val treatment: String, val gene: String?, val tumorMatch: TumorMatch, val benefit: Boolean)

class TreatmentRankingModel(private val scoringModel: EvidenceScoringModel) {

    fun rank(record: PatientRecord): TreatmentEvidenceRanking {
        val actionables = record.molecularTests.asSequence().flatMap {
            it.drivers.fusions + it.drivers.variants + it.drivers.copyNumbers +
                    it.drivers.homozygousDisruptions + it.drivers.disruptions + it.drivers.viruses +
                    it.characteristics.microsatelliteStability + it.characteristics.homologousRecombination +
                    it.characteristics.tumorMutationalLoad + it.characteristics.tumorMutationalBurden
        }.filterNotNull()

        val treatmentEvidencesWithTarget = treatmentEvidencesWithTargets(actionables)
        val scoredTreatments = treatmentEvidencesWithTarget.map { it to scoringModel.score(it.treatmentEvidence) }
        val scoredTreatmentsWithDuplicatesDiminished = groupEvidenceForDuplicationAndDiminishScores(scoredTreatments)

        val rankingResults = scoredTreatmentsWithDuplicatesDiminished.map { it.key.treatment to it.value }.groupBy { it.first }
            .map { TreatmentRankResult(it.key, it.value.flatMap { t -> t.second }) }

        return TreatmentEvidenceRanking(rankingResults.sorted().map {
            RankedTreatment(it.treatment, it.scores.map { s -> s.event }.toSet(),
                it.scores.sumOf { s -> s.score })
        })
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
            (actionable.evidence.treatmentEvidence.asSequence() + actionable.evidence.indirectTreatmentEvidence.asSequence())
                .map { treatmentEvidence ->
                    TreatmentEvidenceWithTarget(
                        treatmentEvidence,
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
                    )
                }
        }

    private fun saturatingDiminishingReturnsScore(
        evidenceScores: List<EvidenceScore>, slope: Double = 1.5, midpoint: Double = 1.0
    ) = evidenceScores.sortedDescending().withIndex().map { (index, score) ->
        score to if (index >= 1) score.score * (1.0 / (1.0 + exp(slope * (index - midpoint)))) else score.score
    }.map { it.first.copy(score = it.second) }
}
