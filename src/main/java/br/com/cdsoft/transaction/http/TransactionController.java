package br.com.cdsoft.transaction.http;

import br.com.cdsoft.transaction.business.TransactionBusiness;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@Slf4j
public class TransactionController {

    private static final int SECONDS = 5;
    private TransactionBusiness transactionBusiness;

    public TransactionController(final TransactionBusiness transactionBusiness) {
        this.transactionBusiness = transactionBusiness;
    }

    @GetMapping(value = "/transaction", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<List<TransactionDTO>> queryTransaction(@RequestHeader(value = "Content-Type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType,
                                                       @RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {


        return Flux.just(transactionBusiness.queryTransaction(agencia, conta))
                .map(items -> {
                    return transactionBusiness.mapToTransactionDTO(items);

                })
                .timeout(Duration.ofSeconds(SECONDS))
                .doFinally(signalType -> {
                    if (log.isDebugEnabled()) {

                        log.debug(String.format("Finally returning : %s", signalType));
                    }

                }).doOnError(throwable -> {
                    log.error(throwable.getMessage());
                });


    }


}