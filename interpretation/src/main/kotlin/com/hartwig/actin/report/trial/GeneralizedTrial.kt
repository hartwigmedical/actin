package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import java.util.*

data class GeneralizedTrial(
    val trialId: String,
    val nctId: String?,
    val source: String,
    val acronym: String?,
    val title: String,
    val isOpen: Boolean?,
    val hasSlots: Boolean?,
    val countries: SortedSet<CountryDetails>,
    val therapyNames: SortedSet<String>,
    val actinMolecularEvents: SortedSet<String>,
    val sourceMolecularEvents: SortedSet<String>,
    val applicableCancerTypes: SortedSet<CancerType>,
    val url: String
)