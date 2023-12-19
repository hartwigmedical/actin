package com.hartwig.actin.molecular.datamodel.pharmaco

import org.apache.logging.log4j.util.Strings

object TestPharmacoFactory {
    @JvmStatic
    fun builder(): ImmutableHaplotype.Builder {
        return ImmutableHaplotype.builder().name(Strings.EMPTY).function(Strings.EMPTY)
    }
}
