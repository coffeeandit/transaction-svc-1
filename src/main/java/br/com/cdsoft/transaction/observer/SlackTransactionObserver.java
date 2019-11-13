package br.com.cdsoft.transaction.observer;

import br.com.cdsoft.transaction.http.SlackAlertService;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import org.springframework.stereotype.Service;

@Service
public class SlackTransactionObserver implements TransactionObserver<TransactionDTO> {

    private SlackAlertService slackAlertService;

    public SlackTransactionObserver(final SlackAlertService slackAlertService) {
        this.slackAlertService = slackAlertService;
    }

    @Override
    public void doObserver(final TransactionDTO item) {

        slackAlertService.send(item);

    }
}
