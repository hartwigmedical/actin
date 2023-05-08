package com.hartwig.actin.util.json;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonSetAdapter<T> implements JsonSerializer<Set<T>> {

    @Override
    public JsonElement serialize(Set<T> set, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        Stream<T> stream = set.stream();
        if (!set.isEmpty() && set.iterator().next() instanceof Comparable) {
            stream = stream.sorted();
        }
        stream.map(context::serialize).forEach(jsonArray::add);
        return jsonArray;
    }
}
