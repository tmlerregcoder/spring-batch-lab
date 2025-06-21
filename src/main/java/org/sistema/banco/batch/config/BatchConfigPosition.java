package org.sistema.banco.batch.config;

import org.sistema.banco.batch.converter.StringToLocalDateTimeConverter;
import org.sistema.banco.batch.converter.StringToLocaleDateConverter;
import org.sistema.banco.batch.model.Cliente;
import org.sistema.banco.batch.model.ClienteContaDTO;
import org.sistema.banco.batch.processor.ClienteContaProcessor;
import org.sistema.banco.batch.repository.ClienteRepository;
import org.sistema.banco.batch.writer.ClienteWriterComRepository;
import org.sistema.banco.batch.writer.ClienteWriterEM; // Importar o writer que você está usando (EM ou Repository)
// import org.sistema.banco.batch.writer.ClienteWriterRepository; // Se optar pelo writer com Repository

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory; // Necessário se ClienteWriterEM for usado

@Configuration
@EnableBatchProcessing
public class BatchConfigPosition {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory; // Apenas se usar ClienteWriterEM
    private final ClienteRepository clienteRepository;

    @Autowired
    public BatchConfigPosition(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                               EntityManagerFactory entityManagerFactory, ClienteRepository clienteRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory; // Apenas se usar ClienteWriterEM
        this.clienteRepository = clienteRepository; // Apenas se usar ClienteWriterRepository
    }

    @Bean
    public FlatFileItemReader<ClienteContaDTO> positionalReader() {
        return new FlatFileItemReaderBuilder<ClienteContaDTO>()
                .name("clienteContaPositionalItemReader")
                .resource(new ClassPathResource("data/clientes_contas_fixed.csv"))
                .fixedLength()
                .columns(new Range[]{
                        new Range(1, 10),
                        new Range(11, 60),
                        new Range(61, 75),
                        new Range(76, 90),
                        new Range(91, 100),
                        new Range(101, 120),
                        new Range(121, 125),
                        new Range(126, 130),
                        new Range(131, 140),
                        new Range(141, 160),
                        new Range(161, 175)
                })
                .names("id_cliente", "nome_cliente", "cpf_cliente", "rg_cliente", "data_nascimento_cliente",
                        "data_atualizacao", "ativo",
                        "agencia_conta", "numero_conta", "tipo_conta", "saldo_conta")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<ClienteContaDTO>() {{
                    setTargetType(ClienteContaDTO.class);
                    DefaultConversionService conversionService = new DefaultConversionService();
                    conversionService.addConverter(new StringToLocaleDateConverter());
                    conversionService.addConverter(new StringToLocalDateTimeConverter());
                    this.setConversionService(conversionService);
                }})
                .linesToSkip(1)
                .build();
    }

    // Processor e Writer já existem como beans em outras configurações,
    // mas podem ser referenciados aqui. Se você precisar de instâncias dedicadas
    // para esta configuração, elas precisariam ser definidas aqui ou importadas via @Import.
    // No entanto, para reutilização, podemos simplesmente injetá-los se eles forem @Component.

    // Já temos um processor (ClienteContaProcessor) que é um @Component,
    // então o Spring pode injetá-lo.
    // @Bean
    // public ClienteContaProcessor processor() {
    //     return new ClienteContaProcessor();
    // }

    // Já temos um writer (ClienteWriterEM ou ClienteWriterRepository) que é um @Component,
    // então o Spring pode injetá-lo.
    // @Bean
    // public ClienteWriterEM writer() { // Ou ClienteWriterRepository
    //     return new ClienteWriterEM(entityManagerFactory); // Ou new ClienteWriterRepository(clienteRepository)
    // }

    @Bean
    public Step importClientesContasPositionalStep(FlatFileItemReader<ClienteContaDTO> positionalReader,
                                                   ClienteContaProcessor processor, // Injeta o processor existente
                                                   ClienteWriterComRepository writer) { // Injeta o writer existente (ajuste o tipo se usar ClienteWriterRepository)
        return stepBuilderFactory.get("importClientesContasPositionalStep")
                .<ClienteContaDTO, Cliente>chunk(500)
                .reader(positionalReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importUserPositionalJob(Step importClientesContasPositionalStep) {
        return jobBuilderFactory.get("importUserPositionalJob")
                .flow(importClientesContasPositionalStep)
                .end()
                .build();
    }
}