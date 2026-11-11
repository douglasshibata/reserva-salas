package com.reservasalas.sugestao.model;

public class RoomDTO {
    public Long id;
    public String name;
    public Integer capacity;
    public String location;
    public String features;
    public Boolean active;

    public RoomDTO() {}

    public RoomDTO(Long id, String name, Integer capacity, String location, String features, Boolean active) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.features = features;
        this.active = active;
    }
}
