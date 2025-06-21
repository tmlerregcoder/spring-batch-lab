package org.sistema.banco.batch.config;

import org.sistema.banco.batch.converter.StringToLocalDateTimeConverter;
import org.sistema.banco.batch.converter.StringToLocaleDateConverter;
import org.sistema.banco.batch.model.Cliente;
import org.sistema.banco.batch.model.ClienteContaDTO;
import org.sistema.banco.batch.processor.ClienteContaProcessor;
import org.sistema.banco.batch.writer.ClienteWriterEM;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // Injeção de dependências para construir Jobs e Steps
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory; // Necessário se usar ClienteWriterEM com EntityManager

    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory; // Injete se usar EntityManager
    }

    @Bean
    public FlatFileItemReader<ClienteContaDTO> reader() {
        //Linhas comentadas servem para usar a leitura do CSV em outro caminho que não seja no path do projeto
        //String csvFilePath = "C:/csv"
        return new FlatFileItemReaderBuilder<ClienteContaDTO>()
                .name("clienteContaItemReader")
                .resource(new ClassPathResource("data/clientes_contas.csv"))
                // .resource(new FileSystemResource(csvFilePath))
                .delimited()
                .names(new String[]{ // Nomes das colunas no seu CSV
                        "id_cliente", "nome_cliente", "cpf_cliente", "rg_cliente", "data_nascimento_cliente",
                        "data_atualizacao", "ativo",
                        "agencia_conta", "numero_conta", "tipo_conta", "saldo_conta"
                })
                .fieldSetMapper(new BeanWrapperFieldSetMapper<ClienteContaDTO>() {{
                    setTargetType(ClienteContaDTO.class);
                    DefaultConversionService conversionService = new DefaultConversionService();
                    conversionService.addConverter(new StringToLocaleDateConverter());
                    conversionService.addConverter(new StringToLocalDateTimeConverter());
                   this.setConversionService(conversionService);
                }})
                .linesToSkip(1) // Ignora a linha de cabeçalho
                .build();
    }
    @Bean
    public ClienteContaProcessor processor() {
        return new ClienteContaProcessor();
    }

    // ItemWriter como um Bean (use a versão que você escolheu: com EntityManager ou Repository)
    @Bean
    public ClienteWriterEM writer() {
        // Se estiver usando a versão com EntityManagerFactory:
        return new ClienteWriterEM(entityManagerFactory);

        // OU, se estiver usando a versão com ClienteRepository (RECOMENDADO):
        // Precisa injetar ClienteRepository aqui e passá-lo para o construtor do ClienteWriterEM
        // @Autowired
        // private ClienteRepository clienteRepository;
        // return new ClienteWriterEM(clienteRepository);
    }

    // Define o Step que combina Reader, Processor e Writer
    @Bean
    public Step importClientesContasStep(FlatFileItemReader<ClienteContaDTO> reader,
                                         ClienteContaProcessor processor,
                                         ClienteWriterEM writer) {
        return stepBuilderFactory.get("importClientesContasStep")
                .<ClienteContaDTO, Cliente>chunk(500) // Processa 500 itens por chunk
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Define o Job que executa o Step
    @Bean
    public Job importUserJob(Step importClientesContasStep) {
        return jobBuilderFactory.get("importUserJob")
                .flow(importClientesContasStep) // O Job executa este Step
                .end()
                .build();
    }
}