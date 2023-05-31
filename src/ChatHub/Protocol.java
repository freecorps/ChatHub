package ChatHub;

import java.util.HashMap;
import java.util.Map;

public class Protocol {
    public static String encodeMessage(Map<String, String> message) {
    StringBuilder encodedMessage = new StringBuilder();

    for (Map.Entry<String, String> entry : message.entrySet()) {
        encodedMessage.append(entry.getKey());
        encodedMessage.append("=");
        encodedMessage.append(entry.getValue());
        encodedMessage.append(";");
    }

    return encodedMessage.toString();
    }
    
    public static Map<String, String> decodeMessage(String encodedMessage) {
    Map<String, String> message = new HashMap<>();

    String[] keyValuePairs = encodedMessage.split(";");

    for (String keyValuePair : keyValuePairs) {
        String[] keyValue = keyValuePair.split("=", 2);
        if (keyValue.length == 2) {
            message.put(keyValue[0], keyValue[1]);
        }
    }

    return message;
    }
}
