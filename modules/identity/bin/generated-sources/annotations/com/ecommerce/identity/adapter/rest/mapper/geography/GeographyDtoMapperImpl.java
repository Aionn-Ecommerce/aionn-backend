package com.ecommerce.identity.adapter.rest.mapper.geography;

import com.ecommerce.identity.adapter.rest.dto.geography.GeographyResponse;
import com.ecommerce.identity.application.dto.geography.result.GeographyResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class GeographyDtoMapperImpl implements GeographyDtoMapper {

    @Override
    public GeographyResponse toResponse(GeographyResult result) {
        if ( result == null ) {
            return null;
        }

        String code = null;
        String name = null;
        String nameEn = null;

        code = result.code();
        name = result.name();
        nameEn = result.nameEn();

        GeographyResponse geographyResponse = new GeographyResponse( code, name, nameEn );

        return geographyResponse;
    }

    @Override
    public List<GeographyResponse> toResponses(List<GeographyResult> results) {
        if ( results == null ) {
            return null;
        }

        List<GeographyResponse> list = new ArrayList<GeographyResponse>( results.size() );
        for ( GeographyResult geographyResult : results ) {
            list.add( toResponse( geographyResult ) );
        }

        return list;
    }
}
