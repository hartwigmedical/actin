package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusQCStatus;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VirusExtractorTest {

    @Test
    public void canExtractViruses() {
        VirusInterpreterEntry virusEntry1 = TestVirusInterpreterFactory.builder()
                .reported(true)
                .name("virus 1")
                .qcStatus(VirusExtractor.QC_PASS_STATUS)
                .interpretation(VirusInterpretation.HPV)
                .integrations(2)
                .driverLikelihood(VirusDriverLikelihood.HIGH)
                .build();

        VirusInterpreterEntry virusEntry2 = TestVirusInterpreterFactory.builder()
                .reported(false)
                .name("virus 2")
                .qcStatus(VirusQCStatus.LOW_VIRAL_COVERAGE)
                .interpretation(null)
                .integrations(0)
                .driverLikelihood(VirusDriverLikelihood.LOW)
                .build();

        VirusInterpreterRecord virusInterpreter = ImmutableVirusInterpreterRecord.builder()
                .addEntries(virusEntry1, virusEntry2)
                .build();

        VirusExtractor virusExtractor = new VirusExtractor(TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Virus> viruses = virusExtractor.extract(virusInterpreter);
        assertEquals(2, viruses.size());

        Virus virus1 = findByName(viruses, "virus 1");
        assertTrue(virus1.isReportable());
        assertEquals(DriverLikelihood.HIGH, virus1.driverLikelihood());
        assertEquals(VirusType.HUMAN_PAPILLOMA_VIRUS, virus1.type());
        assertTrue(virus1.isReliable());
        assertEquals(2, virus1.integrations());

        Virus virus2 = findByName(viruses, "virus 2");
        assertFalse(virus2.isReportable());
        assertEquals(DriverLikelihood.LOW, virus2.driverLikelihood());
        assertEquals(VirusType.OTHER, virus2.type());
        assertFalse(virus2.isReliable());
        assertEquals(0, virus2.integrations());
    }

    @Test
    public void canDetermineDriverLikelihoodForAllVirusDriverLikelihoods() {
        Map<VirusDriverLikelihood, DriverLikelihood> expectedDriverLikelihoodLookup = new HashMap<>();
        expectedDriverLikelihoodLookup.put(VirusDriverLikelihood.LOW, DriverLikelihood.LOW);
        expectedDriverLikelihoodLookup.put(VirusDriverLikelihood.HIGH, DriverLikelihood.HIGH);
        expectedDriverLikelihoodLookup.put(VirusDriverLikelihood.UNKNOWN, null);

        for (VirusDriverLikelihood virusDriverLikelihood : VirusDriverLikelihood.values()) {
            assertEquals(expectedDriverLikelihoodLookup.get(virusDriverLikelihood),
                    VirusExtractor.determineDriverLikelihood(virusDriverLikelihood));
        }
    }

    @Test
    public void canDetermineTypeForAllInterpretations() {
        assertEquals(VirusType.OTHER, VirusExtractor.determineType(null));

        for (VirusInterpretation interpretation : VirusInterpretation.values()) {
            assertNotNull(VirusExtractor.determineType(interpretation));
        }
    }

    @NotNull
    private static Virus findByName(@NotNull Set<Virus> viruses, @NotNull String nameToFind) {
        for (Virus virus : viruses) {
            if (virus.name().equals(nameToFind)) {
                return virus;
            }
        }

        throw new IllegalStateException("Could not find virus with name: " + nameToFind);
    }
}