package com.hartwig.actin.treatment

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import kotlin.math.exp

data class TreatmentRankResult(val treatment: String, val scores: List<EvidenceScore>) : Comparable<TreatmentRankResult> {

    val score = scores.sumOf { it.score }

    override fun compareTo(other: TreatmentRankResult): Int {
        return Comparator.comparingDouble<TreatmentRankResult> { it.score }.reversed().compare(this, other)
    }
}

class TreatmentRankingModel(private val scoringModel: EvidenceScoringModel) {

    fun rank(record: PatientRecord): TreatmentEvidenceRanking {
        val actionables = record.molecularHistory.molecularTests.asSequence().flatMap {
            (it.drivers.fusions + it.drivers.variants + it.drivers.copyNumbers + it.drivers.homozygousDisruptions + it.drivers.disruptions + it.drivers.viruses) +
                    it.characteristics.microsatelliteStability + it.characteristics.homologousRecombination + it.characteristics.tumorMutationalLoad + it.characteristics.tumorMutationalBurden
        }.filterNotNull()

        data class TreatmentAndMaybeGene(val treatmentEvidence: TreatmentEvidence, val gene: String?)

        val treatmentsAndMaybeGenes =
            actionables.flatMap {
                it.evidence.treatmentEvidence.map { t ->
                    TreatmentAndMaybeGene(
                        t,
                        (if (it is GeneAlteration) it.gene else null)
                    )
                }
            }

        val scoredTreatmentEntries = treatmentsAndMaybeGenes.map { it to scoringModel.score(it.treatmentEvidence, it.gene) }
        val scoredTreatments = scoredTreatmentEntries.groupBy {
            it.first.treatmentEvidence.treatment
        }.mapValues { entry ->
            entry.value.map { it.second }
        }
        val rankingResults = scoredTreatments.map { TreatmentRankResult(it.key, it.value) }.map {
            val diminishedScores = it.scores.groupBy(this::geneTumorMatchAndBenefitVsResistance)
                .mapValues { v ->
                    saturatingDiminishingReturnsScore(v.value)
                }.flatMap { g ->
                    g.value
                }
            it.copy(scores = diminishedScores)
        }
        return TreatmentEvidenceRanking(rankingResults.sorted().map {
            RankedTreatment(it.treatment, it.scores.map { s -> s.event }.toSet(),
                it.scores.sumOf { s -> s.score })
        })
    }

    private fun geneTumorMatchAndBenefitVsResistance(s: EvidenceScore) = Triple(s.gene, s.scoringMatch.tumorMatch, (s.score < 0))
}

fun saturatingDiminishingReturnsScore(
    evidenceScores: List<EvidenceScore>, slope: Double = 1.5, midpoint: Double = 1.0
) = evidenceScores.sortedDescending().withIndex().map { (index, score) ->
    score to if (index >= 1) score.score * (1.0 / (1.0 + exp(slope * (index - midpoint)))) else score.score
}.map { it.first.copy(score = it.second) }