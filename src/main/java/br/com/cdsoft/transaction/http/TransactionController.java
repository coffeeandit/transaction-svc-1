package br.com.cdsoft.transaction.http;

import br.com.cdsoft.transaction.business.TransactionBusiness;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@Slf4j
public class TransactionController {

    public static final String SICREDI_TRANSACTION_EVENT = "sicredi-transaction-event";
    @Value("${app.timeout}")
    private int timeout;
    @Value("${app.cacheTime}")
    public int cacheTime;
    private TransactionBusiness transactionBusiness;

    public TransactionController(final TransactionBusiness transactionBusiness) {
        this.transactionBusiness = transactionBusiness;
    }

    @GetMapping(value = "/transaction", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<TransactionDTO>>> queryTransaction(
            @RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {

        return Flux.interval(Duration.ofSeconds(6))
                .map(sequence -> ServerSentEvent.<List<TransactionDTO>>builder()
                        .id(String.valueOf(sequence))
                        .event(SICREDI_TRANSACTION_EVENT)
                        .data(transactionBusiness.queryTransactionFewSeconds(agencia, conta))
                        .build());


    }


}