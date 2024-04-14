package br.org.fundatec.atividade03.controller.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class CepAbertoResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    //private Cidade cidade;
    //private Estado estado;

}
