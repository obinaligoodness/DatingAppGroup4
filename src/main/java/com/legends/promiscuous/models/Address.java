package com.legends.promiscuous.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Address")
@Setter
@Getter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String houseNumber;
    private String street;
    private String state;
    private String country;

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(houseNumber)
                .append(",")
                .append(street)
                .append(",")
                .append(state)
                .append(",")
                .append(country);

        return builder.toString();
    }
}
