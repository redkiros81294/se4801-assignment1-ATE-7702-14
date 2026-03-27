package com.shopwave;

import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.shopwave.shopwavestarter.ShopwaveStarterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronicsCategory;
    private Category clothingCategory;
    private Product laptopProduct;
    private Product phoneProduct;
    private Product shirtProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronicsCategory = Category.builder()
                .name("Electronics")
                .desc("Electronic devices and accessories")
                .build();
        electronicsCategory = categoryRepository.save(electronicsCategory);

        clothingCategory = Category.builder()
                .name("Clothing")
                .desc("Apparel and fashion items")
                .build();
        clothingCategory = categoryRepository.save(clothingCategory);

        laptopProduct = Product.builder()
                .name("Laptop Pro 15")
                .desc("High-performance laptop with 16GB RAM")
                .price(new BigDecimal("1299.99"))
                .stock(50)
                .cat(electronicsCategory)
                .build();
        laptopProduct = productRepository.save(laptopProduct);

        phoneProduct = Product.builder()
                .name("Smartphone X")
                .desc("Latest generation smartphone")
                .price(new BigDecimal("899.99"))
                .stock(100)
                .cat(electronicsCategory)
                .build();
        phoneProduct = productRepository.save(phoneProduct);

        shirtProduct = Product.builder()
                .name("Cotton T-Shirt")
                .desc("Comfortable cotton t-shirt")
                .price(new BigDecimal("29.99"))
                .stock(200)
                .cat(clothingCategory)
                .build();
        shirtProduct = productRepository.save(shirtProduct);
    }

    @Test
    void testFindByCategoryId() {
        List<Product> electronicsProducts = productRepository.findByCat_Id(electronicsCategory.getId());
        List<Product> clothingProducts = productRepository.findByCat_Id(clothingCategory.getId());

        assertEquals(2, electronicsProducts.size());
        assertEquals(1, clothingProducts.size());
        
        assertTrue(electronicsProducts.stream()
                .allMatch(p -> p.getCat().getId().equals(electronicsCategory.getId())));
        
        assertTrue(clothingProducts.stream()
                .allMatch(p -> p.getCat().getId().equals(clothingCategory.getId())));
    }

    @Test
    void testFindByPriceLessThanEqual() {
        List<Product> cheapProducts = productRepository.findByPriceLessThanEqual(new BigDecimal("100.00"));
        List<Product> midRangeProducts = productRepository.findByPriceLessThanEqual(new BigDecimal("1000.00"));
        List<Product> allProducts = productRepository.findByPriceLessThanEqual(new BigDecimal("5000.00"));

        assertEquals(1, cheapProducts.size());
        assertEquals(2, midRangeProducts.size());
        assertEquals(3, allProducts.size());
        
        assertEquals("Cotton T-Shirt", cheapProducts.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Product> laptopResults = productRepository.findByNameContainingIgnoreCase("Laptop");
        List<Product> smartphoneResults = productRepository.findByNameContainingIgnoreCase("smartphone");
        List<Product> caseInsensitiveResults = productRepository.findByNameContainingIgnoreCase("PRO");
        List<Product> noResults = productRepository.findByNameContainingIgnoreCase("XYZ123");

        assertEquals(1, laptopResults.size());
        assertEquals("Laptop Pro 15", laptopResults.get(0).getName());
        
        assertEquals(1, smartphoneResults.size());
        assertEquals("Smartphone X", smartphoneResults.get(0).getName());
        
        assertEquals(1, caseInsensitiveResults.size());
        assertEquals("Laptop Pro 15", caseInsensitiveResults.get(0).getName());
        
        assertTrue(noResults.isEmpty());
    }

    @Test
    void testFindTopByOrderByPriceDesc() {
        Product mostExpensive = productRepository.findTopByOrderByPriceDesc();

        assertNotNull(mostExpensive);
        assertEquals("Laptop Pro 15", mostExpensive.getName());
        assertEquals(new BigDecimal("1299.99"), mostExpensive.getPrice());
    }

    @Test
    void testSaveProduct() {
        Product newProduct = Product.builder()
                .name("Tablet Mini")
                .desc("Compact tablet for everyday use")
                .price(new BigDecimal("499.99"))
                .stock(75)
                .cat(electronicsCategory)
                .build();

        Product savedProduct = productRepository.save(newProduct);

        assertNotNull(savedProduct.getId());
        
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());
        assertTrue(retrievedProduct.isPresent());
        assertEquals("Tablet Mini", retrievedProduct.get().getName());
    }

    @Test
    void testUpdateProduct() {
        Long productId = laptopProduct.getId();
        Optional<Product> productToUpdate = productRepository.findById(productId);
        assertTrue(productToUpdate.isPresent());

        Product product = productToUpdate.get();
        product.setStock(45);
        product.setPrice(new BigDecimal("1199.99"));
        productRepository.save(product);

        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(45, updatedProduct.get().getStock());
        assertEquals(new BigDecimal("1199.99"), updatedProduct.get().getPrice());
    }

    @Test
    void testDeleteProduct() {
        Long productId = shirtProduct.getId();
        assertTrue(productRepository.existsById(productId));

        productRepository.deleteById(productId);

        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void testFindAll() {
        List<Product> allProducts = productRepository.findAll();

        assertEquals(3, allProducts.size());
    }

    @Test
    void testFindByNonExistentCategory() {
        List<Product> products = productRepository.findByCat_Id(999L);

        assertTrue(products.isEmpty());
    }

    @Test
    void testFindByPriceLessThanEqualNoResults() {
        List<Product> products = productRepository.findByPriceLessThanEqual(new BigDecimal("1.00"));

        assertTrue(products.isEmpty());
    }

    @Test
    void testProductCategoryRelationship() {
        Optional<Product> product = productRepository.findById(laptopProduct.getId());

        assertTrue(product.isPresent());
        assertNotNull(product.get().getCat());
        assertEquals("Electronics", product.get().getCat().getName());
    }
}