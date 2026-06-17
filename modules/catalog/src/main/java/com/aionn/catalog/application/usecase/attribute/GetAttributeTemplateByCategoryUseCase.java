package com.aionn.catalog.application.usecase.attribute;

import com.aionn.catalog.application.dto.attribute.query.GetAttributeTemplateByCategoryQuery;
import com.aionn.catalog.application.dto.attribute.result.AttributeTemplateResult;
import com.aionn.catalog.application.port.in.attribute.GetAttributeTemplateByCategoryInputPort;
import com.aionn.catalog.application.service.AttributeTemplateService;
import com.aionn.catalog.domain.exception.CatalogErrorCode;
import com.aionn.catalog.domain.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAttributeTemplateByCategoryUseCase implements GetAttributeTemplateByCategoryInputPort {

    private final AttributeTemplateService attributeTemplateService;

    @Override
    public AttributeTemplateResult execute(GetAttributeTemplateByCategoryQuery query) {
        return attributeTemplateService.findByCategoryId(query.categoryId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.ATTRIBUTE_TEMPLATE_NOT_FOUND));
    }
}
