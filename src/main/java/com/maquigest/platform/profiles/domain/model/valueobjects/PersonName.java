package com.maquigest.platform.profiles.domain.model.valueobjects;
public record PersonName(String firstName, String lastName) {
    public PersonName {
        firstName = required(firstName, "firstName", 100);
        lastName = required(lastName, "lastName", 150);
    }
    private static String required(String s,String field,int max) {
        if(s==null||s.isBlank()||s.trim().length()>max) throw new IllegalArgumentException(field+" is required (max "+max+")");
        return s.trim();
    }
}
