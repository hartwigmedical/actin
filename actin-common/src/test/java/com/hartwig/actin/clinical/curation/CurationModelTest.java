package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.junit.Test;

public class CurationModelTest {

    private static final String CURATION_DIRECTORY = Resources.getResource("clinical/curation").getPath();

    @Test
    public void canCreateFromCurationDirectory() throws IOException {
        assertNotNull(CurationModel.fromCurationDirectory(CURATION_DIRECTORY));
    }

    @Test
    public void canCurateTumorDetails() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        TumorDetails curated = model.curateTumorDetails("Unknown", "Carcinoma");
        assertEquals("Unknown", curated.primaryTumorLocation());

        TumorDetails missing = model.curateTumorDetails("Does not", "Exist");
        assertNull(missing.primaryTumorLocation());

        model.evaluate();
    }

    @Test
    public void canCurateCancerRelatedComplications() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<CancerRelatedComplication> cancerRelatedComplications =
                model.curateCancerRelatedComplications(Lists.newArrayList("term", "no curation"));

        assertEquals(2, cancerRelatedComplications.size());
        assertTrue(cancerRelatedComplications.contains(ImmutableCancerRelatedComplication.builder().name("curated").build()));
        assertTrue(cancerRelatedComplications.contains(ImmutableCancerRelatedComplication.builder().name("no curation").build()));

        assertTrue(model.curateCancerRelatedComplications(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorTreatments() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorTumorTreatment> priorTreatments =
                model.curatePriorTumorTreatments(Lists.newArrayList("Resection 2020", "no systemic treatment", "cannot curate"));

        assertEquals(1, priorTreatments.size());
        assertEquals("Primary Resection", priorTreatments.get(0).surgeryType());

        assertTrue(model.curatePriorTumorTreatments(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorSecondPrimaries() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorSecondPrimary> priorSecondPrimaries =
                model.curatePriorSecondPrimaries(Lists.newArrayList("Breast cancer 2018", "no a second primary", "cannot curate"));

        assertEquals(1, priorSecondPrimaries.size());
        assertEquals("Breast", priorSecondPrimaries.get(0).tumorLocation());

        assertTrue(model.curatePriorSecondPrimaries(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCuratePriorOtherConditions() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        List<PriorOtherCondition> priorOtherConditions =
                model.curatePriorOtherConditions(Lists.newArrayList("sickness", "not a condition", "cannot curate"));

        assertEquals(1, priorOtherConditions.size());
        assertEquals("sick", priorOtherConditions.get(0).name());

        assertTrue(model.curatePriorOtherConditions(null).isEmpty());
        model.evaluate();
    }

    @Test
    public void canCurateECGAberrations() {
        CurationModel model = TestCurationFactory.createProperTestCurationModel();

        assertEquals("Cleaned aberration", model.curateAberrationECG("Weird aberration"));
        assertEquals("No curation needed", model.curateAberrationECG("No curation needed"));
        assertNull(model.curateAberrationECG(null));

        model.evaluate();
    }
}