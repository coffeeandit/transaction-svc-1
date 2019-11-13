package br.com.cdsoft.transaction.transaction.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "uui")
@ToString
public class TransactionDTO implements Serializable {

    private static final long serialVersionUID = 2806421523585360625L;


    private BigDecimal valor;
    private LocalDateTime data;
    private Conta conta;
    private BeneficiatioDto beneficiario;
    private TipoTransacao tipoTransacao;
    private UUID uui;


}
