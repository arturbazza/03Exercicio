package br.org.fundatec.atividade03.service;

import br.org.fundatec.atividade03.controller.response.ViaCepResponse;
import br.org.fundatec.atividade03.model.Address;
import br.org.fundatec.atividade03.repository.AddressRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
        String apiUrl = "https://viacep.com.br/ws/" + cep + "/json/";
        ViaCepResponse viaCepResponse = restTemplate.getForObject(apiUrl, ViaCepResponse.class);

        Address newAddress = new Address();
        newAddress.setCep(viaCepResponse.getCep());
        newAddress.setStreet(viaCepResponse.getLogradouro());
        newAddress.setCity(viaCepResponse.getLocalidade());
        newAddress.setState(viaCepResponse.getUf());

        return addressRepository.save(newAddress);
    }
}
