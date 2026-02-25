package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.Disruption

class HomozygousDisruptionExtractor(private val geneFilter: GeneFilter) {

    fun extractHomozygousDisruptions(disruptions: List<Disruption>): List<HomozygousDisruption> {
        return relevantHomozygousDisruptions(disruptions).filter { MappingUtil.includedInGeneFilter(it, geneFilter) }
            .map { homozygousDisruption ->
                HomozygousDisruption(
                    gene = homozygousDisruption.gene(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    isReportable = true,
                    event = homozygousDisruption.event(),
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ExtractionUtil.noEvidence(),
                )
            }
            .distinctBy { it.gene }
            .sorted()
    }

    private fun relevantHomozygousDisruptions(disruptions: List<Disruption>): Set<Disruption> {
        return disruptions.filter { it.isHomozygous }.toSet()
    }
}