package com.hartwig.actin.algo.evaluation.intolerance

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdMatches

object IntoleranceFunctions {

    fun findIntoleranceMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<Intolerance> {
        val matches = icdModel.findInstancesMatchingAnyIcdCode(record.intolerances, targetIcdCodes)

        return IcdMatches(
            fullMatches = matches.fullMatches,
            mainCodeMatchesWithUnknownExtension = matches.mainCodeMatchesWithUnknownExtension
        )
    }
}