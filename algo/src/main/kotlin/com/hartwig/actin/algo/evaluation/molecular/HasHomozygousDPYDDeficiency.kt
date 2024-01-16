package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

class HasHomozygousDPYDDeficiency internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val pharmaco = record.molecular().pharmaco()

        if (pharmaco.find { it.gene() == "DPYD" } == null) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        val isHomozygousDeficient = isHomozygousDeficient(pharmaco)

        if (isHomozygousDeficient) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addInclusionMolecularEvents("DPYD deficient")
                .addPassSpecificMessages("Patient is homozygous DPYD deficient")
                .addPassGeneralMessages("Patient is homozygous DPYD deficient")
                .build()
        }

        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("Patient is not homozygous DPYD deficient")
            .addFailGeneralMessages("Patient is not homozygous DPYD deficient")
            .build()


    }

    private fun isHomozygousDeficient(pharmaco: Set<PharmacoEntry>): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene() == "DPYD") {
                for (haplotype in pharmacoEntry.haplotypes()) {
                    if (haplotype.function() == "Normal function") {
                        return false
                    }
                }
            }
        }
        return true
    }

}


