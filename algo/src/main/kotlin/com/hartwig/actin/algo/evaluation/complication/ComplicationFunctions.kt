package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.IcdCodeMatcher
import com.hartwig.actin.algo.evaluation.IcdMatches
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

object ComplicationFunctions: IcdCodeMatcher {

    fun findComplicationsMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<Complication> {
        return findInstancesMatchingAnyIcdCode(icdModel, record.complications, targetIcdCodes)
    }

    fun isYesInputComplication(complication: Complication): Boolean {
        return complication.name.isEmpty()
    }
}