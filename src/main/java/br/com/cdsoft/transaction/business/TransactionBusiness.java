package br.com.cdsoft.transaction.business;

import br.com.cdsoft.transaction.business.dsl.DynamoTable;
import br.com.cdsoft.transaction.business.dsl.InsertableItem;
import br.com.cdsoft.transaction.transaction.dto.BeneficiatioDto;
import br.com.cdsoft.transaction.transaction.dto.Conta;
import br.com.cdsoft.transaction.transaction.dto.TipoTransacao;
import br.com.cdsoft.transaction.transaction.dto.TransactionDTO;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Service
@Transactional
@Slf4j
public class TransactionBusiness implements DynamoTable, InsertableItem<TransactionDTO> {


    public static final String TRANSACAO = "transacao";
    public static final String UUI = "uui";
    public static final String VALOR = "valor";
    public static final String TIPOTRANSACAO = "tipotransacao";
    public static final String CONTA = "conta";
    public static final String AGENCIA = "agencia";
    public static final String AGENCIA_BENEFICIARIO = "agenciaBeneficiario";
    public static final String CONTA_BENEFICIARIO = "contaBeneficiario";
    public static final String CPF = "cpf";
    public static final String NOME_FAVORECIDO = "nomeFavorecido";
    public static final String BANCO_FAVORECIDO = "bancoFavorecido";

    public static final String DT_TRANSACTION = "dtTransaction";
    public static final String TRANSACOES_SEMANA = "agencia = :agencia AND  conta = :conta AND dtTransaction BETWEEN :dataInicial and :dataFinal";
    public static final int AMOUNT_TO_ADD = -1;

    private Table transacao;
    private DynamoDB dynamoDB;

    private Function<Item, TransactionDTO> itemTransactionDTOFunction = item -> {
        var transacao = new TransactionDTO();
        transacao.setValor(item.getNumber(TransactionBusiness.VALOR));
        transacao.setData(LocalDateTime.parse(item.getString(DT_TRANSACTION)));
        transacao.setTipoTransacao(TipoTransacao.valueOf(item.getString(TIPOTRANSACAO)));
        transacao.setUui(UUID.fromString(item.getString(UUI)));
        var conta = new Conta();
        conta.setCodigoConta(item.getLong(CONTA));
        conta.setCodigoAgencia(item.getLong(AGENCIA));
        transacao.setConta(conta);
        var beneficiario = new BeneficiatioDto();
        beneficiario.setAgencia(item.getString(AGENCIA_BENEFICIARIO));
        beneficiario.setCodigoBanco(item.getLong(BANCO_FAVORECIDO));
        beneficiario.setConta(item.getString(CONTA_BENEFICIARIO));
        beneficiario.setCPF(item.getLong(CPF));
        beneficiario.setNomeFavorecido(item.getString(NOME_FAVORECIDO));
        transacao.setBeneficiario(beneficiario);

        return transacao;
    };


    public TransactionBusiness(final DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    @NewSpan
    public Item putItem(@SpanTag(key = "transaction") final TransactionDTO transaction) {

        log.info(String.format("Inserindo a transação %s", transaction));
        var table = getTable();
        var conta = transaction.getConta();
        var beneficiario = transaction.getBeneficiario();
        var item = getItem(transaction, conta, beneficiario);
        var putItemOutcome = table.putItem(item);
        var putItemOutcomeItem = putItemOutcome.getItem();
        return Objects.nonNull(putItemOutcome.getItem()) ? putItemOutcomeItem : item;

    }

    @NewSpan
    public DeleteItemOutcome removeItem(@SpanTag(key = "removeTransaction") final TransactionDTO transaction) {
        var table = getTable();


        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey(UUI, transaction.getUui().toString()));

        return table.deleteItem(deleteItemSpec);


    }

    public ItemCollection<ScanOutcome> queryTransaction(final Long agencia, final Long conta) {


        var now = LocalDateTime.now();
        var lastWeek = now.plus(AMOUNT_TO_ADD, ChronoUnit.WEEKS);

        var expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":".concat(AGENCIA), agencia);
        expressionAttributeValues.put(":".concat(CONTA), conta);
        expressionAttributeValues.put(":dataInicial", lastWeek.toString());
        expressionAttributeValues.put(":dataFinal", now.toString());


        return getTable().scan(TRANSACOES_SEMANA,
                null,
                expressionAttributeValues);

    }

    public List<TransactionDTO> mapToTransactionDTO(final ItemCollection<ScanOutcome> scanOutcome) {

        var transactions = new ArrayList<TransactionDTO>();

        scanOutcome.forEach(item -> {
            transactions.add(itemTransactionDTOFunction.apply(item));
        });
        transactions.sort(Comparator.comparing(TransactionDTO::getData));
        return transactions;
    }

    private Item getItem(TransactionDTO transaction, Conta conta, BeneficiatioDto beneficiario) {
        var data = transaction.getData();
        if (Objects.isNull(data)) {
            transaction.setData(LocalDateTime.now());
        }
        return new Item().withPrimaryKey(UUI, transaction.getUui().toString())
                .withNumber(VALOR, transaction.getValor())
                .withString(TIPOTRANSACAO, transaction.getTipoTransacao().name())
                .withLong(CONTA, conta.getCodigoConta())
                .withLong(AGENCIA, conta.getCodigoAgencia())
                .withString(AGENCIA_BENEFICIARIO, beneficiario.getAgencia())
                .withString(CONTA_BENEFICIARIO, beneficiario.getConta())
                .withLong(CPF, beneficiario.getCPF())
                .withString(NOME_FAVORECIDO, beneficiario.getNomeFavorecido())
                .withLong(BANCO_FAVORECIDO, beneficiario.getCodigoBanco())
                .withString(DT_TRANSACTION, data.toString());

    }

    public Table getTable() {
        if (Objects.isNull(transacao)) {
            transacao = dynamoDB.getTable(TRANSACAO);
        }
        return transacao;
    }

}
