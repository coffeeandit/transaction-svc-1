package br.com.cdsoft.transaction.transaction.kafka;


import br.com.cdsoft.transaction.business.TransactionBusiness;
import br.com.cdsoft.transaction.observer.TransactionObserverService;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TransactionMessageReader {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionBusiness transactionBusiness;

    @Autowired
    private TransactionObserverService transactionObserverService;


    @KafkaListener(topics = "${app.topic}")
    public void onConsume(final String message) {

        CompletableFuture.supplyAsync(() -> {
            try {
                var transaction = getTransaction(message);
                transactionBusiness.putItem(transaction);

                return transaction;
            } catch (IOException exception) {
                log.error(exception.getMessage(), exception);
                throw new RuntimeException(exception);
            }
        }).whenCompleteAsync((item, throwable) -> {
            if (Objects.nonNull(throwable)) {
                log.error(throwable.getMessage(), throwable);
            } else {
                doneTransaction(item);
            }

        });


    }

    @KafkaListener(topics = "${app.returnTopic}")
    public void onConsumeExtorno(final String message) {
        try {
            var transaction = getTransaction(message);
            log.info(String.format("Solicitação de remoção de transação %s", transaction));
            transactionBusiness.removeItem(transaction);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @ContinueSpan
    public void doneTransaction(final TransactionDTO transaction) {
        transactionObserverService.notification(transaction);

    }

    private TransactionDTO getTransaction(String message) throws IOException {
        return objectMapper.readValue(message, TransactionDTO.class);
    }
}
