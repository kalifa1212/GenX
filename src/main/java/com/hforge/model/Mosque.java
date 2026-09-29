package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Mosque {

    /** node | way | relation */
    private String osmType;

    private long osmId;

    private String name;

    private String nameFr;

    private String nameEn;

    private String nameAr;

    private String altName;

    private String denomination;

    private String building;

    private String city;

    private String street;

    private String houseNumber;

    private String postcode;

    private String phone;

    private String website;

    private String openingHours;

    private String wikidata;

    private double latitude;

    private double longitude;

}
