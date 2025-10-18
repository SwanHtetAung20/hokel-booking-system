package com.sha.client_service.service;

import com.sha.client_service.dto.ClientRequestResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    ClientRequestResponseDTO create(ClientRequestResponseDTO dto);

    Optional<ClientRequestResponseDTO> findById(String id);

    List<ClientRequestResponseDTO> findAll();

    ClientRequestResponseDTO update(String id, ClientRequestResponseDTO dto);

    void delete(String id);

}
