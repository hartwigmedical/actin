package com.hartwig.actin.molecular.panel

import java.time.LocalDate

data class IHCExtraction(
    val date: LocalDate?,
    val fusionPositiveGenes: Set<String>,
    val fusionNegativeGenes: Set<String>
)