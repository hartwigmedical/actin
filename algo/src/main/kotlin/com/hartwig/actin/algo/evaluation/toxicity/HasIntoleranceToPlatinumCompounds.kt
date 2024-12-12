package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.Intolerance.IntoleranceFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceToPlatinumCompounds(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        //TODO(Change && to || when able to read out platinum extensioncode)
        val platinumAllergies = record.intolerances
            .filter { intolerance ->
                stringCaseInsensitivelyMatchesQueryCollection(
                    intolerance.name,
                    PLATINUM_COMPOUNDS
                ) && IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(
                    icdModel,
                    record,
                    IcdConstants.DRUG_ALLERGY_SET.map { IcdCode(it) }.toSet()
                ).fullMatches.contains(intolerance)
            }
            .map(Intolerance::name)
            .toSet()

        return if (platinumAllergies.isNotEmpty()) {
            EvaluationFactory.pass("Patient has allergy to a platinum compounds: " + Format.concatWithCommaAndAnd(platinumAllergies))
        } else
            EvaluationFactory.fail("No known allergy to platinum compounds")
    }

    companion object {
        val PLATINUM_COMPOUNDS =
            setOf("oxaliplatin", "eloxatin", "carboplatin", "paraplatin", "cisplatin", "platinol", "imifolatin", "nedaplatin", "NC-6004")
    }
}