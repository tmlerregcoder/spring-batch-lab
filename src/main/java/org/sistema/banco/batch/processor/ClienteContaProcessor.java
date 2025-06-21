package org.sistema.banco.batch.processor;

import org.sistema.banco.batch.model.Cliente;
import org.sistema.banco.batch.model.ClienteContaDTO;
import org.sistema.banco.batch.model.Conta;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ClienteContaProcessor implements ItemProcessor<ClienteContaDTO, Cliente> {
    @Override
    public Cliente process(ClienteContaDTO item) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setIdClienteOrigem(item.getId_cliente());
        cliente.setNome(item.getNome_cliente());
        cliente.setCpf(item.getCpf_cliente());
        cliente.setRg(item.getRg_cliente());
        cliente.setDataNascimento(item.getData_nascimento_cliente());
        cliente.setDataAtualizacao(item.getData_atualizacao());
        cliente.setAtivo(Optional.ofNullable(item.getAtivo()).orElse(false));

        Conta conta = new Conta();
        conta.setAgencia(item.getAgencia_conta());
        conta.setNumero(item.getNumero_conta());
        conta.setTipo(item.getTipo_conta());
        conta.setSaldo(Optional.ofNullable(item.getSaldo_conta()).orElse(BigDecimal.ZERO));
        conta.setCliente(cliente);

        // A lista 'contas' já é inicializada em Cliente.java
        cliente.getContas().add(conta);

        return cliente;
    }
}