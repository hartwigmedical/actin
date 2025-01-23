package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.sort.driver.HomozygousDisruptionComparator
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.linx.LinxRecord

internal class HomozygousDisruptionExtractor(private val geneFilter: GeneFilter) {

    fun extractHomozygousDisruptions(linx: LinxRecord): List<HomozygousDisruption> {
        val relevantHomozygousDisruptions = relevantHomozygousDisruptions(linx)

        relevantHomozygousDisruptions.find { !geneFilter.include(it.gene()) }
            ?.let { homozygousDisruption ->
                throw IllegalStateException(
                    "Filtered a reported homozygous disruption through gene filtering: '${homozygousDisruption.gene()}'. "
                            + "Please make sure '${homozygousDisruption.gene()}' is configured as a known gene."
                )
            }

        return relevantHomozygousDisruptions.map { homozygousDisruption ->
            HomozygousDisruption(
                gene = homozygousDisruption.gene(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                isReportable = true,
                event = DriverEventFactory.homozygousDisruptionEvent(homozygousDisruption),
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = ExtractionUtil.noEvidence(),
            )
        }
            .distinctBy { it.gene }
            .sortedWith(HomozygousDisruptionComparator())
    }

    private fun relevantHomozygousDisruptions(linx: LinxRecord): Set<com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption> {
        return listOfNotNull(linx.somaticHomozygousDisruptions(), linx.germlineHomozygousDisruptions()).flatten().toSet()
    }
}