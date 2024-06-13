package io.github.akuniutka.kanban.web;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.akuniutka.kanban.exception.ManagerValidationException;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(duration.toMinutes());
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        JsonToken type = jsonReader.peek();
        if (type == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        try {
            return Duration.ofMinutes(jsonReader.nextLong());
        } catch (IllegalStateException | NumberFormatException | ArithmeticException exception) {
            throw new ManagerValidationException("wrong value for duration");
        }
    }
}
