package br.org.fundatec.atividade03.service;

import br.org.fundatec.atividade03.controller.response.BrasilApiResponse;
import br.org.fundatec.atividade03.controller.response.CepAbertoResponse;
import br.org.fundatec.atividade03.controller.response.ViaCepResponse;
import br.org.fundatec.atividade03.model.Address;
import br.org.fundatec.atividade03.repository.AddressRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.scheduling.annotation.Async;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Data
@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Async
    public CompletableFuture<Address> findOrCreateByCep(String cep) {
        Address existingAddress = addressRepository.findByCep(cep);
        if (existingAddress != null) {
            return CompletableFuture.completedFuture(existingAddress);
        }

        RestTemplate restTemplate = new RestTemplate();

        CompletableFuture<ViaCepResponse> viaCepFuture = CompletableFuture.supplyAsync(() -> {
            String viaCepUrl = "https://viacep.com.br/ws/" + cep + "/json/";
            return restTemplate.getForObject(viaCepUrl, ViaCepResponse.class);
        });

        CompletableFuture<BrasilApiResponse> brasilApiFuture = CompletableFuture.supplyAsync(() -> {
            String brasilApiUrl = "https://brasilapi.com.br/api/cep/v1/" + cep;
            return restTemplate.getForObject(brasilApiUrl, BrasilApiResponse.class);
        });

        CompletableFuture<CepAbertoResponse> cepAbertoFuture = CompletableFuture.supplyAsync(() -> {
            String cepAbertoUrl = "https://www.cepaberto.com/api/v3/cep?cep=" + cep;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token token=21e77485e82bf19d0fba79125e8a75ff");
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            ResponseEntity<CepAbertoResponse> response = restTemplate.exchange(cepAbertoUrl, HttpMethod.GET, entity, CepAbertoResponse.class);
            return response.getBody();
        });

        CompletableFuture<Address> result = CompletableFuture.allOf(viaCepFuture, brasilApiFuture, cepAbertoFuture)
                .thenApplyAsync(v -> {
                    try {
                        ViaCepResponse viaCepResponse = viaCepFuture.get();
                        BrasilApiResponse brasilApiResponse = brasilApiFuture.get();
                        CepAbertoResponse cepAbertoResponse = cepAbertoFuture.get();

                        Address viaCepAddress = createAddressFromResponse(viaCepResponse, "viacep");
                        Address brasilApiAddress = createAddressFromResponse(brasilApiResponse, "brasilapi");
                        Address cepAbertoAddress = createAddressFromResponse(cepAbertoResponse, "cepaberto");

                        addressRepository.save(viaCepAddress);
                        addressRepository.save(brasilApiAddress);
                        addressRepository.save(cepAbertoAddress);

                        return viaCepAddress; // Pode retornar qualquer um dos endereços
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                });

        return result;
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

