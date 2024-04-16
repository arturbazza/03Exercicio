package br.org.fundatec.atividade03.repository;

import br.org.fundatec.atividade03.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findByCep(String cep);
}
