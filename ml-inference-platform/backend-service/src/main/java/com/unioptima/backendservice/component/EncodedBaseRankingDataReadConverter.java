package com.unioptima.backendservice.component;


import com.unioptima.backendservice.model.EncodedBaseRankingData;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class EncodedBaseRankingDataReadConverter extends AbstractDynamicFieldConverter<EncodedBaseRankingData> {

    @Override
    protected EncodedBaseRankingData createInstance() {
        return new EncodedBaseRankingData();
    }

    @Override
    protected void populateStandardFields(EncodedBaseRankingData instance, org.bson.Document source) {
        instance.setId(source.getString("_id"));
        instance.setIdOSYM(source.getString("idOSYM"));
        instance.setAcademicYear(source.getInteger("academicYear"));
    }

    @Override
    protected void setExtraFields(EncodedBaseRankingData instance, java.util.Map<String, Object> map) {
        instance.setExtraFields(map);
    }
}
