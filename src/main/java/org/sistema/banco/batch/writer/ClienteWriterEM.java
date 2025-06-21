package org.sistema.banco.batch.writer;

import org.sistema.banco.batch.model.Cliente;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;

@Component
public class ClienteWriterEM implements ItemWriter<Cliente> {

    private final EntityManagerFactory entityManagerFactory;

    @Autowired // Injeta o EntityManagerFactory
    public ClienteWriterEM(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void write(List<? extends Cliente> items) throws Exception {
        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin(); // Inicia uma transação

            for (Cliente cliente : items) {
                // Se o cliente já existir (ex: pelo CPF ou id_cliente_origem), você pode querer atualizá-lo
                // Para este exemplo, vamos assumir que estamos inserindo novos registros.
                entityManager.persist(cliente); // Persiste o cliente e suas contas associadas (devido a CascadeType.ALL)
            }

            transaction.commit(); // Confirma a transação
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback(); // Em caso de erro, desfaz a transação
            }
            throw new RuntimeException("Falha ao escrever clientes no banco de dados.", e);
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close(); // Fecha o EntityManager
            }
        }
    }
}