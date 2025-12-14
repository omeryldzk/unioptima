package com.unioptima.backendservice.component;
import com.unioptima.backendservice.model.RawData;
import org.bson.Document;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ReadingConverter
public class RawDataReadConverter extends AbstractDynamicFieldConverter<RawData> {

    @Override
    protected RawData createInstance() {
        return new RawData();
    }

    @Override
    protected void populateStandardFields(RawData instance, Document source) {
        instance.setId(source.getString("_id"));
        instance.setIdOSYM(source.getString("idOSYM"));
        instance.setAcademicYear(source.getInteger("academicYear"));
    }

    @Override
    protected void setExtraFields(RawData instance, Map<String, Object> map) {
        instance.setExtraFields(map);
    }
}