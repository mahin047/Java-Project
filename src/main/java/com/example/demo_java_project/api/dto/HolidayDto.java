package com.example.demo_java_project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayDto {

    private String date;        // "2026-12-16"
    private String localName;
    private String name;
    private String countryCode;

    public String getDate()               { return date; }
    public void setDate(String date)      { this.date = date; }

    public String getLocalName()          { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public String getCountryCode()        { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}