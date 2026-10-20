package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdMatches

object DrugAllergyFunctions {

    fun <T : Comorbidity> findDrugAllergies(icdModel: IcdModel, comorbidities: List<T>, drugExtensionCodes: Set<String>): IcdMatches<T> {
        val fullMatches = icdModel.findInstancesMatchingAnyIcdCode(
            comorbidities, targetIcdCodes(IcdConstants.DRUG_ALLERGY_SET, drugExtensionCodes)
        ).fullMatches

        val unknownDrugMatches = icdModel.findInstancesMatchingAnyIcdCode(
            comorbidities - fullMatches.toSet(), targetIcdCodes(IcdConstants.DRUG_SPECIFIC_ALLERGY_SET, drugExtensionCodes)
        ).mainCodeMatchesWithUnknownExtension

        return IcdMatches(fullMatches, unknownDrugMatches)
    }

    private fun targetIcdCodes(mainCodes: Set<String>, extensionCodes: Set<String>): List<IcdCode> =
        mainCodes.flatMap { mainCode -> extensionCodes.map { extensionCode -> IcdCode(mainCode, extensionCode) } }
}
