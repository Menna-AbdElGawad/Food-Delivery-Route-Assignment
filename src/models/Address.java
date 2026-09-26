package models;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Address {
    private final String district;
    private final String details;

    public Address(String district, String details) {
        if (district == null || district.isBlank()) {
            throw new IllegalArgumentException("District is required.");
        }

        if (details == null || details.isBlank()) {
            throw new IllegalArgumentException("Address details are required.");
        }

        this.district = district.trim();
        this.details = details.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return district.equalsIgnoreCase(address.district) && details.equalsIgnoreCase(address.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(district.toLowerCase(), details.toLowerCase());
    }

    @Override
    public String toString() {
        return "Address{" +
                "district='" + district + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
