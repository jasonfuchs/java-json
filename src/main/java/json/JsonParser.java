package json;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import json.JsonValue.JsonBoolean;
import json.JsonValue.JsonNumber;

@FunctionalInterface
public interface JsonParser<T> {
    sealed interface Result<T> {
	T orThrow() throws JsonException;

	<U> Result<U> map(Function<? super T, ? extends U> mapper);

	Result<T> or(Supplier<? extends Result<? extends T>> other);

	record Success<T>(T parsedValue, String remainingInput) implements Result<T> {
	    @Override
	    public T orThrow() throws JsonException {
		return parsedValue;
	    }

	    @Override
	    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
		return new Success<>(mapper.apply(parsedValue), remainingInput);
	    }

	    @Override
	    public Result<T> or(Supplier<? extends Result<? extends T>> other) {
		return this;
	    }
	}

	record Failure<T>(JsonException exception) implements Result<T> {
	    @Override
	    public T orThrow() throws JsonException {
		throw exception;
	    }

	    @Override
	    @SuppressWarnings("unchecked")
	    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
		return (Result<U>) this;
	    }

	    @Override
	    @SuppressWarnings("unchecked")
	    public Result<T> or(Supplier<? extends Result<? extends T>> other) {
		// the compiler is to stupid
		return (Result<T>) other.get();
	    }
	}
    }

    Result<T> parse(String input);

    default <U> JsonParser<U> map(Function<? super T, ? extends U> mapper) {
	Objects.requireNonNull(mapper);
	return input -> parse(input).map(mapper);
    }

    default JsonParser<T> or(JsonParser<? extends T> other) {
	Objects.requireNonNull(other);
	return input -> parse(input).or(() -> other.parse(input));
    }

    static JsonParser<Character> parseIf(String description, Predicate<Character> predicate) {
	Objects.requireNonNull(description);
	Objects.requireNonNull(predicate);
	return input -> {
	    if (input.isEmpty()) {
		return new Result.Failure<>(new JsonException("expected " + description + ", reached end of string"));
	    }

	    char x = input.charAt(0);
	    String remainingInput = input.substring(1);

	    if (predicate.test(x)) {
		return new Result.Success<>(x, remainingInput);
	    }

	    return new Result.Failure<>(new JsonException("expected " + description + ", but found '" + x + "'"));
	};
    }

    static JsonParser<String> spanP(String description, Predicate<Character> predicate) {
	Objects.requireNonNull(description);
	Objects.requireNonNull(predicate);
	return input -> {
	    var buffer = new StringBuffer();
	    var parser = parseIf(description, predicate);

	    var result = parser.parse(input);
	    while (result instanceof Result.Success(var parsedInput, var remainingInput)) {
		input = remainingInput;
		buffer.append(parsedInput);
		result = parser.parse(input);
	    }

	    return new Result.Success<>(buffer.toString(), input);
	};
    }

    static JsonParser<Character> charP(char x) {
	return parseIf("'" + x + "'", y -> y.equals(x));
    }

    static JsonParser<String> stringP(String string) {
	Objects.requireNonNull(string);
	return input -> {
	    var buffer = new StringBuffer();

	    for (char c : string.toCharArray()) {
		var result = charP(c).parse(input);

		if (result instanceof Result.Failure(var __)) {
		    return new Result.Failure<>(
			    new JsonException("expeced \"" + string + "\", found \"" + buffer + "\""));
		}

		if (result instanceof Result.Success(var parsedInput, var remainingInput)) {
		    input = remainingInput;
		    buffer.append(parsedInput);
		}
	    }

	    return new Result.Success<>(buffer.toString(), input);
	};
    }

    static JsonParser<JsonValue> jsonNull() {
	return stringP("null").map(__ -> JsonValue.NULL);
    }

    static JsonParser<JsonValue> jsonTrue() {
	return stringP("true").map(__ -> new JsonBoolean(true));
    }

    static JsonParser<JsonValue> jsonFalse() {
	return stringP("false").map(__ -> new JsonBoolean(false));
    }

    static JsonParser<JsonValue> jsonBoolean() {
	return jsonTrue().or(jsonFalse());
    }

    static JsonParser<JsonValue> jsonNumber() {
	// TODO add decimal support
	return spanP("a number", Character::isDigit).map(parsedValue -> new JsonNumber(Double.valueOf(parsedValue)));
    }

    static JsonParser<JsonValue> jsonValue() {
	return jsonNull().or(jsonBoolean()).or(jsonNumber());
    }
}
