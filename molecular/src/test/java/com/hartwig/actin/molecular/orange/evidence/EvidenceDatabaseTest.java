package com.hartwig.actin.molecular.orange.evidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;

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

        PurpleCopyNumber copyNumber = TestPurpleFactory.copyNumberBuilder().build();
        assertNotNull(database.geneAlterationForCopyNumber(copyNumber));
        assertEquals(1, evidenceCount(database.evidenceForCopyNumber(copyNumber)));

        LinxHomozygousDisruption homozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().build();
        assertNotNull(database.geneAlterationForHomozygousDisruption(homozygousDisruption));
        assertEquals(2, evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption)));

        LinxBreakend breakend = TestLinxFactory.breakendBuilder().reported(true).build();
        assertNotNull(database.geneAlterationForBreakend(breakend));
        assertEquals(1, evidenceCount(database.evidenceForBreakend(breakend)));

        LinxFusion fusion = TestLinxFactory.fusionBuilder().reported(true).build();
        assertNotNull(database.lookupKnownFusion(fusion));
        assertEquals(2, evidenceCount(database.evidenceForFusion(fusion)));

        VirusInterpreterEntry virus = TestVirusInterpreterFactory.builder().reported(true).interpretation(VirusInterpretation.HPV).build();
        assertEquals(1, evidenceCount(database.evidenceForVirus(virus)));
    }

    private static int evidenceCount(@NotNull ActionabilityMatch match) {
        return match.onLabelEvents().size() + match.offLabelEvents().size();
    }
}