package com.maquigest.platform.profiles.domain.model.valueobjects;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompanyAddressTests {
    @Test void rejectsInvalidCoordinates(){
        assertThrows(IllegalArgumentException.class,()->new CompanyAddress("","","","",91,0));
        assertThrows(IllegalArgumentException.class,()->new CompanyAddress("","","","",0,-190));
        assertThrows(IllegalArgumentException.class,()->new CompanyAddress("","","","",Double.NaN,0));
    }
    @Test void supportsInitiallyEmptyAddress(){assertEquals("",CompanyAddress.empty().city());}
}
