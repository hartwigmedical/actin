package com.hartwig.actin.doid;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;

public final class TestDoidModelFactory {

    private TestDoidModelFactory() {
    }

    @NotNull
    public static DoidModel createMinimalTestDoidModel() {
        return new DoidModel(ArrayListMultimap.create(), Maps.newHashMap(), Maps.newHashMap());
    }

    @NotNull
    public static DoidModel createWithOneParentChild(@NotNull String parent, @NotNull String child) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        relationship.put(child, parent);
        return createWithRelationship(relationship);
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

        return new DoidModel(ArrayListMultimap.create(), termPerDoidMap, doidPerLowerCaseTermMap);
    }

    @NotNull
    private static DoidModel createWithRelationship(@NotNull Multimap<String, String> relationship) {
        return new DoidModel(relationship, Maps.newHashMap(), Maps.newHashMap());
    }
}
