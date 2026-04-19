package com.indodb.games_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItadApiServiceTests {

    private static final String BASE_URL = "https://api.isthereanydeal.com";
    private static final String API_KEY = "test-key";

    @Test
    void lookupItadIdBySteamIdReturnsGameId() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        FakeRateLimiterService rateLimiter = new FakeRateLimiterService();

        ItadApiService service = service(restTemplate, rateLimiter);

        server.expect(once(), requestTo(BASE_URL + "/games/lookup/v1?key=" + API_KEY + "&appid=292030"))
                .andRespond(withSuccess("""
                        {
                          "found": true,
                          "game": {
                            "id": "018d937f-012f-73b8-ab2c-898516969e6a",
                            "title": "The Witcher 3: Wild Hunt"
                          }
                        }
                        """, APPLICATION_JSON));

        String itadId = service.lookupItadIdBySteamId("292030");

        assertThat(itadId).isEqualTo("018d937f-012f-73b8-ab2c-898516969e6a");
        assertThat(rateLimiter.recordedApiName).isEqualTo("itad");
        server.verify();
    }

    @Test
    void getBatchPricesExtractsEpicAndGogPrices() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        FakeRateLimiterService rateLimiter = new FakeRateLimiterService();

        ItadApiService service = service(restTemplate, rateLimiter);
        String itadId = "018d937f-012f-73b8-ab2c-898516969e6a";

        server.expect(once(), requestTo(BASE_URL + "/games/prices/v3?key=" + API_KEY + "&country=IN"))
                .andExpect(method(POST))
                .andExpect(content().json("[\"" + itadId + "\"]"))
                .andRespond(withSuccess("""
                        [
                          {
                            "id": "018d937f-012f-73b8-ab2c-898516969e6a",
                            "deals": [
                              {
                                "shop": { "id": 16, "name": "Epic Games Store" },
                                "price": { "amount": 599.00, "currency": "INR" }
                              },
                              {
                                "shop": { "id": 35, "name": "GOG" },
                                "price": { "amount": 499.00, "currency": "INR" }
                              },
                              {
                                "shop": { "id": 61, "name": "Steam" },
                                "price": { "amount": 699.00, "currency": "INR" }
                              }
                            ]
                          }
                        ]
                        """, APPLICATION_JSON));

        Map<String, Map<String, BigDecimal>> prices = service.getBatchPrices(List.of(itadId));

        assertThat(prices).containsKey(itadId);
        assertThat(prices.get(itadId).get("EPIC")).isEqualByComparingTo(new BigDecimal("599.00"));
        assertThat(prices.get(itadId).get("GOG")).isEqualByComparingTo(new BigDecimal("499.00"));
        assertThat(prices.get(itadId)).doesNotContainKey("STEAM");
        assertThat(rateLimiter.recordedApiName).isEqualTo("itad");
        server.verify();
    }

    private ItadApiService service(RestTemplate restTemplate, ApiRateLimiterService rateLimiter) {
        ItadApiService service = new ItadApiService(rateLimiter, restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(service, "itadApiKey", API_KEY);
        ReflectionTestUtils.setField(service, "itadApiBaseUrl", BASE_URL);
        return service;
    }

    private static class FakeRateLimiterService extends ApiRateLimiterService {
        private String recordedApiName;

        private FakeRateLimiterService() {
            super(null);
        }

        @Override
        public boolean canMakeOtherApiCall(String apiName) {
            return true;
        }

        @Override
        public void recordApiCall(String apiName) {
            this.recordedApiName = apiName;
        }
    }
}
