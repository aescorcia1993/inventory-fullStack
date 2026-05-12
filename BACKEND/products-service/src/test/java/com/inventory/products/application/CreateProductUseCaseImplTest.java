package com.inventory.products.application;

import com.inventory.products.application.command.CreateProductCommand;
import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.out.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CreateProductUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_withValidCommand_savesAndReturnsProduct() {
        var command = new CreateProductCommand("Widget", new BigDecimal("9.99"), "A widget");

        Product result = sut.execute(command);

        assertThat(result.getNombre()).isEqualTo("Widget");
        assertThat(result.getPrecio()).isEqualByComparingTo("9.99");
        assertThat(result.getDescripcion()).isEqualTo("A widget");
        assertThat(result.getId()).isNotNull();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Widget");
    }

    @Test
    void execute_withNullNombre_throwsIllegalArgumentException() {
        var command = new CreateProductCommand(null, new BigDecimal("9.99"), null);
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        verifyNoInteractions(productRepository);
    }

    @Test
    void execute_withNegativePrice_throwsIllegalArgumentException() {
        var command = new CreateProductCommand("Widget", new BigDecimal("-1.00"), null);
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precio");
        verifyNoInteractions(productRepository);
    }

    @Test
    void execute_withZeroPrice_throwsIllegalArgumentException() {
        var command = new CreateProductCommand("Widget", BigDecimal.ZERO, null);
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(productRepository);
    }
}
