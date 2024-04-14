package br.org.fundatec.atividade03.service;

import br.org.fundatec.atividade03.controller.response.BrasilApiResponse;
import br.org.fundatec.atividade03.controller.response.CepAbertoResponse;
import br.org.fundatec.atividade03.controller.response.ViaCepResponse;
import br.org.fundatec.atividade03.model.Address;
import br.org.fundatec.atividade03.repository.AddressRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
@Data
@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address findOrCreateByCep(String cep) {
        Address existingAddress = addressRepository.findByCep(cep);
        if (existingAddress != null) {
            return existingAddress;
        }

        RestTemplate restTemplate = new RestTemplate();

        // Consulta no ViaCEP
        String viaCepUrl = "https://viacep.com.br/ws/" + cep + "/json/";
        ViaCepResponse viaCepResponse = restTemplate.getForObject(viaCepUrl, ViaCepResponse.class);
        if (viaCepResponse != null) {
            Address newAddress = createAddressFromResponse(viaCepResponse, "viacep");
            return addressRepository.save(newAddress);
        }

        // Consulta no BrasilAPI
        String brasilApiUrl = "https://brasilapi.com.br/api/cep/v1/" + cep;
        BrasilApiResponse brasilApiResponse = restTemplate.getForObject(brasilApiUrl, BrasilApiResponse.class);
        if (brasilApiResponse != null) {
            Address newAddress = createAddressFromResponse(brasilApiResponse, "brasilapi");
            return addressRepository.save(newAddress);
        }

        // Consulta no CepAberto
        String cepAbertoUrl = "https://www.cepaberto.com/api/v3/cep?cep=" + cep;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token token=21e77485e82bf19d0fba79125e8a75ff");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<CepAbertoResponse> response = restTemplate.exchange(cepAbertoUrl, HttpMethod.GET, entity, CepAbertoResponse.class);
        CepAbertoResponse cepAbertoResponse = response.getBody();
        if (cepAbertoResponse != null) {
            Address newAddress = createAddressFromResponse(cepAbertoResponse, "cepaberto");
            return addressRepository.save(newAddress);
        }
        return null;
    }

    private Address createAddressFromResponse(ViaCepResponse viaCepResponse, String site) {
        Address newAddress = new Address();
        newAddress.setCep(viaCepResponse.getCep());
        newAddress.setStreet(viaCepResponse.getLogradouro());
        newAddress.setCity(viaCepResponse.getLocalidade());
        newAddress.setState(viaCepResponse.getUf());
        newAddress.setSite(site);
        return newAddress;
    }

    private Address createAddressFromResponse(BrasilApiResponse brasilApiResponse, String site) {
        Address newAddress = new Address();
        newAddress.setCep(brasilApiResponse.getCep());
        newAddress.setStreet(brasilApiResponse.getLogradouro());
        newAddress.setCity(brasilApiResponse.getLocalidade());
        newAddress.setState(brasilApiResponse.getUf());
        newAddress.setSite(site);
        return newAddress;
    }

    private Address createAddressFromResponse(CepAbertoResponse cepAbertoResponse, String site) {
        Address newAddress = new Address();
        newAddress.setCep(cepAbertoResponse.getCep());
        newAddress.setStreet(cepAbertoResponse.getLogradouro());
        newAddress.setCity(null); //cepAbertoResponse.getCidade().getNome());
        newAddress.setState(null);  //cepAbertoResponse.getEstado().getSigla());
        newAddress.setSite(site);
        return newAddress;
    }
}

