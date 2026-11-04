package com.maquigest.platform.profiles.domain.model.valueobjects;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PersonNameTests {
    @Test void rejectsBlankNames(){assertThrows(IllegalArgumentException.class,()->new PersonName(" ","Company"));}
    @Test void trimsNames(){assertEquals("Ana",new PersonName(" Ana "," Perez ").firstName());}
    @Test void emailNormalizesAndValidates(){
        assertEquals("test@example.com",new EmailAddress(" TEST@EXAMPLE.COM ").value());
        assertThrows(IllegalArgumentException.class,()->new EmailAddress("invalid"));
    }
}
