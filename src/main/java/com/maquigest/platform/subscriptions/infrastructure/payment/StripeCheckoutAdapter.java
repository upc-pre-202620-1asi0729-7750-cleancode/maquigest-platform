package com.maquigest.platform.subscriptions.infrastructure.payment;
import com.maquigest.platform.subscriptions.application.ports.StripeCheckoutPort;
import com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure;
import static com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure.Code.*;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import tools.jackson.databind.ObjectMapper;
@Component
public class StripeCheckoutAdapter implements StripeCheckoutPort {
    private final boolean enabled;
    private final String key,webhookSecret,baseUrl;
    private final RestClient client;
    private final ObjectMapper mapper;
    public StripeCheckoutAdapter(@Value("${maquigest.stripe.enabled:false}") boolean enabled,
            @Value("${STRIPE_SECRET_KEY:}") String key,@Value("${STRIPE_WEBHOOK_SECRET:}") String webhookSecret,
            @Value("${MAQUIGEST_WEBAPP_BASE_URL:http://localhost:4200}") String baseUrl,ObjectMapper mapper) {
        this.enabled=enabled;this.key=key;this.webhookSecret=webhookSecret;this.baseUrl=baseUrl.replaceAll("/+$","");this.mapper=mapper;
        if (enabled && (!key.matches("(?:sk_test_|rkcs_test_)[A-Za-z0-9_]+") || !webhookSecret.startsWith("whsec_") || !this.baseUrl.matches("https://[A-Za-z0-9.-]+(?::[0-9]+)?|http://localhost:[0-9]+")))
            throw new IllegalStateException("Stripe test configuration incomplete");
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client=RestClient.builder().baseUrl("https://api.stripe.com").requestFactory(factory).build();
    }
    public boolean isEnabled(){return enabled;}
    public Session create(SubscriptionCheckout c) {
        if(!enabled) throw new CheckoutFailure(UNAVAILABLE);
        var form=new LinkedMultiValueMap<String,String>();
        form.add("mode","payment");form.add("payment_method_types[]","card");
        form.add("adaptive_pricing[enabled]","false");
        form.add("line_items[0][price_data][currency]","pen");
        form.add("line_items[0][price_data][product_data][name]","MaquiGest monthly plan "+c.getPlanId());
        form.add("line_items[0][price_data][unit_amount]",Long.toString(c.getAmountMinor()));
        form.add("line_items[0][quantity]","1");
        form.add("client_reference_id",c.getReference());
        form.add("expires_at",Long.toString(c.getExpiresAt().getEpochSecond()));
        var returnUrl=baseUrl+"/subscriptions/plans?checkout="+c.getReference();
        form.add("success_url",returnUrl);form.add("cancel_url",returnUrl);
        try {
            return parse(client.post().uri("/v1/checkout/sessions").headers(h->{h.setBearerAuth(key);h.set("Idempotency-Key",c.getReference());})
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class));
        } catch(RuntimeException e){throw new CheckoutFailure(PROVIDER);}
    }
    public Session retrieve(String id) {
        if(!enabled)throw new CheckoutFailure(UNAVAILABLE);
        if(id==null || !id.matches("cs_test_[A-Za-z0-9_]+"))throw new CheckoutFailure(INVALID);
        try{return parse(client.get().uri("/v1/checkout/sessions/{id}",id).headers(h->h.setBearerAuth(key)).retrieve().body(Map.class));}
        catch(RuntimeException e){throw new CheckoutFailure(PROVIDER);}
    }
    private static Session parse(Map<?,?> data) {
        if(data==null || !(data.get("amount_total") instanceof Number amount))throw new CheckoutFailure(PROVIDER);
        String url=(String)data.get("url");
        if(url!=null && !url.startsWith("https://checkout.stripe.com/"))throw new CheckoutFailure(PROVIDER);
        return new Session((String)data.get("id"),(String)data.get("client_reference_id"),amount.longValue(),(String)data.get("currency"),
            (String)data.get("status"),(String)data.get("payment_status"),Boolean.FALSE.equals(data.get("livemode")),(String)data.get("mode"),url);
    }
    static boolean validSignature(byte[] payload,String header,String secret,Instant now) {
        if(payload==null || header==null || secret==null || secret.isBlank())return false;
        try {
            String timestamp=null;var signatures=new ArrayList<String>();
            for(String part:header.split(",")) {var pair=part.trim().split("=",2);if(pair.length!=2)continue;
                if("t".equals(pair[0])){if(timestamp!=null)return false;timestamp=pair[1];}else if("v1".equals(pair[0]))signatures.add(pair[1]);}
            if(timestamp==null || !timestamp.matches("[0-9]{1,12}") || Math.abs(now.getEpochSecond()-Long.parseLong(timestamp))>300)return false;
            Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            mac.update((timestamp+".").getBytes(StandardCharsets.UTF_8));byte[] expected=mac.doFinal(payload);
            for(String sig:signatures)if(sig.matches("[a-fA-F0-9]{64}") && MessageDigest.isEqual(expected,HexFormat.of().parseHex(sig)))return true;
            return false;
        }catch(Exception e){return false;}
    }
    public Optional<String> verifyWebhook(byte[] payload,String signature) {
        if(!enabled)throw new CheckoutFailure(UNAVAILABLE);
        if(!validSignature(payload,signature,webhookSecret,Instant.now()))throw new CheckoutFailure(INVALID);
        try {
            var event=mapper.readTree(payload);
            if(!event.path("livemode").isBoolean() || event.path("livemode").asBoolean())throw new CheckoutFailure(INVALID);
            String type=event.path("type").asText();
            if(!Set.of("checkout.session.completed","checkout.session.expired","checkout.session.async_payment_succeeded").contains(type))return Optional.empty();
            String id=event.path("data").path("object").path("id").asText();
            if(!id.matches("cs_test_[A-Za-z0-9_]+"))throw new CheckoutFailure(INVALID);
            return Optional.of(id);
        }catch(RuntimeException e){throw new CheckoutFailure(INVALID);}
    }
}
