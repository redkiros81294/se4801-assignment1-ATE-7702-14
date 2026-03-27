package com.shopwave;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import com.shopwave.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private Category testCategory;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .cat(testCategory)
                .createdAt(LocalDateTime.now())
                .build();

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .categoryName("Electronics")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateProductRequest.builder()
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .categoryId(1L)
                .build();
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.createProduct(createRequest);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("99.99"), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateProductWithoutCategory() {
        CreateProductRequest requestWithoutCategory = CreateProductRequest.builder()
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.createProduct(requestWithoutCategory);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = Arrays.asList(testProduct);
        org.springframework.data.domain.Page<Product> productPage = 
                new org.springframework.data.domain.PageImpl<>(products);
        
        when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        org.springframework.data.domain.Page<ProductDTO> result = 
                productService.getAllProducts(org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testSearchProducts() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        List<ProductDTO> result = productService.searchProducts("Test", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void testSearchProductsByMaxPrice() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByPriceLessThanEqual(new BigDecimal("100.00"))).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        List<ProductDTO> result = productService.searchProducts(null, new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByPriceLessThanEqual(new BigDecimal("100.00"));
    }

    @Test
    void testSearchProductsWithKeywordAndMaxPrice() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        List<ProductDTO> result = productService.searchProducts("Test", new BigDecimal("100.00"));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void testUpdateStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.updateStock(1L, 5);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateStockNegative() {
        Product productWithLowStock = Product.builder()
                .id(1L)
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(3)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWithLowStock));

        assertThrows(IllegalArgumentException.class, () -> productService.updateStock(1L, -5));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateStockDecrease() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.updateStock(1L, -5);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateStockProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateStock(999L, 5));
        verify(productRepository, times(1)).findById(999L);
    }
}