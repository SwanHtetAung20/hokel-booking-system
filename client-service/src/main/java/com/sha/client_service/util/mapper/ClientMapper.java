package com.sha.client_service.util.mapper;

import com.sha.client_service.domain.Client;
import com.sha.client_service.dto.ClientRequestResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

//    @Mapping(target = "id", source = "id", qualifiedByName = "stringToUuid")
    Client toEntity(ClientRequestResponseDTO dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    ClientRequestResponseDTO toDto(Client client);

    default UUID stringToUuid(String id) {
        return id == null || id.isEmpty() ? null : UUID.fromString(id);
    }

    default String uuidToString(UUID id) {
        return id == null ? null : id.toString();
    }
}
