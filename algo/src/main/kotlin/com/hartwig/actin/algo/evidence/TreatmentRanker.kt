package com.hartwig.actin.algo.evidence

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.exp

data class RankResult(val treatment: String, val scores: List<Score>) : Comparable<RankResult> {

    val score = scores.sumOf { it.score }

    override fun compareTo(other: RankResult): Int {
        return Comparator.comparingDouble<RankResult> { it.score }.reversed().compare(this, other)
    }
}

class TreatmentRanker {

    fun rank(record: PatientRecord): List<RankResult> {
        val scorer = TreatmentScorer()
        val treatments = record.molecularHistory.molecularTests.asSequence().flatMap {
            (it.drivers.fusions + it.drivers.variants + it.drivers.copyNumbers + it.drivers.homozygousDisruptions + it.drivers.disruptions + it.drivers.viruses).map { d -> d.evidence } +
                    it.characteristics.microsatelliteStability?.evidence + it.characteristics.homologousRecombination?.evidence + it.characteristics.tumorMutationalLoad?.evidence + it.characteristics.tumorMutationalBurden?.evidence
        }.filterNotNull().flatMap { it.treatmentEvidence }.toSet()

        val scoredTreatmentEntries = treatments.map { it to scorer.score(it) }
        val scoredTreatments = scoredTreatmentEntries.groupBy {
            it.first.treatment
        }.mapValues { entry ->
            entry.value.map { it.second }
        }
        val originalScores = scoredTreatments.map { RankResult(it.key, it.value) }
        return originalScores.map {

            val diminishedScores = it.scores.groupBy { s -> Triple(parseGene(s.event), s.scoringMatch.tumorMatch, (s.score < 0)) }
                .mapValues { v ->
                    saturatingDiminishingReturnsScore(if (v.value.any { s -> s.scoringMatch.variantMatch == VariantMatch.EXACT }) {
                        v.value.map { s -> if (s.scoringMatch.variantMatch != VariantMatch.EXACT) s.copy(score = 0.0) else s }
                    } else {
                        v.value
                    })
                }.flatMap { g ->
                    g.value
                }
            it.copy(scores = diminishedScores)
        }
    }

    private fun parseGene(event: String): String {
        return event.split(" ")[0]
    }
}

fun saturatingDiminishingReturnsScore(
    evidenceScores: List<Score>, slope: Double = 1.5, midpoint: Double = 1.0
) = evidenceScores.sortedDescending().withIndex().map { (index, score) ->
    score to if (index >= 1) score.score * (1.0 / (1.0 + exp(slope * (index - midpoint)))) else score.score
}.map { it.first.copy(score = it.second) }

fun main() {

    for (s in listOf("957")) {
        try {
            val case = "case_$s"
            val patientRecordPath = System.getProperty("user.home") + "/Code/actin/$case.patient_record.json"
            val patientRecord = PatientRecordJson.fromJson(Files.readString(Path.of(patientRecordPath)))
            val records = TreatmentRanker().rank(patientRecord)


            var stringToWrite = "Treatment,Variant,Variant Match,Tumor Match,Approval,Score,Description\n"

            for (record in records.sorted()) {
                var treatmentText = ""
                var scoreSum = 0.0
                for (scoreObject in record.scores.sortedBy { it.score }.reversed()) {
                    with(scoreObject) {
                        val eventHeader = ",$event,${scoringMatch.variantMatch},${scoringMatch.tumorMatch},${evidenceLevelDetails},$score,${
                            evidenceDescription.replace(
                                ",", ""
                            )
                        }\n"
                        treatmentText += eventHeader
                    }
                    scoreSum += scoreObject.score
                }
                stringToWrite += "${record.treatment},,,,,,,$scoreSum\n"
                stringToWrite += treatmentText
            }
            Files.writeString(Path.of("${case}_ranking.csv"), stringToWrite)
        } catch (e: Exception) {
            println("Patient record $s not found or invalid. Skipping...")
            e.printStackTrace()
        }
    }


}