package com.unioptima.backendservice.component;
import com.unioptima.backendservice.model.EncodedDemandData;
import org.bson.Document;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ReadingConverter
public class EncodedDemandDataReadConverter extends AbstractDynamicFieldConverter<EncodedDemandData> {

    @Override
    protected EncodedDemandData createInstance() {
        return new EncodedDemandData();
    }

    @Override
    protected void populateStandardFields(EncodedDemandData instance, Document source) {
        instance.setId(source.getString("_id"));
        instance.setIdOSYM(source.getString("idOSYM"));
        instance.setAcademicYear(source.getInteger("academicYear"));
    }

    @Override
    protected void setExtraFields(EncodedDemandData instance, Map<String, Object> map) {
        instance.setExtraFields(map);
    }
}