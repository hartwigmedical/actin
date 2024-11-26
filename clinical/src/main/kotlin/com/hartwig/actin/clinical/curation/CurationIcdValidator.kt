package com.hartwig.actin.clinical.curation

import com.hartwig.actin.icd.IcdModel

class CurationIcdValidator(private val icdModel: IcdModel) {
    fun isValidIcdTitle(icdTitle: String): Boolean {
        return icdModel.titleToCodeMap.containsKey(icdTitle)
    }

    fun getCodeFromTitle(icdTitle: String): String? {
        return icdModel.titleToCodeMap[icdTitle]
    }
}