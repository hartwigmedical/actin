package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import org.junit.Assert
import org.junit.Test

class EvidenceDatabaseTest {
    @Test
    fun canMatchEvidenceForSignatures() {
        // TODO (KZP): review EvidenceDatabas api to see if reasonable to remove nullability, then clean up !!'s here
        val database = TestEvidenceDatabaseFactory.createProperDatabase()
        Assert.assertNull(database.evidenceForMicrosatelliteStatus(null))
        Assert.assertEquals(0, evidenceCount(database.evidenceForMicrosatelliteStatus(false)!!).toLong())
        Assert.assertEquals(1, evidenceCount(database.evidenceForMicrosatelliteStatus(true)!!).toLong())
        Assert.assertNull(database.evidenceForHomologousRepairStatus(null))
        Assert.assertEquals(0, evidenceCount(database.evidenceForHomologousRepairStatus(false)!!).toLong())
        Assert.assertEquals(1, evidenceCount(database.evidenceForHomologousRepairStatus(true)!!).toLong())
        Assert.assertNull(database.evidenceForTumorMutationalBurdenStatus(null))
        Assert.assertEquals(0, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(false)!!).toLong())
        Assert.assertEquals(1, evidenceCount(database.evidenceForTumorMutationalBurdenStatus(true)!!).toLong())
        Assert.assertNull(database.evidenceForTumorMutationalLoadStatus(null))
        Assert.assertEquals(0, evidenceCount(database.evidenceForTumorMutationalLoadStatus(false)!!).toLong())
        Assert.assertEquals(1, evidenceCount(database.evidenceForTumorMutationalLoadStatus(true)!!).toLong())
    }

    @Test
    fun canMatchEvidenceForDrivers() {
        val database = TestEvidenceDatabaseFactory.createProperDatabase()

        // Assume default ORANGE objects match with default SERVE objects
        val variant: PurpleVariant = TestPurpleFactory.variantBuilder().reported(true).build()
        Assert.assertNotNull(database.geneAlterationForVariant(variant))
        Assert.assertEquals(1, evidenceCount(database.evidenceForVariant(variant)).toLong())
        val gainLoss: PurpleGainLoss = TestPurpleFactory.gainLossBuilder().build()
        Assert.assertNotNull(database.geneAlterationForCopyNumber(gainLoss))
        Assert.assertEquals(1, evidenceCount(database.evidenceForCopyNumber(gainLoss)).toLong())
        val homozygousDisruption: HomozygousDisruption = TestLinxFactory.homozygousDisruptionBuilder().build()
        Assert.assertNotNull(database.geneAlterationForHomozygousDisruption(homozygousDisruption))
        Assert.assertEquals(2, evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption)).toLong())
        val breakend: LinxBreakend = TestLinxFactory.breakendBuilder().reportedDisruption(true).build()
        Assert.assertNotNull(database.geneAlterationForBreakend(breakend))
        Assert.assertEquals(1, evidenceCount(database.evidenceForBreakend(breakend)).toLong())
        val fusion: LinxFusion = TestLinxFactory.fusionBuilder().reported(true).build()
        Assert.assertNotNull(database.lookupKnownFusion(fusion))
        Assert.assertEquals(2, evidenceCount(database.evidenceForFusion(fusion)).toLong())
        val virus: AnnotatedVirus = TestVirusInterpreterFactory.builder().reported(true).interpretation(VirusInterpretation.HPV).build()
        Assert.assertEquals(1, evidenceCount(database.evidenceForVirus(virus)).toLong())
    }

    companion object {
        private fun evidenceCount(match: ActionabilityMatch): Int {
            return match.onLabelEvents.size + match.offLabelEvents.size
        }
    }
}