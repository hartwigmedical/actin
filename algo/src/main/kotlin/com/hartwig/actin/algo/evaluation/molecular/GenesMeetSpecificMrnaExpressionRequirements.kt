package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class GenesMeetSpecificMrnaExpressionRequirements(maxTestAge: LocalDate? = null, private val genes: Set<String>) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Specific mRNA expression requirements for gene(s) ${Format.concat(genes)} undetermined",
            isMissingMolecularResultForEvaluation = true
        )
    }
}