package com.hartwig.actin.molecular.orange.evidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption;
import com.hartwig.hmftools.datamodel.linx.LinxBreakend;
import com.hartwig.hmftools.datamodel.linx.LinxFusion;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus;
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceDatabaseTest {

    @Test
    public void canMatchEvidenceForSignatures() {
        EvidenceDatabase database = TestEvidenceDatabaseFactory.createProperDatabase();

        assertNull(database.evidenceForMicrosatelliteStatus(null));
        assertEquals(0, evidenceCount(database.evidenceForMicrosatelliteStatus(false)));
        assertEquals(1, evidenceCount(database.evidenceForMicrosatelliteStatus(true)));

        assertNull(database.evidenceForHomologousRepairStatus(null));
        assertEquals(0, evidenceCount(database.evidenceForHomologousRepairStatus(false)));
        assertEquals(1, evidenceCount(database.evidenceForHomologousRepairStatus(true)));

        assertNull(database.evidenceForTumorMutationalBurdenStatus(null));
        assertEquals(0, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(false)));
        assertEquals(1, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(true)));

        assertNull(database.evidenceForTumorMutationalLoadStatus(null));
        assertEquals(0, evidenceCount(database.evidenceForTumorMutationalLoadStatus(false)));
        assertEquals(1, evidenceCount(database.evidenceForTumorMutationalLoadStatus(true)));
    }

    @Test
    public void canMatchEvidenceForDrivers() {
        EvidenceDatabase database = TestEvidenceDatabaseFactory.createProperDatabase();

        // Assume default ORANGE objects match with default SERVE objects
        PurpleVariant variant = TestPurpleFactory.variantBuilder().reported(true).build();
        assertNotNull(database.geneAlterationForVariant(variant));
        assertEquals(1, evidenceCount(database.evidenceForVariant(variant)));

        PurpleGainLoss gainLoss = TestPurpleFactory.gainLossBuilder().build();
        assertNotNull(database.geneAlterationForCopyNumber(gainLoss));
        assertEquals(1, evidenceCount(database.evidenceForCopyNumber(gainLoss)));

        HomozygousDisruption homozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().build();
        assertNotNull(database.geneAlterationForHomozygousDisruption(homozygousDisruption));
        assertEquals(2, evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption)));

        LinxBreakend breakend = TestLinxFactory.breakendBuilder().reportedDisruption(true).build();
        assertNotNull(database.geneAlterationForBreakend(breakend));
        assertEquals(1, evidenceCount(database.evidenceForBreakend(breakend)));

        LinxFusion fusion = TestLinxFactory.fusionBuilder().reported(true).build();
        assertNotNull(database.lookupKnownFusion(fusion));
        assertEquals(2, evidenceCount(database.evidenceForFusion(fusion)));

        AnnotatedVirus virus = TestVirusInterpreterFactory.builder().reported(true).interpretation(VirusInterpretation.HPV).build();
        assertEquals(1, evidenceCount(database.evidenceForVirus(virus)));
    }

    private static int evidenceCount(@NotNull ActionabilityMatch match) {
        return match.onLabelEvents().size() + match.offLabelEvents().size();
    }
}