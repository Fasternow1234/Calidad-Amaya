package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.response.StockResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public List<StockResponse> listarPorAlmacen(Long idAlmacen) {
        return stockRepository.findByAlmacenId(idAlmacen).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StockResponse> listarPorProducto(Long idProducto) {
        return stockRepository.findByProductoId(idProducto).stream()
                .map(this::toResponse)
                .toList();
    }

    private StockResponse toResponse(Stock s) {
        return StockResponse.builder()
                .id(s.getId())
                .idProducto(s.getProducto().getId())
                .codigoProducto(s.getProducto().getCodigo())
                .nombreProducto(s.getProducto().getNombre())
                .unidadMedida(s.getProducto().getUnidadMedida())
                .idAlmacen(s.getAlmacen().getId())
                .nombreAlmacen(s.getAlmacen().getNombre())
                .cantidad(s.getCantidad())
                .fechaActualizacion(s.getFechaActualizacion())
                .build();
    }
}