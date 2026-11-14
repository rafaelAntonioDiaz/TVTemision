package com.rafaeldiaz.emisiontvt_ff.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final List<Product> all = new ArrayList<>();
    private final Map<String, Product> byEs = new HashMap<>();
    private final Map<String, Product> byEn = new HashMap<>();

    public ProductService() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:content/products/*.yml");
        for (Resource r : resources) {
            try (InputStream is = r.getInputStream()) {
                Product p = mapper.readValue(is, Product.class);
                all.add(p);
                byEs.put(p.domain() + "/" + p.slugEs(), p);
                byEn.put(p.domainEn() + "/" + p.slugEn(), p);
            }
        }
        all.sort(Comparator.comparing(Product::name));
    }

    public List<Product> all() { return all; }

    public Optional<Product> findByEs(String domain, String slug) {
        return Optional.ofNullable(byEs.get(domain + "/" + slug));
    }

    public Optional<Product> findByEn(String domain, String slug) {
        return Optional.ofNullable(byEn.get(domain + "/" + slug));
    }

    public Map<String, List<Product>> groupedByDomainEs() {
        return all.stream().collect(Collectors.groupingBy(
                Product::domain, LinkedHashMap::new, Collectors.toList()));
    }

    public Map<String, List<Product>> groupedByDomainEn() {
        return all.stream().collect(Collectors.toMap(
                Product::domainEn, p -> List.of(p), (a, b) -> {
                    List<Product> m = new ArrayList<>(a); m.addAll(b); return m;
                }, LinkedHashMap::new));
    }

    public String domainLabelEs(String domain) {
        return all.stream().filter(p -> p.domain().equals(domain))
                .map(Product::domainLabelEs).findFirst().orElse(domain);
    }

    public String domainLabelEn(String domainEn) {
        return all.stream().filter(p -> p.domainEn().equals(domainEn))
                .map(Product::domainLabelEn).findFirst().orElse(domainEn);
    }
}