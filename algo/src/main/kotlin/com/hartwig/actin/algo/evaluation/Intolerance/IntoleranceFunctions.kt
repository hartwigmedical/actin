package com.hartwig.actin.algo.evaluation.Intolerance

import com.hartwig.actin.algo.evaluation.IcdMatches
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions.findInstancesMatchingAnyIcdCode
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

object IntoleranceFunctions {

    fun findIntoleranceMatchingAnyIcdCode(
        icdModel: IcdModel,
        record: PatientRecord,
        targetIcdCodes: Set<IcdCode>
    ): IcdMatches<Intolerance> {

        return findInstancesMatchingAnyIcdCode(icdModel, record.intolerances, targetIcdCodes)
    }
}