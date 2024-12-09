package com.hartwig.actin.algo.evaluation.Intolerance

import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

object IntoleranceFunctions {

    fun hasIcdMatch(intolerance: Intolerance, targetIcdCode: String, icdModel: IcdModel): Boolean {
        return targetIcdCode in icdModel.returnCodeWithParents(intolerance.icdCode)
    }
}