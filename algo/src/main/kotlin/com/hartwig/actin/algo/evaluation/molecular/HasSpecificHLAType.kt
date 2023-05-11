package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSpecificHLAType internal constructor(private val hlaAlleleToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val immunology = record.molecular().immunology()
        if (!immunology.isReliable) {
            return EvaluationFactory.recoverableUndetermined("HLA typing has not been performed reliably", "HLA typing")
        }
        var hasAlleleUnmodifiedInTumor = false
        var hasAlleleModifiedInTumor = false
        for (hlaAllele in immunology.hlaAlleles()) {
            if (hlaAllele.name() == hlaAlleleToFind) {
                val alleleIsPresentInTumor = hlaAllele.tumorCopyNumber() >= 0.5
                val alleleHasSomaticMutations = hlaAllele.hasSomaticMutations()
                if (alleleIsPresentInTumor && !alleleHasSomaticMutations) {
                    hasAlleleUnmodifiedInTumor = true
                } else {
                    hasAlleleModifiedInTumor = true
                }
            }
        }
        if (hasAlleleUnmodifiedInTumor) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addInclusionMolecularEvents(hlaAlleleToFind)
                .addPassSpecificMessages(
                    "Patient has HLA type " + hlaAlleleToFind + " which is equal to required allele type " + hlaAlleleToFind
                            + ", this allele is present and without somatic variants in tumor"
                )
                .addPassGeneralMessages("HLA type")
                .build()
        } else if (hasAlleleModifiedInTumor) {
            return unrecoverable()
                .result(EvaluationResult.WARN)
                .addInclusionMolecularEvents(hlaAlleleToFind)
                .addWarnSpecificMessages(
                    "Patient has HLA type " + hlaAlleleToFind + " which is equal to required allele type " + hlaAlleleToFind
                            + ", however, this allele is affected in tumor."
                )
                .addWarnGeneralMessages("HLA type")
                .build()
        }
        return EvaluationFactory.fail("Patient does not have HLA type '$hlaAlleleToFind'", "HLA typing")
    }
}