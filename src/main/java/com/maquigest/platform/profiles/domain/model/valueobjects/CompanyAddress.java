package com.maquigest.platform.profiles.domain.model.valueobjects;
public record CompanyAddress(String street, String district, String city, String country,
                             double latitude, double longitude) {
    public CompanyAddress {
        street=clean(street,"street",250);
        district=clean(district,"district",120);
        city=clean(city,"city",120);
        country=clean(country,"country",120);
        if(!Double.isFinite(latitude)||latitude < -90||latitude > 90)
            throw new IllegalArgumentException("latitude out of range");
        if(!Double.isFinite(longitude)||longitude < -180||longitude > 180)
            throw new IllegalArgumentException("longitude out of range");
    }
    private static String clean(String s, String field,int max){
        String value=s==null?"":s.trim();
        if(value.length()>max)throw new IllegalArgumentException(field+" exceeds "+max+" characters");
        return value;
    }
    public static CompanyAddress empty(){return new CompanyAddress("","","","",0,0);}
}
