package br.org.fundatec.atividade03.controller;

import br.org.fundatec.atividade03.model.Address;
import br.org.fundatec.atividade03.service.AddressService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Data
@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/address/{cep}")
    public CompletableFuture<Address> getAddressByCep(@PathVariable String cep) {
        return addressService.findOrCreateByCep(cep);
    }
}
