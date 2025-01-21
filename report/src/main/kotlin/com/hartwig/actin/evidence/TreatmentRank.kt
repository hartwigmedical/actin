package com.hartwig.actin.evidence

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path

data class RankResult(val treatment: String, val scores: List<Score>) : Comparable<RankResult> {
    override fun compareTo(other: RankResult): Int {
        return Comparator.comparingDouble<RankResult> { it.scores.sumOf { s -> s.score }.toDouble() }.compare(this, other)
    }

}

class TreatmentRank {

    fun rank(record: PatientRecord): List<RankResult> {
        val scorer = TreatmentScorer()
        val treatments = record.molecularHistory.molecularTests.asSequence().flatMap {
            (it.drivers.fusions + it.drivers.variants).map { d -> d.evidence } +
                    it.characteristics.microsatelliteEvidence + it.characteristics.homologousRepairEvidence + it.characteristics.tumorMutationalBurdenEvidence + it.characteristics.tumorMutationalLoadEvidence
        }.filterNotNull().flatMap { it.treatmentEvidence }.toSet()

        val scoredTreatmentEntries = treatments.map { it to scorer.score(it) }
        val scoredTreatments =
            scoredTreatmentEntries.groupBy {
                it.first.treatment to it.first.molecularMatch.sourceEvent
            }.mapValues { entry ->
                entry.value.map { it.second }.toList()
            }
        return scoredTreatments.map { RankResult(it.key.first, it.value) }
    }
}

fun main() {
    val patientRecord = PatientRecordJson.fromJson(Files.readString(Path.of("/Users/pwolfe/Code/actin/case_976.patient_record.json")))
    val records = TreatmentRank().rank(patientRecord)
    var stringToWrite = "Treatment,Variant,Variant Match,Tumor Match,Approval,Factor,Score,Total Score,Description\n"
    for (record in records.sorted().reversed()) {
        var treatmentText = ""
        var scoreSum = 0
        for (scoreObject in record.scores.sortedBy { it.score() }.reversed()) {
            with(scoreObject) {
                val eventHeader =
                    ",$variant,${scoringMatch.variantMatch},${scoringMatch.tumorMatch},${evidenceLevelDetails},$factor,$score,${score()},${evidenceDescription.replace(",","")}\n"
                treatmentText += eventHeader
            }
            scoreSum += scoreObject.score()
        }
        stringToWrite += "${record.treatment},,,,,,,$scoreSum\n"
        stringToWrite += treatmentText
    }
    Files.writeString(Path.of("case976_ranking.csv"), stringToWrite)

}