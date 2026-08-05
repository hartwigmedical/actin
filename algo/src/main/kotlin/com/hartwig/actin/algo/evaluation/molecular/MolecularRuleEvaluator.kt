package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult

object MolecularRuleEvaluator {

    fun geneIsAmplifiedForPatient(gene: String, record: PatientRecord, labels: EvaluationLabels.Molecular): Boolean {
        return GeneIsAmplified(gene, null, labels).evaluate(record).result == EvaluationResult.PASS
    }

    fun geneIsInactivatedForPatient(gene: String, record: PatientRecord, labels: EvaluationLabels.Molecular): Boolean {
        return GeneIsInactivated(gene, false, labels).evaluate(record).result == EvaluationResult.PASS
    }
}