package com.br.azevedo.conversor.serializer;

import com.br.azevedo.utils.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BigDecimalCurrencySerializerTest {

    public static class SampleDto {
        private BigDecimal valor;

        public SampleDto() {}

        public SampleDto(BigDecimal valor) {
            this.valor = valor;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }
    }

    @Test
    @DisplayName("Deve serializar BigDecimal no objectMapperRedis sem lançar Type id handling not implemented")
    void shouldSerializeBigDecimalWithRedisObjectMapper() {
        SampleDto dto = new SampleDto(new BigDecimal("150.50"));

        assertDoesNotThrow(() -> {
            String json = JsonUtils.objectMapperRedis().writeValueAsString(dto);
            assertTrue(json.contains("150,50"));
        });
    }
}
