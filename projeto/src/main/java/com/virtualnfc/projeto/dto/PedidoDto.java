package com.virtualnfc.projeto.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PedidoDto(
    List<ItemPedidoDto> itens,
    Double valorTotal
) {
    @JsonCreator
    public PedidoDto(
        @JsonProperty("itens") List<ItemPedidoDto> itens,
        @JsonProperty("valorTotal") Double valorTotal
    ) {
        this.itens = itens;
        this.valorTotal = valorTotal;
    }
}