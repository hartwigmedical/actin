package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator;

import org.jetbrains.annotations.NotNull;

class HomozygousDisruptionExtractor {

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public HomozygousDisruptionExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<HomozygousDisruption> extractHomozygousDisruptions(@NotNull LinxRecord linx) {
        Set<HomozygousDisruption> homozygousDisruptions = Sets.newTreeSet(new HomozygousDisruptionComparator());
        for (LinxHomozygousDisruption homozygousDisruption : linx.homozygousDisruptions()) {
            if (geneFilter.include(homozygousDisruption.gene())) {
                homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                        .from(GeneAlterationFactory.convertAlteration(homozygousDisruption.gene(),
                                evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)))
                        .isReportable(true)
                        .event(DriverEventFactory.homozygousDisruptionEvent(homozygousDisruption))
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption)))
                        .build());
            } else {
                throw new IllegalStateException(
                        "Filtered a reported homozygous disruption through gene filtering: '" + homozygousDisruption.gene() + "'");
            }
        }
        return homozygousDisruptions;
    }
}
