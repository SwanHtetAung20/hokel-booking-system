package com.sha.client_service.service.impl;

import com.sha.client_service.domain.Client;
import com.sha.client_service.dto.ClientRequestResponseDTO;
import com.sha.client_service.repository.ClientRepository;
import com.sha.client_service.service.ClientService;
import com.sha.client_service.util.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper clientMapper;


    @Override
    public ClientRequestResponseDTO create(ClientRequestResponseDTO dto) {
        Client client = clientMapper.toEntity(dto);
        client = repository.save(client);
        log.info("Client created: {}", client);
        return clientMapper.toDto(client);
    }
}
