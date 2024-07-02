package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.molecular.datamodel.Variant

data class GeneVariants(val gene: String, val variants: List<Variant>)