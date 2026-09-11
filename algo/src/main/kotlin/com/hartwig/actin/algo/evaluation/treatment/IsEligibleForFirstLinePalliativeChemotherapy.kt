package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class IsEligibleForFirstLinePalliativeChemotherapy(private val hasMetastaticCancer: HasMetastaticCancer) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val palliativeTreatments = record.oncologicalHistory.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val categoriesList = Format.concatItemsWithAnd(palliativeTreatments.flatMap { it.categories() })
        val hasMetastaticCancerResult = hasMetastaticCancer.evaluate(record).result

        return when {
            hasMetastaticCancerResult == EvaluationResult.FAIL -> {
                EvaluationFactory.fail("No metastatic cancer and hence requirements for first line palliative chemotherapy are not met")
            }

            palliativeTreatments.any { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) } -> {
                EvaluationFactory.fail("Palliative chemotherapy in provided treatments and hence requirements for first line palliative chemotherapy are not met")
            }

            palliativeTreatments.isNotEmpty() && hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined("Palliative $categoriesList in provided treatments (hence requirements for first line palliative chemotherapy may not be met)")
            }

            hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined("Undetermined whether requirements for first line palliative chemotherapy are met for metastatic disease")
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if metastatic cancer (hence requirements for first line palliative chemotherapy may not be met)")
            }
        }
    }
}