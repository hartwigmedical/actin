package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.AtcLevel

interface AtcTree {
    fun resolve(rawAtcCode: String): AtcLevel

    companion object {
        fun create(): AtcTree{
            return object: AtcTree{
                override fun resolve(rawAtcCode: String): AtcLevel {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}