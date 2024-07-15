package com.hartwig.actin.molecular.orange.datamodel.cuppa

import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaPrediction

object TestCuppaFactory {

    fun builder(): ImmutableCuppaPrediction.Builder {
        return ImmutableCuppaPrediction.builder().cancerType("").likelihood(0.0)
    }
}
