package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.datamodel.clinical.IhcTest

internal object IhcTestFactory {

    fun create(
        item: String,
        scoreText: String? = null,
        score: Double? = null,
        scoreLowerBound: Double? = score,
        scoreUpperBound: Double? = score,
        scoreValueUnit: String? = null,
        impliesPotentialIndeterminateStatus: Boolean = false,
        measure: String? = null
    ) = IhcTest(
        item = item,
        measure = measure,
        scoreText = scoreText,
        scoreLowerBound = scoreLowerBound,
        scoreUpperBound = scoreUpperBound,
        scoreValueUnit = scoreValueUnit,
        impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
    )
}
