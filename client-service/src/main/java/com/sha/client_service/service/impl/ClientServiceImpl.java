package com.sha.client_service.service.impl;

import com.sha.client_service.domain.Client;
import com.sha.client_service.dto.ClientRequestResponseDTO;
import com.sha.client_service.exception.EmailAlreadyExistException;
import com.sha.client_service.grpc.BillingServiceGrpcClient;
import com.sha.client_service.kafka.KafkaProducer;
import com.sha.client_service.repository.ClientRepository;
import com.sha.client_service.service.ClientService;
import com.sha.client_service.util.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper clientMapper;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;


    @Override
    public ClientRequestResponseDTO create(ClientRequestResponseDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            log.warn("Client with email {} already exists", dto.getEmail());
            throw new EmailAlreadyExistException("Client with email " + dto.getEmail() + " already exists");
        }
        Client client = clientMapper.toEntity(dto);
        client = repository.save(client);
        this.billingServiceGrpcClient.createBillingAccount(
                client.getId().toString(),
                client.getName(),
                client.getEmail()
        );
        kafkaProducer.sendEvent(client);
        log.info("Client created: {}", client);
        return clientMapper.toDto(client);
    }

    @Override
    public Optional<ClientRequestResponseDTO> findById(String id) {
        return repository.findById(UUID.fromString(id)).map(clientMapper::toDto);
    }

    @Override
    public List<ClientRequestResponseDTO> findAll() {
        return repository.findAll().stream().map(clientMapper::toDto).toList();
    }

    @Override
    public ClientRequestResponseDTO update(String id, ClientRequestResponseDTO dto) {
        Optional<Client> existingClientOpt = repository.findById(UUID.fromString(id));
        if (existingClientOpt.isPresent()) {
            if (repository.existsByEmail(dto.getEmail())) {
                log.warn("Client with email {} already exists", dto.getEmail());
                throw new EmailAlreadyExistException("Client with email " + dto.getEmail() + " already exists");
            }
            Client existingClient = existingClientOpt.get();
            Client updatedClient = clientMapper.toEntity(dto);
            updatedClient.setId(existingClient.getId());
            updatedClient = repository.save(updatedClient);
            log.info("Client updated: {}", updatedClient);
            return clientMapper.toDto(updatedClient);
        } else {
            log.warn("Client with id {} not found for update", id);
            throw new IllegalStateException("Client with id " + id + " not found");
        }
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
        log.info("Client with id {} deleted", id);
    }
}
