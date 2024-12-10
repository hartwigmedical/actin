package com.hartwig.actin.algo.evaluation.Intolerance

import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

object IntoleranceFunctions {

    fun hasIcdMatch(intolerance: Intolerance, targetIcdCodes: List<String>, icdModel: IcdModel): Boolean {
        return targetIcdCodes.any { it in icdModel.returnCodeWithParents(intolerance.icdCode) }
    }
}