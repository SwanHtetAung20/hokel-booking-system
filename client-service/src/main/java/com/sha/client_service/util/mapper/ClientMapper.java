package com.sha.client_service.util.mapper;

import com.sha.client_service.domain.Client;
import com.sha.client_service.dto.ClientRequestResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.UUID;

import static com.sha.client_service.util.DateTimeUtils.DEFAULT_DATE_FORMATTER;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "dateOfBirth", expression = "java(parseDateOfBirth(dto.getDateOfBirth()))")
    Client toEntity(ClientRequestResponseDTO dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    ClientRequestResponseDTO toDto(Client client);

    @Named("uuidToString")
    default String uuidToString(UUID id) {
        return id == null ? null : id.toString();
    }

    public default LocalDate parseDateOfBirth(String dateOfBirth) {
        return LocalDate.parse(dateOfBirth, DEFAULT_DATE_FORMATTER);
    }
}
