package com.hartwig.actin.doid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.doid.config.AdenoSquamousMapping;
import com.hartwig.actin.doid.config.DoidManualConfig;
import com.hartwig.actin.doid.config.ImmutableAdenoSquamousMapping;
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DoidModelTest {

    @Test
    public void canAddParents() {
        DoidModel model = createTestModel(TestDoidManualConfigFactory.createWithOneAdditionalDoid("300", "301"));

        Set<String> withParents200 = model.doidWithParents("200");
        assertEquals(4, withParents200.size());
        assertTrue(withParents200.contains("200"));
        assertTrue(withParents200.contains("300"));
        assertTrue(withParents200.contains("301"));
        assertTrue(withParents200.contains("400"));

        Set<String> withParents300 = model.doidWithParents("300");
        assertEquals(3, withParents300.size());
        assertFalse(withParents300.contains("200"));
        assertTrue(withParents300.contains("300"));
        assertTrue(withParents300.contains("301"));
        assertTrue(withParents300.contains("400"));

        Set<String> withParents400 = model.doidWithParents("400");
        assertEquals(1, withParents400.size());
        assertFalse(withParents400.contains("200"));
        assertFalse(withParents400.contains("300"));
        assertFalse(withParents400.contains("301"));
        assertTrue(withParents400.contains("400"));

        assertEquals(1, model.doidWithParents("500").size());
    }

    @Test
    public void canResolveMainCancerTypes() {
        DoidModel doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneMainCancerDoid("123"));

        assertFalse(doidModel.mainCancerDoids("123").isEmpty());
        assertTrue(doidModel.mainCancerDoids("does not exist").isEmpty());
    }

    @Test
    public void canResolveAdenoSquamousMappings() {
        AdenoSquamousMapping mapping =
                ImmutableAdenoSquamousMapping.builder().adenoSquamousDoid("1").squamousDoid("2").adenoDoid("3").build();
        DoidModel doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping));

        assertEquals(0, doidModel.adenoSquamousMappingsForDoid("1").size());
        assertEquals(1, doidModel.adenoSquamousMappingsForDoid("2").size());
        assertEquals(1, doidModel.adenoSquamousMappingsForDoid("3").size());
        assertEquals(0, doidModel.adenoSquamousMappingsForDoid("4").size());
    }

    @Test
    public void canResolveTerms() {
        DoidModel model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());

        assertEquals("tumor A", model.resolveTermForDoid("200"));
        assertNull(model.resolveTermForDoid("300"));
    }

    @Test
    public void canResolveDoids() {
        DoidModel model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());

        assertEquals("200", model.resolveDoidForTerm("tumor a"));
        assertEquals("200", model.resolveDoidForTerm("tumor A"));
        assertEquals("200", model.resolveDoidForTerm("Tumor A"));
        assertNull(model.resolveDoidForTerm("tumor B"));
    }

    @NotNull
    private static DoidModel createTestModel(@NotNull DoidManualConfig manualConfig) {
        Multimap<String, String> childToParentsMap = ArrayListMultimap.create();
        childToParentsMap.put("200", "300");
        childToParentsMap.put("300", "400");

        Map<String, String> termPerDoidMap = Maps.newHashMap();
        termPerDoidMap.put("200", "tumor A");

        Map<String, String> doidPerLowerCaseTermMap = Maps.newHashMap();
        doidPerLowerCaseTermMap.put("tumor a", "200");

        return new DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, manualConfig);
    }
}