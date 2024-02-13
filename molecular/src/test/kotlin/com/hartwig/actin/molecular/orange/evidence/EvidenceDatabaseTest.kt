package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EvidenceDatabaseTest {

    @Test
    fun canMatchEvidenceForSignatures() {
        // TODO (KZP): review EvidenceDatabase api to see if reasonable to remove nullability, then clean up !!'s here
        val database = TestEvidenceDatabaseFactory.createProperDatabase()
        assertNull(database.evidenceForMicrosatelliteStatus(null))
        assertEquals(0, evidenceCount(database.evidenceForMicrosatelliteStatus(false)!!).toLong())
        assertEquals(1, evidenceCount(database.evidenceForMicrosatelliteStatus(true)!!).toLong())
        assertNull(database.evidenceForHomologousRepairStatus(null))
        assertEquals(0, evidenceCount(database.evidenceForHomologousRepairStatus(false)!!).toLong())
        assertEquals(1, evidenceCount(database.evidenceForHomologousRepairStatus(true)!!).toLong())
        assertNull(database.evidenceForTumorMutationalBurdenStatus(null))
        assertEquals(0, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(false)!!).toLong())
        assertEquals(1, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(true)!!).toLong())
        assertNull(database.evidenceForTumorMutationalLoadStatus(null))
        assertEquals(0, evidenceCount(database.evidenceForTumorMutationalLoadStatus(false)!!).toLong())
        assertEquals(1, evidenceCount(database.evidenceForTumorMutationalLoadStatus(true)!!).toLong())
    }

    @Test
    fun canMatchEvidenceForDrivers() {
        val database = TestEvidenceDatabaseFactory.createProperDatabase()

        // Assume default ORANGE objects match with default SERVE objects
        val variant: PurpleVariant = TestPurpleFactory.variantBuilder().reported(true).build()
        assertNotNull(database.geneAlterationForVariant(variant))
        assertEquals(1, evidenceCount(database.evidenceForVariant(variant)).toLong())

        val gainLoss: PurpleGainLoss = TestPurpleFactory.gainLossBuilder().build()
        assertNotNull(database.geneAlterationForGainLoss(gainLoss))
        assertEquals(1, evidenceCount(database.evidenceForCopyNumber(gainLoss)).toLong())

        val homozygousDisruption: LinxHomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().build()
        assertNotNull(database.geneAlterationForHomozygousDisruption(homozygousDisruption))
        assertEquals(2, evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption)).toLong())

        val breakend: LinxBreakend = TestLinxFactory.breakendBuilder().reported(true).build()
        assertNotNull(database.geneAlterationForBreakend(breakend))
        assertEquals(1, evidenceCount(database.evidenceForBreakend(breakend)).toLong())

        val fusion: LinxFusion = TestLinxFactory.fusionBuilder().reported(true).build()
        assertNotNull(database.lookupKnownFusion(fusion))
        assertEquals(2, evidenceCount(database.evidenceForFusion(fusion)).toLong())

        val virus: VirusInterpreterEntry = TestVirusInterpreterFactory.builder().reported(true).interpretation(VirusInterpretation.HPV).build()
        assertEquals(1, evidenceCount(database.evidenceForVirus(virus)).toLong())
    }

    companion object {
        private fun evidenceCount(match: ActionabilityMatch): Int {
            return match.onLabelEvents.size + match.offLabelEvents.size
        }
    }
}