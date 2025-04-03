package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasGeneWithHaplotype(private val gene: String, private val haplotype: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Undetermined if patient has $gene haplotype $haplotype")
    }
}