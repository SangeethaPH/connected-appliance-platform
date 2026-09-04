package com.connectedhome.infra.connector;

import com.connectedhome.infra.entity.VendorEntity;
import com.connectedhome.infra.exception.VendorCommunicationException;
import com.connectedhome.infra.security.CredentialCipher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BasicAuthVendorConnector implements VendorConnector {
    private final RestClient.Builder restClientBuilder;
    private final CredentialCipher credentialCipher;

    public BasicAuthVendorConnector(RestClient.Builder restClientBuilder, CredentialCipher credentialCipher) {
        this.restClientBuilder = restClientBuilder;
        this.credentialCipher = credentialCipher;
    }

    @Override
    public VendorApplianceSnapshot fetchAppliance(VendorEntity vendor, String externalApplianceId) {
        try {
            VendorApplianceSnapshot response = restClientBuilder.baseUrl(vendor.getBaseUrl())
                    .defaultHeaders(headers -> headers.setBasicAuth(
                            vendor.getAuthUsername(), credentialCipher.decrypt(vendor.getAuthPasswordEncrypted())))
                    .build()
                    .get()
                    .uri("/api/simulator/appliances/{id}", externalApplianceId)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(VendorApplianceSnapshot.class);
            if (response == null) {
                throw new VendorCommunicationException("Vendor returned an empty appliance response");
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw new VendorCommunicationException(
                    "Vendor request failed with HTTP " + exception.getStatusCode().value(), exception);
        } catch (VendorCommunicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new VendorCommunicationException("Could not contact vendor: " + exception.getMessage(), exception);
        }
    }
}
