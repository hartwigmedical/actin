package com.hartwig.actin.algo.evidence

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.exp

data class RankResult(val treatment: String, val event: String, val scores: List<Score>) : Comparable<RankResult> {

    val score = scores.sumOf { it.score() }

    override fun compareTo(other: RankResult): Int {
        return Comparator.comparingDouble<RankResult> { it.score }.reversed().compare(this, other)
    }
}

class TreatmentRanker {

    fun rank(record: PatientRecord): List<RankResult> {
        val scorer = TreatmentScorer()
        val treatments = record.molecularHistory.molecularTests.asSequence().flatMap {
            (it.drivers.fusions + it.drivers.variants).map { d -> d.evidence } +
                    it.characteristics.microsatelliteEvidence +
                    it.characteristics.homologousRecombinationEvidence +
                    it.characteristics.tumorMutationalBurdenEvidence +
                    it.characteristics.tumorMutationalLoadEvidence
        }.filterNotNull().flatMap { it.treatmentEvidence }.toSet()

        val scoredTreatmentEntries = treatments.map { it to scorer.score(it) }
        val scoredTreatments =
            scoredTreatmentEntries.groupBy {
                it.first.treatment to it.first.molecularMatch.sourceEvent
            }.mapValues { entry ->
                entry.value.map { it.second }.toList()
            }
        val originalScores = scoredTreatments.map { RankResult(it.key.first, it.key.second, it.value) }
        return originalScores.map { it.copy(scores = saturatingDiminishingReturnsScore(it.scores)) }
    }
}

fun saturatingDiminishingReturnsScore(
    evidenceScores: List<Score>,
    slope: Double = 1.5,
    midpoint: Double = 1.0
) = evidenceScores.sortedDescending().withIndex().map { (index, score) ->
    score to if (index >= 1) score.score() * (1.0 / (1.0 + exp(slope * (index - midpoint)))) else score.score()
}.map { it.first.copy(score = it.second, factor = 1) }

fun main() {

    for (s in listOf("5", "6", "957", "958", "974", "976")) {
        val case = "case_$s"
        val patientRecordPath = System.getProperty("user.home") + "/Code/actin/$case.patient_record.json"
        val patientRecord = PatientRecordJson.fromJson(Files.readString(Path.of(patientRecordPath)))
        val records = TreatmentRanker().rank(patientRecord)


        var stringToWrite = "Treatment,Variant,Variant Match,Tumor Match,Approval,Factor,Score,Total Score,Description\n"

        for (record in records.sorted()) {
            var treatmentText = ""
            var scoreSum = 0.0
            for (scoreObject in record.scores.sortedBy { it.score() }.reversed()) {
                with(scoreObject) {
                    val eventHeader =
                        ",$variant,${scoringMatch.variantMatch},${scoringMatch.tumorMatch},${evidenceLevelDetails},$factor,$score,${score()},${
                            evidenceDescription.replace(
                                ",",
                                ""
                            )
                        }\n"
                    treatmentText += eventHeader
                }
                scoreSum += scoreObject.score()
            }
            stringToWrite += "${record.treatment},,,,,,,$scoreSum\n"
            stringToWrite += treatmentText
        }
        Files.writeString(Path.of("${case}_ranking.csv"), stringToWrite)
    }


}