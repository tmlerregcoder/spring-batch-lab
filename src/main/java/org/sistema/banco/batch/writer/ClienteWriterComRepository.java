package org.sistema.banco.batch.writer;

import org.sistema.banco.batch.model.Cliente;
import org.sistema.banco.batch.repository.ClienteRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClienteWriterComRepository implements ItemWriter<Cliente> {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteWriterComRepository(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void write(List<? extends Cliente> items) throws Exception {
        clienteRepository.saveAll(items);
    }
}