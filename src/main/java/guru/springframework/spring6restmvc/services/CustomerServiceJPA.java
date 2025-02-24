package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
public class CustomerServiceJPA implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream().map(customerMapper::customerToCustomerDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<CustomerDTO> getCustomerById(UUID id) {
        return customerRepository.findById(id).map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customerMapper.customerDtoToCustomer(customer)));
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customer) {
        return customerRepository.findById(customerId).map(customerFound -> {
                    customerFound.setCustomerName(customer.getCustomerName());
                    return customerMapper.customerToCustomerDTO(customerRepository.save(customerFound));
                }
        );
    }

    @Override
    public Boolean deleteCustomerById(UUID customerId) {
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<CustomerDTO> updateCustomerPartiallyById(UUID customerId, CustomerDTO customer) {
        return customerRepository.findById(customerId).map(customerFound -> {
            if (StringUtils.hasText(customer.getCustomerName())) {
                customerFound.setCustomerName(customer.getCustomerName());
            }
            return customerMapper.customerToCustomerDTO(customerRepository.save(customerFound));
        });
    }
}
