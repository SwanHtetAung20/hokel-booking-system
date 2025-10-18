package com.sha.client_service.controller;

import com.sha.client_service.dto.ClientRequestResponseDTO;
import com.sha.client_service.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientRequestResponseDTO> create(@Valid @RequestBody ClientRequestResponseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ClientRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(clientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientRequestResponseDTO> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(clientService.findById(id).orElse(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientRequestResponseDTO> update(@PathVariable("id") String id, @Valid @RequestBody ClientRequestResponseDTO dto) {
        return ResponseEntity.ok(clientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
