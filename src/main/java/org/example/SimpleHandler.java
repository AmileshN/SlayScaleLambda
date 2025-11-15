package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleHandler implements RequestHandler<LambdaRequest, LambdaResponse> {

    private static final String BASE_URL = "https://slayscale.azurewebsites.net/api";

    @Override
    public LambdaResponse handleRequest(LambdaRequest r, Context context) {

        HttpClient client = HttpClient.newHttpClient();

        // ---------------------------
        // 1. CREATE USERS
        // ---------------------------
        String[] users = {
                "{\"username\": \"amilesh\"}",
                "{\"username\": \"tim\"}",
                "{\"username\": \"randy\"}"
        };

        for (int i = 0; i < users.length; i++) {
            post(client, BASE_URL + "/users", users[i], context, "Create User " + (i+1));
        }


        // ---------------------------
        // 2. CREATE PRODUCTS
        // ---------------------------
        String[] products = {
                "{\"url\": \"http://example.com/p1\", \"category\": \"ELECTRONICS\"}",
                "{\"url\": \"http://example.com/p2\", \"category\": \"BOOKS\"}",
                "{\"url\": \"http://example.com/p3\", \"category\": \"TOYS\"}"
        };

        for (int i = 0; i < products.length; i++) {
            post(client, BASE_URL + "/products", products[i], context, "Create Product " + (i+1));
        }


        // ---------------------------
        // 3. ADD REVIEWS
        // User 1 → Product 1
        // User 2 → Products 1 & 2
        // User 3 → Products 1, 2 & 3
        // ---------------------------

        // Reviews for each user
        Map<Integer, List<String>> reviewsMap = new HashMap<>();

        // User 1
        reviewsMap.put(1, List.of(
                jsonReview(1, 5, "Great product!")
        ));

        // User 2
        reviewsMap.put(2, List.of(
                jsonReview(1, 3, "Pretty good."),
                jsonReview(2, 4, "Nice one!")
        ));

        // User 3
        reviewsMap.put(3, List.of(
                jsonReview(1, 2, "Okay..."),
                jsonReview(2, 4, "Liked it!"),
                jsonReview(3, 5, "Amazing!")
        ));

        // Send all review POST requests
        reviewsMap.forEach((userId, reviewList) -> {
            for (String json : reviewList) {
                post(client,
                        BASE_URL + "/users/" + userId + "/review",
                        json,
                        context,
                        "Add Review for User " + userId);
            }
        });


        return new LambdaResponse("Completed!");
    }


    // ------------------------------------------------------------------
    // Helper: Build review JSON
    // ------------------------------------------------------------------
    private String jsonReview(int productId, int rating, String text) {
        return "{"
                + "\"productId\": " + productId + ","
                + "\"rating\": " + rating + ","
                + "\"text\": \"" + text + "\""
                + "}";
    }

    // ------------------------------------------------------------------
    // Helper: Send POST requests safely
    // ------------------------------------------------------------------
    private void post(HttpClient client, String url, String json, Context ctx, String label) {
        try {
            ctx.getLogger().log("Sending (" + label + "): " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ctx.getLogger().log("Response (" + label + "): " + response.body());
        }
        catch (Exception e) {
            throw new RuntimeException("Error during " + label + ": " + e.getMessage(), e);
        }
    }
}
