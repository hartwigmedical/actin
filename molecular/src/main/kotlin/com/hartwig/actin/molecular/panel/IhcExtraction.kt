package com.hartwig.actin.molecular.panel

import java.time.LocalDate

data class IhcExtraction(
    val date: LocalDate?,
    val fusionTestedGenes: Set<String>,
    val mutationAndDeletionTestedGenes: Set<String>
)