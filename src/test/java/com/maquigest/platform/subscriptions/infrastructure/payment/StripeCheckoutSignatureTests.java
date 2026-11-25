package com.maquigest.platform.subscriptions.infrastructure.payment;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.jupiter.api.Assertions.*;
class StripeCheckoutSignatureTests {
    @Test void configurationAcceptsOnlySupportedTestKeys() {
        var mapper = new tools.jackson.databind.ObjectMapper();
        var url = "http://localhost:4200";
        assertTrue(new StripeCheckoutAdapter(true,"sk_test_example","whsec_example",url,mapper).isEnabled());
        assertTrue(new StripeCheckoutAdapter(true,"rkcs_test_example","whsec_example",url,mapper).isEnabled());
        for (String key : new String[]{"sk_live_example","rkcs_live_example","pk_test_example","sk_test_","rkcs_test_",""}) {
            assertThrows(IllegalStateException.class, () -> new StripeCheckoutAdapter(true,key,"whsec_example",url,mapper));
        }
    }
    @Test void signatureRequiresOriginalBodyAndFreshTimestamp() throws Exception {
        var now=Instant.parse("2026-01-01T00:00:00Z");var secret="whsec_test_secret";var payload="{\"test\":true}".getBytes(StandardCharsets.UTF_8);
        var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));mac.update((now.getEpochSecond()+".").getBytes(StandardCharsets.UTF_8));
        var header="t="+now.getEpochSecond()+",v1="+HexFormat.of().formatHex(mac.doFinal(payload));
        assertTrue(StripeCheckoutAdapter.validSignature(payload,header,secret,now));
        assertFalse(StripeCheckoutAdapter.validSignature(new byte[]{1},header,secret,now));
        assertFalse(StripeCheckoutAdapter.validSignature(payload,header,secret,now.plusSeconds(301)));
        assertFalse(StripeCheckoutAdapter.validSignature(payload,header,"wrong",now));
    }
}
