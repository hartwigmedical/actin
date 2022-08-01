package com.hartwig.actin.doid;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.doid.config.DoidManualConfig;
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDoidModelFactory {

    private TestDoidModelFactory() {
    }

    @NotNull
    public static DoidModel createMinimalTestDoidModel() {
        return create(ArrayListMultimap.create(),
                Maps.newHashMap(),
                Maps.newHashMap(),
                TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    public static DoidModel createWithOneMainCancerDoid(@NotNull String mainCancerDoid) {
        return create(ArrayListMultimap.create(),
                Maps.newHashMap(),
                Maps.newHashMap(),
                TestDoidManualConfigFactory.createWithOneMainCancerDoid(mainCancerDoid));
    }

    @NotNull
    public static DoidModel createWithOneParentChild(@NotNull String parent, @NotNull String child) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        relationship.put(child, parent);
        return createWithRelationship(relationship);
    }

    @NotNull
    public static DoidModel createWithOneParentMainCancerTypeChild(@NotNull String parent, @NotNull String child) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        relationship.put(child, parent);

        return create(relationship, Maps.newHashMap(), Maps.newHashMap(), TestDoidManualConfigFactory.createWithOneMainCancerDoid(parent));
    }

    @NotNull
    public static DoidModel createWithChildParentMap(@NotNull Map<String, String> childParentMap) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        for (Map.Entry<String, String> entry : childParentMap.entrySet()) {
            relationship.put(entry.getKey(), entry.getValue());
        }
        return createWithRelationship(relationship);
    }

    @NotNull
    public static DoidModel createWithOneDoidAndTerm(@NotNull String doid, @NotNull String term) {
        Map<String, String> termPerDoidMap = Maps.newHashMap();
        termPerDoidMap.put(doid, term);

        Map<String, String> doidPerLowerCaseTermMap = Maps.newHashMap();
        doidPerLowerCaseTermMap.put(term.toLowerCase(), doid);

        return create(ArrayListMultimap.create(),
                termPerDoidMap,
                doidPerLowerCaseTermMap,
                TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    private static DoidModel createWithRelationship(@NotNull Multimap<String, String> relationship) {
        return create(relationship, Maps.newHashMap(), Maps.newHashMap(), TestDoidManualConfigFactory.createMinimalTestDoidManualConfig());
    }

    @NotNull
    private static DoidModel create(@NotNull Multimap<String, String> relationship, @NotNull Map<String, String> termPerDoidMap,
            @NotNull Map<String, String> doidPerLowerCaseTermMap, @NotNull DoidManualConfig config) {
        return new DoidModel(relationship, termPerDoidMap, doidPerLowerCaseTermMap, config);
    }
}
