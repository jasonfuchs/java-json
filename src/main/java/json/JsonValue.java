package json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public sealed interface JsonValue {
    static final JsonNull NULL = new JsonNull();

    static JsonValue fromJava(Object bean) {
	return switch (bean) {
	case null -> NULL;

	case Boolean bool -> new JsonBoolean(bool);
	case Number num -> new JsonNumber(num.doubleValue());
	case String string -> new JsonString(string);

	case List<?> ls -> JsonArray.fromJava(ls);
	case Map<?, ?> map -> JsonObject.fromJava(map);

	case char[] arr -> JsonArray.fromJava(arr);

	case byte[] arr -> JsonArray.fromJava(arr);
	case short[] arr -> JsonArray.fromJava(arr);
	case int[] arr -> JsonArray.fromJava(arr);
	case long[] arr -> JsonArray.fromJava(arr);

	case float[] arr -> JsonArray.fromJava(arr);
	case double[] arr -> JsonArray.fromJava(arr);

	case boolean[] arr -> JsonArray.fromJava(arr);

	default -> JsonObject.fromJava(bean);
	};
    }

    default String toPrettyString() {
	return toString();
    }

    static final class JsonNull implements JsonValue {
	private JsonNull() {
	}

	@Override
	public String toString() {
	    return "null";
	}
    }

    record JsonBoolean(boolean value) implements JsonValue {
	@Override
	public String toString() {
	    return String.format("%b", value);
	}
    }

    record JsonNumber(double value) implements JsonValue {
	@Override
	public String toString() {
	    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}
    }

    record JsonString(String value) implements JsonValue {
	@Override
	public String toString() {
	    return String.format("\"%s\"", value);
	}
    }

    record JsonArray(List<JsonValue> values) implements JsonValue {
	public static JsonArray fromJava(List<?> values) {
	    return new JsonArray(values.stream().map(JsonValue::fromJava).toList());
	}

	public static <T> JsonArray fromJava(T[] values) {
	    List<T> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(byte[] values) {
	    List<Byte> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(short[] values) {
	    List<Short> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(int[] values) {
	    List<Integer> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(long[] values) {
	    List<Long> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(float[] values) {
	    List<Float> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(double[] values) {
	    List<Double> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(boolean[] values) {
	    List<Boolean> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	public static JsonArray fromJava(char[] values) {
	    List<Character> ls = new ArrayList<>();
	    for (var value : values) {
		ls.add(value);
	    }
	    return fromJava(ls);
	}

	@Override
	public String toString() {
	    List<String> values = this.values.stream().map(Object::toString).toList();
	    return String.format("[%s]", String.join(",", values));
	}

	@Override
	public String toPrettyString() {
	    List<String> values = this.values.stream().map(JsonValue::toPrettyString)
		    .map(value -> value.replaceAll("\n", "\n  ")).toList();
	    return values.isEmpty() ? "[]" : String.format("[\n  %s\n]", String.join(",\n  ", values));
	}
    }

    record JsonObject(Map<JsonString, JsonValue> values) implements JsonValue {
	static JsonObject fromJava(Object bean) {
	    Map<JsonString, JsonValue> values = new HashMap<>();
	    for (var clazz = bean.getClass(); clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
		var fields = clazz.getDeclaredFields();

		for (var field : fields) {
		    var accessible = field.canAccess(bean);

		    if (!accessible) {
			field.setAccessible(accessible);
		    }

		    try {
			var name = field.getName();
			var value = field.get(bean);

			if (values.get(new JsonString(name)) != null) {
			    name = clazz.getCanonicalName() + "." + name;
			}

			values.put(new JsonString(name), JsonValue.fromJava(value));

		    } catch (IllegalArgumentException | IllegalAccessException ignore) {
			// this shouldn't happen
		    } finally {
			field.setAccessible(accessible);
		    }
		}
	    }
	    return new JsonObject(values);
	}

	static JsonObject fromJava(Map<?, ?> bean) {
	    Map<JsonString, JsonValue> values = new HashMap<>();
	    for (var entry : values.entrySet()) {
		values.put(new JsonString(entry.getKey().toString()), JsonValue.fromJava(entry.getValue()));
	    }
	    return new JsonObject(values);
	}

	@Override
	public String toString() {
	    List<String> values = this.values.entrySet().stream()
		    .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue())).toList();
	    return String.format("{%s}", String.join(",", values));
	}

	@Override
	public String toPrettyString() {
	    List<String> values = this.values.entrySet().stream()
		    .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue().toPrettyString()))
		    .map(value -> value.replaceAll("\n", "\n  ")).toList();
	    return values.isEmpty() ? "{}" : String.format("{\n  %s\n}", String.join(",\n  ", values));
	}
    }
}
