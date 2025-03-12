import json.JsonParser;

public class Main {
    public static void main(String[] args) throws Exception {
	var jsonValue = JsonParser.jsonValue().parse("42").orThrow();
	System.out.println(jsonValue);
    }
}
