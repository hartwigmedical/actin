package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

object ComplicationFunctions {

    fun findComplicationsMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<Complication> {
        val matches = icdModel.findInstancesMatchingAnyIcdCode(record.complications, targetIcdCodes)

        return IcdMatches(
            fullMatches = matches.fullMatches,
            mainCodeMatchesWithUnknownExtension = matches.mainCodeMatchesWithUnknownExtension
        )
    }

    fun isYesInputComplication(complication: Complication): Boolean {
        return complication.name.isEmpty()
    }
}