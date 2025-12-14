package com.unioptima.backendservice.component;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Generic Converter that maps specific fields to POJO properties
 * and dumps everything else into a 'extraFields' Map.
 *
 * @param <T> The target model class (e.g., EncodedDemandData)
 */
public abstract class AbstractDynamicFieldConverter<T> implements Converter<Document, T> {

    // Fields that we manually map to class properties.
    // These will be EXCLUDED from the 'extraFields' map.
    private static final Set<String> KNOWN_FIELDS = Set.of(
            "_id",
            "idOSYM",
            "current_academicYear",
            "_class" // Spring metadata
    );

    /**
     * Subclasses must implement this to return an empty instance of their specific model.
     */
    protected abstract T createInstance();

    /**
     * Subclasses must implement this to set the standard fields on the instance.
     */
    protected abstract void populateStandardFields(T instance, Document source);

    /**
     * Subclasses must implement this to set the map on the instance.
     */
    protected abstract void setExtraFields(T instance, Map<String, Object> map);

    @Override
    public T convert(Document source) {
        T instance = createInstance();

        // 1. Let the subclass map the known hard-coded fields (ID, Year, etc.)
        populateStandardFields(instance, source);

        // 2. Iterate dynamically to fill the map
        Map<String, Object> dynamicMap = new HashMap<>();

        for (String key : source.keySet()) {
            if (!KNOWN_FIELDS.contains(key)) {
                // This puts "lag_P", "quota", etc. into the map
                dynamicMap.put(key, source.get(key));
            }
        }

        // 3. Set the map on the instance
        setExtraFields(instance, dynamicMap);

        return instance;
    }
}