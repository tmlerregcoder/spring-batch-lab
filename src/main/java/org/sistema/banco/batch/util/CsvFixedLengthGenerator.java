package org.sistema.banco.batch.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CsvFixedLengthGenerator {
    private static final int NUM_RECORDS = 700000;
    // O caminho do arquivo para ser gerado dentro de src/main/resources/data
    private static final String FILE_NAME = "src/main/resources/data/clientes_contas_fixed.csv";

    public static void main(String[] args) {
        // Garante que o diretório 'data' dentro de 'src/main/resources' exista
        Path directoryPath = Paths.get("src/main/resources/data");
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            System.err.println("Erro ao criar o diretório: " + directoryPath + " - " + e.getMessage());
            e.printStackTrace();
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Gera 10 caracteres
        // O reader espera 20 caracteres para data_atualizacao
        // Garantir que o formato da data e hora preencha exatamente 20 caracteres.
        // O padrão "yyyy-MM-dd HH:mm:ss" gera 19 caracteres. Adicionamos um espaço no final.
        DateTimeFormatter dateTimeFormatter20Chars = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss "); // 20 chars com espaço no final

        Random random = new Random();

        System.out.println("Gerando arquivo CSV de tamanho fixo em: " + FILE_NAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // Escrever cabeçalho com os tamanhos CORRETOS para alinhamento visual
            writer.write(String.format("%-10s%-50s%-15s%-15s%-10s%-20s%-5s%-5s%-10s%-20s%-15s",
                    "IDCLIENTE", "NOMECLIENTE", "CPFCLIENTE", "RGCLIENTE", "DTNASCIMENTO",
                    "DTATUALIZACAO", "ATIVO", "AGCONTA", "NUMCONTA", "TIPOCONTA", "SALDOCONTA"));
            writer.newLine();

            for (int i = 1; i <= NUM_RECORDS; i++) {
                // id_cliente (Range 1,10 - 10 caracteres)
                String idCliente = String.format("%010d", i);

                // nome_cliente (Range 11,60 - 50 caracteres)
                // Preenchimento com espaços à direita
                String nomeCliente = String.format("%-50s", "Cliente Teste " + i);

                // cpf_cliente (Range 61,75 - 15 caracteres)
                // Gerar um número que tenha no máximo 15 dígitos e preencher com zeros à esquerda.
                // Usamos Math.abs para garantir que não seja negativo e % 10^15 para limitar o tamanho.
                String cpfCliente = String.format("%015d", Math.abs(random.nextLong()) % 1000000000000000L);

                // rg_cliente (Range 76,90 - 15 caracteres)
                // Gerar um número que tenha no máximo 15 dígitos e preencher com zeros à esquerda.
                String rgCliente = String.format("%015d", Math.abs(random.nextLong()) % 1000000000000000L);

                // data_nascimento_cliente (Range 91,100 - 10 caracteres) - Formato YYYY-MM-DD
                LocalDate dataNascimento = LocalDate.of(1950 + random.nextInt(50), random.nextInt(12) + 1, random.nextInt(28) + 1);
                String dataNascimentoStr = dataNascimento.format(dateFormatter);

                // data_atualizacao (Range 101,120 - 20 caracteres) - Formato YYYY-MM-DD HH:MM:SS
                LocalDateTime dataAtualizacao = LocalDateTime.now().minusMinutes(random.nextInt(10000)).minusSeconds(random.nextInt(60));
                String dataAtualizacaoStr = dataAtualizacao.format(dateTimeFormatter20Chars); // Usar o formatador de 20 chars

                // ativo (Range 121,125 - 5 caracteres) - "true " ou "false"
                String ativo = random.nextBoolean() ? "true " : "false";

                // agencia_conta (Range 126,130 - 5 caracteres)
                String agenciaConta = String.format("%05d", 1000 + random.nextInt(9000));

                // numero_conta (Range 131,140 - 10 caracteres)
                // Gerar um número e formatá-lo para 10 caracteres, preenchendo à esquerda se necessário
                // Exemplo: "000000001-0" ou "0000000010"
                String numeroContaRaw = String.format("%d-%d", random.nextInt(999999999) + 1, random.nextInt(10));
                String numeroConta = String.format("%-10s", numeroContaRaw).substring(0, 10); // Garante 10 chars, truncando se passar

                // tipo_conta (Range 141,160 - 20 caracteres)
                String tipoConta;
                int tipo = random.nextInt(3);
                if (tipo == 0) tipoConta = "Corrente";
                else if (tipo == 1) tipoConta = "Poupanca";
                else tipoConta = "Investimento";
                tipoConta = String.format("%-20s", tipoConta); // Preenche com espaços para garantir 20 chars

                // saldo_conta (Range 161,175 - 15 caracteres)
                // Formato com 2 casas decimais, ponto como separador, preenchido com zeros à esquerda.
                double saldo = 100.00 + random.nextDouble() * 9999999.99; // Aumentei o range do saldo para usar mais casas inteiras.
                String saldoConta = String.format("%015.2f", saldo).replace(',', '.');

                // Concatenação final dos campos para formar a linha
                writer.write(
                        idCliente +
                                nomeCliente +
                                cpfCliente +
                                rgCliente +
                                dataNascimentoStr +
                                dataAtualizacaoStr +
                                ativo +
                                agenciaConta +
                                numeroConta +
                                tipoConta +
                                saldoConta
                );
                writer.newLine();
            }
            System.out.println("Arquivo CSV gerado com sucesso: " + NUM_RECORDS + " linhas.");

        } catch (IOException e) {
            System.err.println("Erro ao gerar o arquivo CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}