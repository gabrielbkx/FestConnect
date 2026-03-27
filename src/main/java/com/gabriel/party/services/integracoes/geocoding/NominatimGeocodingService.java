package com.gabriel.party.services.integracoes.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gabriel.party.dtos.integracoes.CoordenadasDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;

    public NominatimGeocodingService() {
        this.restClient = RestClient.create();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResponse(String lat, String lon) {}

    @Override
    public CoordenadasDTO buscarCoordenadas(String logradouro, String cidade, String estado) {

        String buscaLivre = logradouro + ", " + cidade + ", " + estado + ", Brazil";

        try {

            NominatimResponse[] respostas = restClient.get()
                    .uri("https://nominatim.openstreetmap.org/search?q={query}&format=json", buscaLivre)
                    .header("User-Agent", "PartyApp/1.0 (oliveiraferriera97@gmail.com)")
                    .retrieve()
                    .body(NominatimResponse[].class);


            if (respostas != null && respostas.length > 0) {
                NominatimResponse melhorResultado = respostas[0];

                Double latitude = Double.parseDouble(melhorResultado.lat());
                Double longitude = Double.parseDouble(melhorResultado.lon());

                return new CoordenadasDTO(latitude, longitude);
            }

        } catch (Exception e) {

            throw new RuntimeException("Erro de comunicação com a API de mapas: " + e.getMessage(), e);
        }

        return null;
    }
}
