package com.hartwig.actin.evidence

import com.hartwig.actin.datamodel.PatientRecord

class TreatmentRank {

    fun rank(record: PatientRecord) {
        val scorer = TreatmentScorer()
        val treatments =
            record.molecularHistory.molecularTests.flatMap {
                (it.drivers.fusions + it.drivers.variants).map { d -> d.evidence } +
                        it.characteristics.microsatelliteEvidence + it.characteristics.homologousRepairEvidence + it.characteristics.tumorMutationalBurdenEvidence + it.characteristics.tumorMutationalLoadEvidence
            }.filterNotNull().flatMap { it.treatmentEvidence }.associateBy { scorer.score(it) }

    }
}