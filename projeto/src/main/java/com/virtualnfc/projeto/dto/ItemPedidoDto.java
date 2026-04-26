package com.virtualnfc.projeto.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemPedidoDto(
    String id,
    String nome,
    Integer quantidade,
    Double preco
) {
    @JsonCreator
    public ItemPedidoDto(
        @JsonProperty("id") String id,
        @JsonProperty("nome") String nome,
        @JsonProperty("quantidade") Integer quantidade,
        @JsonProperty("preco") Double preco
    ) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }
}