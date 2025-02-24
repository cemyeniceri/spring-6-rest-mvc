package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CustomerController {
    public static final String CUSTOMER_PATH = "/api/v1/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    private final CustomerService customerService;

    @GetMapping(CUSTOMER_PATH)
    public List<CustomerDTO> listCustomers() {
        log.debug("List customers - in controller");
        return customerService.getAllCustomers();
    }

    @GetMapping(CUSTOMER_PATH_ID)
    public CustomerDTO getCustomerById(@PathVariable("customerId") UUID customerId) {
        log.debug("Get customer by id - in controller");
        return customerService.getCustomerById(customerId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(CUSTOMER_PATH)
    public ResponseEntity<HttpHeaders> createCustomer(@RequestBody CustomerDTO customer) {
        log.debug("Create customer - in controller");
        CustomerDTO savedCustomer = customerService.saveCustomer(customer);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "/api/v1/customer/" + savedCustomer.getId().toString());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @PutMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpHeaders> updateCustomer(@PathVariable("customerId") UUID customerId, @RequestBody CustomerDTO customer) {
        log.debug("Update customer by Id - in controller");
        Optional<CustomerDTO> updateCustomer = customerService.updateCustomerById(customerId, customer);
        if (updateCustomer.isEmpty()) {
            throw new NotFoundException();
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpHeaders> deleteCustomer(@PathVariable("customerId") UUID customerId) {
        log.debug("Delete customer by Id - in controller");
        Boolean result = customerService.deleteCustomerById(customerId);
        if (result) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new NotFoundException();
        }
    }

    @PatchMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpHeaders> updateCustomerPartially(@PathVariable("customerId") UUID customerId, @RequestBody CustomerDTO customer) {
        log.debug("Patch customer by Id - in controller");
        customerService.updateCustomerPartiallyById(customerId, customer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
