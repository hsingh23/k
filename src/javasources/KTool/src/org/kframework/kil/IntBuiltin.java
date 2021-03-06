package org.kframework.kil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.kframework.kil.loader.Constants;
import org.kframework.kil.matchers.Matcher;
import org.kframework.kil.visitors.Transformer;
import org.kframework.kil.visitors.Visitor;
import org.kframework.kil.visitors.exceptions.TransformerException;
import org.w3c.dom.Element;

import aterm.ATermAppl;


/**
 * Class representing a builtin integer token.
 */
public class IntBuiltin extends Token {

	public static final String SORT_NAME = "#Int";

	/* Token cache */
	private static Map<BigInteger, IntBuiltin> tokenCache = new HashMap<BigInteger, IntBuiltin>();
	/* KApp cache */
	private static Map<BigInteger, KApp> kAppCache = new HashMap<BigInteger, KApp>();

	/**
	 * #token("#Int", "0")(.KList)
	 */
	public static final IntBuiltin ZERO_TOKEN = IntBuiltin.of(0);
	/**
	 * #token("#Int", "1")(.KList)
	 */
	public static final IntBuiltin ONE_TOKEN = IntBuiltin.of(1);

	/**
	 * #token("#Int", "0")(.KList)
	 */
	public static final KApp ZERO = IntBuiltin.kAppOf(0);
	/**
	 * #token("#Int", "1")(.KList)
	 */
	public static final KApp ONE = IntBuiltin.kAppOf(1);

	/**
	 * Returns a {@link IntBuiltin} representing the given {@link BigInteger} value.
	 * 
	 * @param value
	 * @return
	 */
	public static IntBuiltin of(BigInteger value) {
		assert value != null;

		IntBuiltin intBuiltin = tokenCache.get(value);
		if (intBuiltin == null) {
			intBuiltin = new IntBuiltin(value);
			tokenCache.put(value, intBuiltin);
		}
		return intBuiltin;
	}

	/**
	 * Returns a {@link IntBuiltin} representing the given {@link long} value.
	 * 
	 * @param value
	 * @return
	 */
	public static IntBuiltin of(long value) {
		return IntBuiltin.of(BigInteger.valueOf(value));
	}

	/**
	 * Returns a {@link IntBuiltin} representing a {@link BigInteger} with the given {@link String} representation.
	 * 
	 * @param value
	 * @return
	 */
	public static IntBuiltin of(String value) {
		assert value != null;

		return IntBuiltin.of(new BigInteger(value));
	}

	/**
	 * Returns a {@link KApp} representing a {@link IntBuiltin} with the given value applied to an empty {@link KList}.
	 * 
	 * @param value
	 * @return
	 */
	public static KApp kAppOf(BigInteger value) {
		assert value != null;

		KApp kApp = kAppCache.get(value);
		if (kApp == null) {
			kApp = KApp.of(IntBuiltin.of(value));
			kAppCache.put(value, kApp);
		}
		return kApp;
	}

	/**
	 * Returns a {@link KApp} representing a {@link IntBuiltin} with the given value applied to an empty {@link KList}.
	 * 
	 * @param value
	 * @return
	 */
	public static KApp kAppOf(long value) {
		return IntBuiltin.kAppOf(BigInteger.valueOf(value));
	}

	/**
	 * Returns a {@link KApp} representing a {@link IntBuiltin} with the given {@link String} representation applied to an empty {@link KList}.
	 * 
	 * @param value
	 * @return
	 */
	public static KApp kAppOf(String value) {
		assert value != null;

		return IntBuiltin.kAppOf(new BigInteger(value));
	}

	private final BigInteger value;

	private IntBuiltin(BigInteger value) {
		this.value = value;
	}

	protected IntBuiltin(Element element) {
		super(element);
		value = new BigInteger(element.getAttribute(Constants.VALUE_value_ATTR));
	}

	protected IntBuiltin(ATermAppl atm) {
		super(atm);
		value = new BigInteger(((ATermAppl) atm.getArgument(0)).getName());
	}

	/**
	 * Returns a {@link BigInteger} representing the (interpreted) value of the int token.
	 */
	public BigInteger bigIntegerValue() {
		return value;
	}

	/**
	 * Returns a {@link String} representing the sort name of a int token.
	 */
	@Override
	public String tokenSort() {
		return IntBuiltin.SORT_NAME;
	}

	/**
	 * Returns a {@link String} representing the (uninterpreted) value of the int token.
	 */
	@Override
	public String value() {
		return value.toString();
	}

	@Override
	public void accept(Matcher matcher, Term toMatch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ASTNode accept(Transformer transformer) throws TransformerException {
		return transformer.transform(this);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
