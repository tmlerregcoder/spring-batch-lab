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
import java.util.Locale;
import java.util.Random;

public class CsvDelimitedGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Random RANDOM = new Random();

    public static void generateCsvFile(String filename, int numLines, double inactivePercentage) {
        Path resourcesPath = Paths.get("src", "main", "resources", "data");
        try {
            Files.createDirectories(resourcesPath);
        } catch (IOException e) {
            System.err.println("Erro ao criar o diretório: " + resourcesPath + " - " + e.getMessage());
            return;
        }

        Path outputPath = resourcesPath.resolve(filename);

        String[] headers = {
                "id_cliente", "nome_cliente", "cpf_cliente", "rg_cliente", "data_nascimento_cliente",
                "data_atualizacao", "ativo",
                "agencia_conta", "numero_conta", "tipo_conta", "saldo_conta"
        };

        System.out.println("--- Gerando arquivo CSV com Java: '" + outputPath + "' com " + numLines + " linhas ---");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            writer.write(String.join(",", headers));
            writer.newLine();

            int numUniqueClients = numLines / 2;
            if (numUniqueClients == 0) numUniqueClients = 1;
            ClientData[] clientCache = new ClientData[numUniqueClients];
            for (int i = 0; i < numUniqueClients; i++) {
                clientCache[i] = generateSingleClientData(i + 1, inactivePercentage);
            }

            String[] accountTypes = {"Corrente", "Poupanca", "Investimento"};

            for (int i = 0; i < numLines; i++) {
                ClientData clientInfo = clientCache[RANDOM.nextInt(numUniqueClients)];

                String agencia = String.format("%04d", RANDOM.nextInt(9999) + 1);
                String numeroConta = String.format("%05d-%d", RANDOM.nextInt(90000) + 10000, RANDOM.nextInt(10));
                String tipoConta = accountTypes[RANDOM.nextInt(accountTypes.length)];
                double saldo = 10.0 + (100000.0 - 10.0) * RANDOM.nextDouble();

                // ***** ÚNICA MUDANÇA AQUI: Force o Locale.US para garantir o ponto como separador decimal *****
                String formattedSaldo = String.format(Locale.US, "%.2f", saldo);

                String[] row = {
                        String.valueOf(clientInfo.id),
                        clientInfo.nome,
                        clientInfo.cpf,
                        clientInfo.rg,
                        clientInfo.dataNascimento.format(DATE_FORMATTER),
                        clientInfo.dataAtualizacao.format(DATETIME_FORMATTER),
                        String.valueOf(clientInfo.ativo),
                        agencia,
                        numeroConta,
                        tipoConta,
                        formattedSaldo // Usar o saldo formatado com ponto
                };
                writer.write(String.join(",", row));
                writer.newLine();

                if ((i + 1) % 50000 == 0) {
                    System.out.println("Geradas " + (i + 1) + " linhas...");
                }
            }
            System.out.println("Arquivo '" + filename + "' com " + numLines + " linhas gerado com sucesso!");

        } catch (IOException e) {
            System.err.println("Erro ao gerar o CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ClientData generateSingleClientData(long id, double inactivePercentage) {
        String name = "Cliente Java " + id;
        String cpf = generateCpf();
        String rg = generateRg();
        LocalDate dob = LocalDate.of(1950 + RANDOM.nextInt(51), RANDOM.nextInt(12) + 1, RANDOM.nextInt(28) + 1);
        LocalDateTime updateDate = LocalDateTime.now().minusDays(RANDOM.nextInt(366));
        Boolean active = RANDOM.nextDouble() > inactivePercentage;

        return new ClientData(id, name, cpf, rg, dob, updateDate, active);
    }

    private static String generateCpf() {
        StringBuilder cpf = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            cpf.append(RANDOM.nextInt(10));
        }
        return cpf.toString();
    }

    private static String generateRg() {
        StringBuilder rg = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            rg.append(RANDOM.nextInt(10));
        }
        return rg.toString();
    }

    static class ClientData {
        long id;
        String nome;
        String cpf;
        String rg;
        LocalDate dataNascimento;
        LocalDateTime dataAtualizacao;
        Boolean ativo;

        ClientData(long id, String nome, String cpf, String rg, LocalDate dataNascimento, LocalDateTime dataAtualizacao, Boolean ativo) {
            this.id = id;
            this.nome = nome;
            this.cpf = cpf;
            this.rg = rg;
            this.dataNascimento = dataNascimento;
            this.dataAtualizacao = dataAtualizacao;
            this.ativo = ativo;
        }
    }

    public static void main(String[] args) {
        generateCsvFile("clientes_contas.csv", 700000, 0.30);
    }
}