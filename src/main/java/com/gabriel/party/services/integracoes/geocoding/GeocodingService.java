package com.gabriel.party.services.integracoes.geocoding;

import com.gabriel.party.dtos.integracoes.CoordenadasDTO;
import org.springframework.stereotype.Service;

@Service
public interface GeocodingService {

    CoordenadasDTO buscarCoordenadas(String logradouro, String cidade, String estado);
}
