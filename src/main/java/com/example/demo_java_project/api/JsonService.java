package com.example.demo_java_project.api;

import com.example.demo_java_project.api.dto.ResourceDto;
import com.example.demo_java_project.model.Resource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonService {

    /** Shared, correctly-configured mapper (handles LocalTime, pretty-prints, ignores extra fields). */
    public static ObjectMapper newMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private final ObjectMapper mapper = newMapper();

    public void exportResources(List<Resource> resources, File file) throws IOException {
        List<ResourceDto> dtos = new ArrayList<>();
        for (Resource r : resources) dtos.add(toDto(r));
        mapper.writeValue(file, dtos);
    }

    public List<ResourceDto> importResources(File file) throws IOException {
        ResourceDto[] array = mapper.readValue(file, ResourceDto[].class);
        List<ResourceDto> list = new ArrayList<>();
        for (ResourceDto d : array) list.add(d);
        return list;
    }

    public ResourceDto toDto(Resource r) {
        ResourceDto dto = new ResourceDto();
        dto.setName(r.getName());
        dto.setType(r.getType());
        dto.setLocation(r.getLocation());
        dto.setCapacity(r.getCapacity());
        dto.setOpenTime(r.getOpenTime());
        dto.setCloseTime(r.getCloseTime());
        dto.setDescription(r.getDescription());
        dto.setAmenities(r.getAmenities());
        dto.setActive(r.isActive());
        return dto;
    }

    public Resource fromDto(ResourceDto dto) {
        return new Resource(0, dto.getName(), dto.getType(), dto.getLocation(), dto.getCapacity(),
                dto.getOpenTime(), dto.getCloseTime(), dto.getDescription(), dto.getAmenities(),
                dto.isActive());
    }
}