package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult

object MolecularRuleEvaluator {
    fun geneIsAmplifiedForPatient(gene: String, record: PatientRecord): Boolean {
        return GeneIsAmplified(gene, null).evaluate(record).result == EvaluationResult.PASS
    }

    fun geneIsInactivatedForPatient(gene: String, record: PatientRecord): Boolean {
        return GeneIsInactivated(gene).evaluate(record).result == EvaluationResult.PASS
    }
}