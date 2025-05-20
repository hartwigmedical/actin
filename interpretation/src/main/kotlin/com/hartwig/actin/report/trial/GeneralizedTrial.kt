package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.MolecularMatchDetails

data class GeneralizedTrial(
    val trialId: String,
    val nctId: String?,
    val source: String,
    val acronym: String?,
    val title: String,
    val isOpen: Boolean?,
    val hasSlots: Boolean?,
    val countries: Set<CountryDetails>,
    val treatments: Set<String>,
    val molecularMatches: Set<MolecularMatchDetails>,
    val actinMolecularEvents: Set<String>,
    val sourceMolecularEvents: Set<String>,
    val applicableCancerTypes: Set<CancerType>,
    val url: String?
)