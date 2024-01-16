package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

class HasSpecificUGT1A1Type internal constructor(private val haplotypeToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val pharmaco = record.molecular().pharmaco()
        val hasUGT1A1Type = hasUGT1A1Type(pharmaco, haplotypeToFind)

        if (pharmaco.find { it.gene() == "UGT1A1" } == null) {
            return EvaluationFactory.recoverableUndetermined("UGT1A1 type undetermined", "UGT1A1 type undetermined")
        }

        if (hasUGT1A1Type) {
            return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addInclusionMolecularEvents(haplotypeToFind)
                .addPassSpecificMessages("Patient has required UGT1A1 type $haplotypeToFind")
                .addPassGeneralMessages("Patient has required UGT1A1 type")
                .build()
        }

        return EvaluationFactory.unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("Patient does not have required HLA type $haplotypeToFind")
            .addFailGeneralMessages("Patient does not have required HLA type")
            .build()
    }


    private fun hasUGT1A1Type(pharmaco: Set<PharmacoEntry>, hapolotypeToFind: String): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene() == "UGT1A1") {
                for (haplotype in pharmacoEntry.haplotypes()) {
                    if (haplotype.name() == hapolotypeToFind) {
                        return true
                    }
                }
            }
        }
        return false
    }

}