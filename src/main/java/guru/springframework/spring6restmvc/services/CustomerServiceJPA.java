package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvcapi.models.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class CustomerServiceJPA implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CacheManager cacheManager;

    @Cacheable(cacheNames = "customerListCache")
    @Override
    public List<CustomerDTO> getAllCustomers() {
        log.info("Get all customers - in service");
        return customerRepository.findAll().stream().map(customerMapper::customerToCustomerDTO).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "customerCache", key = "#id")
    @Override
    public Optional<CustomerDTO> getCustomerById(UUID id) {
        log.info("Get customer by id - in service");
        return customerRepository.findById(id).map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customer) {
        Optional.ofNullable(cacheManager.getCache("customerListCache")).ifPresent(Cache::clear);
        return customerMapper.customerToCustomerDTO(customerRepository.save(customerMapper.customerDtoToCustomer(customer)));
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customer) {
        clearCache(customerId);
        return customerRepository.findById(customerId).map(customerFound -> {
                    customerFound.setCustomerName(customer.getCustomerName());
                    return customerMapper.customerToCustomerDTO(customerRepository.save(customerFound));
                }
        );
    }

    @Override
    public Boolean deleteCustomerById(UUID customerId) {
        clearCache(customerId);
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<CustomerDTO> updateCustomerPartiallyById(UUID customerId, CustomerDTO customer) {
        clearCache(customerId);
        return customerRepository.findById(customerId).map(customerFound -> {
            if (StringUtils.hasText(customer.getCustomerName())) {
                customerFound.setCustomerName(customer.getCustomerName());
            }
            return customerMapper.customerToCustomerDTO(customerRepository.save(customerFound));
        });
    }

    private void clearCache(UUID uuid) {
        Optional.ofNullable(cacheManager.getCache("customerListCache")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("customerCache")).ifPresent(cache -> cache.evict(uuid));
    }
}
