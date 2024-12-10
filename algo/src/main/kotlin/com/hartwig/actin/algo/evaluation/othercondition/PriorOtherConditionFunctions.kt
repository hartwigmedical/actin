package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel

object PriorOtherConditionFunctions {

    fun findPriorOtherConditionsMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        icdMainCodes: List<String>,
        icdExtensionCodes: List<String>? = null
    ): OtherConditionIcdMatches {

        val (fullMatches, matchesWithUnknownExtension) = record.priorOtherConditions
            .filter { icdModel.returnCodeWithParents(it.icdMainCode).any { code -> code in icdMainCodes } }
            .filter { condition ->
                icdExtensionCodes?.let {
                    condition.icdExtensionCode?.let {
                        extension -> icdModel.returnCodeWithParents(extension).any { it in icdExtensionCodes }
                    } ?: true
                } ?: true
            }
            .partition { it.icdExtensionCode != null || icdExtensionCodes == null }

        return OtherConditionIcdMatches(fullMatches, matchesWithUnknownExtension)
    }

    data class OtherConditionIcdMatches(
        val fullMatches: List<PriorOtherCondition>,
        val mainCodeMatchesWithUnknownExtension: List<PriorOtherCondition>
    )
}