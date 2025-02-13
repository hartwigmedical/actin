package com.hartwig.actin.molecular.hotspotcomparison

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory.createMinimalTestOrangeRecord
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.common.GeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import junit.framework.TestCase.assertEquals
import org.junit.Test

private val HOTSPOT_SERVE = TestServeKnownFactory.hotspotBuilder().gene("gene1").chromosome("1").position(1).ref("ref1").alt("alt1")
    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION).geneRole(GeneRole.ONCO).sources(setOf(ActionabilityConstants.EVIDENCE_SOURCE)).build()

private val SERVE_RECORD = ImmutableServeRecord.builder()
    .knownEvents(ImmutableKnownEvents.builder().addHotspots(HOTSPOT_SERVE).build())
    .evidences(emptyList())
    .trials(emptyList())
    .build()

private val PURPLE_VARIANT_1 = TestPurpleFactory.variantBuilder()
    .gene("gene1")
    .chromosome("1")
    .position(1)
    .ref("ref1")
    .alt("alt1")
    .hotspot(HotspotType.HOTSPOT)
    .build()

private val PURPLE_VARIANT_2 = TestPurpleFactory.variantBuilder()
    .gene("gene2")
    .chromosome("2")
    .position(2)
    .ref("ref2")
    .alt("alt2")
    .hotspot(HotspotType.HOTSPOT)
    .build()

private val PURPLE_VARIANT_3 = TestPurpleFactory.variantBuilder()
    .gene("gene3")
    .chromosome("3")
    .position(3)
    .ref("ref3")
    .alt("alt3")
    .hotspot(HotspotType.NON_HOTSPOT)
    .build()

private val PURPLE_RECORD = ImmutablePurpleRecord.builder()
    .from(createMinimalTestOrangeRecord().purple())
    .addAllSomaticVariants(PURPLE_VARIANT_1, PURPLE_VARIANT_2, PURPLE_VARIANT_3).build()

private val ORANGE_RECORD = ImmutableOrangeRecord.builder()
    .from(createMinimalTestOrangeRecord())
    .purple(PURPLE_RECORD).build()

class HotspotComparisonApplicationTest {

    @Test
    fun `Should compare hotspots between ORANGE and SERVE`() {
        val hotspots = HotspotComparisonApplication(HotspotComparisonConfig("", "", "")).annotateHotspots(ORANGE_RECORD, SERVE_RECORD)
        assertEquals(hotspots.count { it.isHotspotOrange }, 2)
        assertEquals(hotspots.count { it.isHotspotServe }, 1)
        assertEquals(hotspots.count { !(it.isHotspotOrange && it.isHotspotServe) }, 1)
    }
}