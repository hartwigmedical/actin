package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalFusion
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalVariant
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalVirus
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
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

        // Assume default objects match with default SERVE objects
        val variant = minimalVariant()
        assertNotNull(database.geneAlterationForVariant(variant))
        assertEquals(1, evidenceCount(database.evidenceForVariant(variant)).toLong())

        val gainLoss = minimalCopyNumber().copy(type = CopyNumberType.LOSS)
        assertNotNull(database.geneAlterationForCopyNumber(gainLoss))
        assertEquals(1, evidenceCount(database.evidenceForCopyNumber(gainLoss)).toLong())

        val homozygousDisruption = TestMolecularFactory.minimalHomozygousDisruption()
        assertNotNull(database.geneAlterationForHomozygousDisruption(homozygousDisruption))
        assertEquals(2, evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption)).toLong())

        val disruption = minimalDisruption().copy(isReportable = true)
        assertNotNull(database.geneAlterationForBreakend(disruption))
        assertEquals(1, evidenceCount(database.evidenceForBreakend(disruption)).toLong())

        val fusion = minimalFusion().copy(isReportable = true)
        assertNotNull(database.lookupKnownFusion(fusion))
        assertEquals(2, evidenceCount(database.evidenceForFusion(fusion)).toLong())

        val virus = minimalVirus().copy(isReportable = true, type = VirusType.HUMAN_PAPILLOMA_VIRUS)
        assertEquals(1, evidenceCount(database.evidenceForVirus(virus)).toLong())
    }

    companion object {
        private fun evidenceCount(match: ActionabilityMatch): Int {
            return match.onLabelEvents.size + match.offLabelEvents.size
        }
    }
}