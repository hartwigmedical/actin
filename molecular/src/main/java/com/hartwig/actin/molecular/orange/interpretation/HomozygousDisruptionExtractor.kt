package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator
import com.hartwig.hmftools.datamodel.linx.LinxRecord

internal class HomozygousDisruptionExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {
    fun extractHomozygousDisruptions(linx: LinxRecord): MutableSet<HomozygousDisruption> {
        val homozygousDisruptions: MutableSet<HomozygousDisruption> = Sets.newTreeSet(HomozygousDisruptionComparator())
        for (homozygousDisruption in relevantHomozygousDisruptions(linx)) {
            if (geneFilter.include(homozygousDisruption.gene())) {
                homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                    .from(GeneAlterationFactory.convertAlteration(homozygousDisruption.gene(),
                        evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)))
                    .isReportable(true)
                    .event(DriverEventFactory.homozygousDisruptionEvent(homozygousDisruption))
                    .driverLikelihood(DriverLikelihood.HIGH)
                    .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption)))
                    .build())
            } else {
                throw IllegalStateException(
                    "Filtered a reported homozygous disruption through gene filtering: '" + homozygousDisruption.gene() + "'. "
                            + "Please make sure '" + homozygousDisruption.gene() + "' is configured as a known gene.")
            }
        }
        return homozygousDisruptions
    }

    companion object {
        private fun relevantHomozygousDisruptions(linx: LinxRecord): MutableSet<com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption> {
            val disruptions: MutableSet<com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption> = Sets.newHashSet()
            disruptions.addAll(linx.somaticHomozygousDisruptions())
            val germlineHomozygousDisruptions = linx.germlineHomozygousDisruptions()
            if (germlineHomozygousDisruptions != null) {
                disruptions.addAll(germlineHomozygousDisruptions)
            }
            return disruptions
        }
    }
}
