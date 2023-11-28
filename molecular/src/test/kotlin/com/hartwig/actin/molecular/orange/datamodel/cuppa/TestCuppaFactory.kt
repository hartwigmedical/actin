package com.hartwig.actin.molecular.orange.datamodel.cuppa

import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaPrediction
import org.apache.logging.log4j.util.Strings

object TestCuppaFactory {

    fun builder(): ImmutableCuppaPrediction.Builder {
        return ImmutableCuppaPrediction.builder().cancerType(Strings.EMPTY).likelihood(0.0)
    }
}
