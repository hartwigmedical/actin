package com.hartwig.actin.util.json;

import java.io.IOException;
import java.time.LocalDate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GsonLocalDateAdapter extends TypeAdapter<LocalDate> {

    @Override
    public void write(@NotNull JsonWriter writer, @Nullable LocalDate localDate) throws IOException {
        if (localDate == null) {
            writer.nullValue();
        } else {
            writer.beginObject();
            writer.name("year");
            writer.value(localDate.getYear());
            writer.name("month");
            writer.value(localDate.getMonthValue());
            writer.name("day");
            writer.value(localDate.getDayOfMonth());
            writer.endObject();
        }
    }

    @Override
    @Nullable
    public LocalDate read(@NotNull JsonReader reader) throws IOException {
        JsonToken firstToken = reader.peek();
        if (firstToken == JsonToken.NULL) {
            return null;
        }

        int year = -1;
        int month = -1;
        int day = -1;

        reader.beginObject();
        String field = reader.nextName();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();

            if (token == JsonToken.NAME) {
                field = reader.nextName();
            }

            if (field.equals("year")) {
                reader.peek();
                year = reader.nextInt();
            }

            if (field.equals("month")) {
                reader.peek();
                month = reader.nextInt();
            }

            if (field.equals("day")) {
                reader.peek();
                day = reader.nextInt();
            }
        }

        reader.endObject();

        return LocalDate.of(year, month, day);
    }
}
