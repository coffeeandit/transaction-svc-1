package br.com.cdsoft.transaction.http;

import br.com.cdsoft.transaction.business.TransactionBusiness;
import br.com.cdsoft.transaction.config.NotFoundResponse;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    @Value("${app.intervalTransaction}")
    private int intervalTransaction;

    public TransactionController(final TransactionBusiness transactionBusiness) {
        this.transactionBusiness = transactionBusiness;
    }

    @GetMapping(value = "/transaction", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<List<TransactionDTO>>> queryTransaction(
            @RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {

        return Flux.interval(Duration.ofSeconds(intervalTransaction))
                .map(sequence -> ServerSentEvent.<List<TransactionDTO>>builder()
                        .id(String.valueOf(sequence))
                        .event(SICREDI_TRANSACTION_EVENT)
                        .data(transactionBusiness.queryTransactionFewSeconds(agencia, conta))
                        .build());


    }

    @GetMapping(value = "/transaction/block", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TransactionDTO> queryTransactionBlock(
            @RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {
        return Flux.fromIterable(transactionBusiness.queryTransaction(agencia, conta))
                .limitRate(100).cache(Duration.ofMinutes(5));


    }

    @GetMapping(value = "/transaction/version", produces = MediaType.TEXT_HTML_VALUE)
    public String getVersion() {
        return "6";
    }

    @GetMapping(value = "/transaction/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionDTO> findById(@PathVariable("id") String uuid, @RequestHeader(name = "content-type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType) {
        var item = transactionBusiness.retrieveItem(uuid);
        if (item.isPresent()) {
            return Mono.just(item.get());
        }
        throw new NotFoundResponse("Transação não encontrada");
    }
}