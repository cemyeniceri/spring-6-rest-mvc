package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.models.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerMapper customerMapper;
    @Autowired
    private BeerOrderRepository beerOrderRepository;

    @Test
    void testGetCustomerByIdNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.getCustomerById(UUID.randomUUID()));
    }

    @Test
    void testGetCustomerById() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());

        assertThat(customerDTO).isNotNull();
    }

    @Test
    void testGetAllCustomers() {
        assertThat(customerRepository.findAll().size()).isEqualTo(3);
    }

    @Transactional
    @Rollback
    @Test
    void testGetCustomerListEmpty() {
        beerOrderRepository.deleteAll();
        customerRepository.deleteAll();
        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Transactional
    @Rollback
    @Test
    void testDeleteCustomer() {
        Customer customer = customerRepository.findAll().getFirst();
        ResponseEntity<HttpHeaders> response = customerController.deleteCustomer(customer.getId());
        Optional<Customer> customerDeleted = customerRepository.findById(customer.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerDeleted.isEmpty()).isTrue();
    }

    @Test
    void testDeleteCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.deleteCustomer(UUID.randomUUID()));
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateCustomer() {
        Customer customer = customerRepository.findAll().getFirst();
        CustomerDTO customerDTO = customerMapper.customerToCustomerDTO(customer);
        customerDTO.setId(null);
        customerDTO.setVersion(null);

        final String customerName = "UPDATED";
        customerDTO.setCustomerName(customerName);

        ResponseEntity<HttpHeaders> response = customerController.updateCustomer(customer.getId(), customerDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<Customer> customerUpdated = customerRepository.findById(customer.getId());
        assertThat(customerUpdated.isPresent()).isTrue();
        assertThat(customerUpdated.get().getCustomerName()).isEqualTo(customerName);
    }

    @Test
    void testUpdateCustomerNotFound() {
        assertThrows(NotFoundException.class, () -> customerController.updateCustomer(UUID.randomUUID(), CustomerDTO.builder().build()));
    }

    @Transactional
    @Rollback
    @Test
    void testCreateCustomer() {
        CustomerDTO customerDTO = CustomerDTO.builder()
                .customerName("New Customer")
                .build();

        ResponseEntity<HttpHeaders> response = customerController.createCustomer(customerDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = response.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);
        Optional<Customer> savedCustomer = customerRepository.findById(savedUUID);
        assertThat(savedCustomer.isPresent()).isTrue();
    }
}