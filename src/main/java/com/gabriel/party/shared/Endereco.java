package com.gabriel.party.shared;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public String estado;

    //  usa para calcular o raio:
    public Double latitude;
    public Double longitude;

    public void atribuirCoordenadas(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


}