package com.gabriel.party.model.prestador.endereco;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    //  usa para calcular o raio:
    private Double latitude;
    private Double longitude;

    public void atribuirCoordenadas(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


}