package org.sistema.banco.batch;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@SpringBootApplication
@EnableBatchProcessing
@AllArgsConstructor
public class ProjetoBancoBatchApplication implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(ProjetoBancoBatchApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            log.info("Nenhum parâmetro fornecido. Informe qual job executar. Ex: java -jar seu-aplicativo.jar nomeDoSeuJob");
            return;
        }
        String jobName = args[0];
        try {
            Job job = (Job) applicationContext.getBean(jobName);
            log.info("Executando o job: {}", jobName);
            JobExecution jobExecution = executarJob(job);

            log.info("JobExecution: {}", jobExecution.getStatus());
            while (jobExecution.isRunning()) {
                log.info("Job em execução...");
                Thread.sleep(5000);
            }
            log.info("Job finalizado com status: {}", jobExecution.getStatus());

        } catch (Exception e) {
            log.error("Erro ao executar o job: {}", jobName, e);
        }
    }

    private JobExecution executarJob(Job job) {
        try {
            Map<String, JobParameter> maps = new HashMap<>();
            maps.put("time", new JobParameter(System.currentTimeMillis()));

            JobParameters parameters = new JobParameters(maps);
            return jobLauncher.run(job, parameters);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar o job: " + job.getName(), e);
        }
    }
}