package com.hartwig.actin.evidence

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.PatientRecord
import java.nio.file.Files
import java.nio.file.Path

class TreatmentRank {

    fun rank(record: PatientRecord) {
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
                entry.value.map { it.second }.sumOf {
                    it.score()
                }
            }
                .map { it }.sortedBy { it.value }.reversed()
        scoredTreatments.map { it }.forEach {
            println("Score [${it.value}] for treatment [${it.key}]")
        }
    }
}

fun main() {
    val patientRecord = PatientRecordJson.fromJson(Files.readString(Path.of("/Users/pwolfe/Code/actin/case_976.patient_record.json")))
    TreatmentRank().rank(patientRecord)
}