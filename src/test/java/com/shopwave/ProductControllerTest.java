package com.shopwave;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwave.controller.ProductController;
import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.exception.GlobalExceptionHandler;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ProductController
 */
@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@Import({GlobalExceptionHandler.class})
class ProductControllerTest {

    @SpringBootApplication(scanBasePackages = "com.shopwave")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO testProductDTO;
    private CreateProductRequest createProductRequest;
    private Page<ProductDTO> productPage;

    @BeforeEach
    void setUp() {
        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .categoryName("Electronics")
                .createdAt(LocalDateTime.now())
                .build();

        createProductRequest = CreateProductRequest.builder()
                .name("Test Product")
                .desc("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .categoryId(1L)
                .build();

        List<ProductDTO> productList = Arrays.asList(testProductDTO);
        productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.content[0].price").value(99.99))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService, times(1)).getAllProducts(any(PageRequest.class));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProductDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(testProductDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void testCreateProductValidationError() throws Exception {
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .name("")
                .desc("")
                .price(BigDecimal.valueOf(-10))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    void testSearchProducts() throws Exception {
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.searchProducts(eq("Test"), isNull())).thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99));

        verify(productService, times(1)).searchProducts(eq("Test"), isNull());
    }

    @Test
    void testSearchProductsByMaxPrice() throws Exception {
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.searchProducts(isNull(), eq(new BigDecimal("100.00")))).thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                        .param("maxPrice", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService, times(1)).searchProducts(isNull(), eq(new BigDecimal("100.00")));
    }

    @Test
    void testSearchProductsNoResults() throws Exception {
        when(productService.searchProducts(any(), any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(productService, times(1)).searchProducts(eq("NonExistent"), isNull());
    }

    @Test
    void testUpdateStock() throws Exception {
        ProductDTO updatedProduct = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .stock(15)
                .build();
        when(productService.updateStock(eq(1L), eq(5))).thenReturn(updatedProduct);

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));

        verify(productService, times(1)).updateStock(1L, 5);
    }

    @Test
    void testUpdateStockMissingDelta() throws Exception {
        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Delta value is required"));

        verify(productService, never()).updateStock(any(), anyInt());
    }

    @Test
    void testUpdateStockNegativeResult() throws Exception {
        when(productService.updateStock(1L, -20)).thenThrow(new IllegalArgumentException("Stock cannot be negative"));

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(productService, times(1)).updateStock(1L, -20);
    }

    @Test
    void testUpdateStockProductNotFound() throws Exception {
        when(productService.updateStock(999L, 5)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(patch("/api/products/999/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(productService, times(1)).updateStock(999L, 5);
    }
}