package br.com.cdsoft.transaction.http;

import br.com.cdsoft.transaction.transaction.dto.SlackMessage;
import br.com.cdsoft.transaction.transaction.dto.TemplateSlack;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class SlackAlertService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${alert.url}")
    private String url;
    @Value("${alert.imagem}")
    private String imagem;
    @Value("${alert.imagem2}")
    private String imagem2;
    @Value("${realtime-stream.message}")
    private String message;
    @Value("${realtime-stream.author}")
    private String author;
    @Value("${transaction.riskValue}")
    BigDecimal riskValue;


    private static final Logger LOGGER = LoggerFactory.getLogger(SlackAlertService.class);

    @NewSpan
    public boolean send(final TransactionDTO transactionDTO) {
        try {
            if (Objects.nonNull(transactionDTO.getTipoTransacao())) {
                var urlDecoded = Base64.getDecoder().decode(url.getBytes());
                var post = template(transactionDTO);
                LOGGER.info("Sending POST Message.: " + post);
                var requestBody =
                        RequestBody.create(MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE), post);
                var client = new OkHttpClient();
                var request = new Request.Builder()
                        .url(new String(urlDecoded))
                        .post(requestBody)
                        .build();

                var response = client.newCall(request).execute();
                return HttpStatus.OK.value() == response.code();
            }
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
        }
        return false;


    }

    private String template(final TransactionDTO transactionDTO) throws JsonProcessingException {

        var templateSlack = new TemplateSlack();
        templateSlack.setText(message);
        addField(templateSlack, transactionDTO);
        return objectMapper.writeValueAsString(templateSlack);

    }

    private void addField(final TemplateSlack templateSlack, final TransactionDTO transactionDTO) {
        final List<SlackMessage> attachments = new ArrayList<>();
        final var messageDTO = new SlackMessage();
        var message = "%s no valor de R$  %s da conta %d, agência %d foi creditada para %s CPF %d na conta %s da " +
                "agência" +
                " %s no " +
                "banco %d";
        messageDTO.setAuthor(author);
        messageDTO.setTitle("Notificação de Transação.");
        messageDTO.setAuthorIcon(imagem);
        messageDTO.setImageUrl(imagem);

        if (transactionDTO.getValor().compareTo(riskValue) > 0) {
            messageDTO.setAuthorIcon(imagem2);
            messageDTO.setImageUrl(imagem2);


        }
        messageDTO.addField("Detalhes:", String.format(message, transactionDTO.getTipoTransacao().toString(),
                transactionDTO.getValor().toPlainString(),
                transactionDTO.getConta().getCodigoConta().longValue(),
                transactionDTO.getConta().getCodigoAgencia().longValue(),
                transactionDTO.getBeneficiario().getNomeFavorecido(),
                transactionDTO.getBeneficiario().getCPF().longValue(),
                transactionDTO.getBeneficiario().getConta(), transactionDTO.getBeneficiario().getAgencia(),
                transactionDTO.getBeneficiario().getCodigoBanco()));
        attachments.add(messageDTO);
        templateSlack.setAttachments(attachments);


    }
}
