package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import java.time.LocalDate

object MolecularRuleEvaluator {
    fun geneIsAmplifiedForPatient(gene: String, record: PatientRecord, maxTestAge: LocalDate?): Boolean {
        return GeneIsAmplified(gene, null, maxTestAge).evaluate(record).result == EvaluationResult.PASS
    }

    fun geneIsInactivatedForPatient(gene: String, record: PatientRecord, maxTestAge: LocalDate?): Boolean {
        return GeneIsInactivated(gene, maxTestAge).evaluate(record).result == EvaluationResult.PASS
    }
}