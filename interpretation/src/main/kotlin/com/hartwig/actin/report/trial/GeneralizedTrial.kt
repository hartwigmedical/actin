package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import java.util.SortedSet

data class GeneralizedTrial(
    val trialId: String,
    val nctId: String?,
    val source: String,
    val acronym: String?,
    val title: String,
    val isOpen: Boolean?,
    val hasSlots: Boolean?,
    val countries: Set<CountryDetails>,
    val therapyNames: Set<String>,
    val actinMolecularEvents: Set<String>,
    val sourceMolecularEvents: Set<String>,
    val applicableCancerTypes: Set<CancerType>,
    val url: String?
)