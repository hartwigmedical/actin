package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

class HasUGT1A1Haplotype(private val haplotypeToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val pharmaco = record.molecular().pharmaco()

        if (pharmaco.none { it.gene() == "UGT1A1" }) {
            return EvaluationFactory.recoverableUndetermined("UGT1A1 haplotype undetermined", "UGT1A1 haplotype undetermined")
        }

        return if (hasUGT1A1Type(pharmaco, haplotypeToFind)) {
            EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addInclusionMolecularEvents(haplotypeToFind)
                .addPassSpecificMessages("Patient has required UGT1A1 type $haplotypeToFind")
                .addPassGeneralMessages("Patient has required UGT1A1 type")
                .build()
        } else {
            EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have required HLA type $haplotypeToFind")
                .addFailGeneralMessages("Patient does not have required HLA type")
                .build()
        }
    }


    private fun hasUGT1A1Type(pharmaco: Set<PharmacoEntry>, hapolotypeToFind: String): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene() == "UGT1A1" && pharmacoEntry.haplotypes().any { it.name() == hapolotypeToFind }) {
                return true
            }
        }
        return false
    }

}