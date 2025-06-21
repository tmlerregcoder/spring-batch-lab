package org.sistema.banco.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteContaDTO {
    private Long id_cliente;
    private String nome_cliente;
    private String cpf_cliente;
    private String rg_cliente;
    private LocalDate data_nascimento_cliente;
    private LocalDateTime data_atualizacao;
    private Boolean ativo;

    private String agencia_conta;
    private String numero_conta;
    private String tipo_conta;
    private BigDecimal saldo_conta;
}