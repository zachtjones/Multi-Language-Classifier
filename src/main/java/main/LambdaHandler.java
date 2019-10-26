package main;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import learners.Decider;
import learners.LanguageDecision;
import org.json.JSONException;
import org.json.JSONObject;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String body = request.getBody();
        System.out.println("Request body: " + body);

        APIGatewayProxyResponseEvent r = new APIGatewayProxyResponseEvent();
        r.setIsBase64Encoded(false);

        try {
            JSONObject bodyJson = new JSONObject(body);
            String phrase = bodyJson.getString("phrase");
            System.out.println("Called with: " + phrase);

            Decider decider = Decider.loadFromResources("learnerOut.dat");
            System.out.println("Loaded learner.");
            LanguageDecision decision = decider.decide(new InputRow(phrase));
            System.out.println("Analyzed the phrase submitted.");
            HashMap<String, Double> response = new HashMap<>();
            Learning.languages.forEach(
                language -> response.put(language, decision.confidenceForLanguage(language)));

            r.setStatusCode(200);
            r.setBody(getBody(response));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

            r.setStatusCode(500);
            r.setBody(e.getMessage());
        } catch (JSONException | ClassCastException e) {
            e.printStackTrace();

            r.setStatusCode(400);
            r.setBody("Please make sure the format is: {\"phrase\": \"example\"} in the request");
        }

        System.out.println("Responded with status: " + r.getStatusCode());
        return r;
    }

    private static String getBody(Map<String, Double> response) {
        String results = response.entrySet().stream().map(i -> String.format("\"%s\": %f", i.getKey(), i.getValue()))
            .collect(Collectors.joining(","));
        return "{" + results.replace("\"", "\\\"") + "}";
    }
}
