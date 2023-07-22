// Generated from JianMuEl.g4 by ANTLR 4.9.1

package dev.jianmu.el.antlr4;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JianMuElLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		INT_LITERAL=1, FLOAT_LITERAL=2, BOOL_LITERAL=3, STRING_LITERAL=4, NULL_LITERAL=5, 
		TEMPLATE=6, LBRACK=7, RBRACK=8, LPAREN=9, RPAREN=10, PLUS=11, MINUS=12, 
		TIMES=13, DIV=14, MODULO=15, DOT=16, GT=17, GE=18, LT=19, LE=20, EQ=21, 
		NE=22, NOT=23, AND=24, OR=25, DollarString=26, OpenCurlyBracket=27, CloseCurlyBracket=28, 
		REVERSE_QUOTE_SYMB=29, WS=30, VARNAME=31;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"INT_LITERAL", "FLOAT_LITERAL", "BOOL_LITERAL", "STRING_LITERAL", "NULL_LITERAL", 
			"TEMPLATE", "LBRACK", "RBRACK", "LPAREN", "RPAREN", "PLUS", "MINUS", 
			"TIMES", "DIV", "MODULO", "DOT", "GT", "GE", "LT", "LE", "EQ", "NE", 
			"NOT", "AND", "OR", "DollarString", "OpenCurlyBracket", "CloseCurlyBracket", 
			"REVERSE_QUOTE_SYMB", "WS", "VARNAME", "EscapeSequence", "HexDigit", 
			"DIGIT", "LetterOrDigitOrDot", "Letter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, "'null'", null, "'['", "']'", "'('", "')'", 
			"'+'", "'-'", "'*'", "'/'", "'%'", "'.'", "'>'", "'>='", "'<'", "'<='", 
			"'=='", "'!='", "'!'", "'&&'", "'||'", "'$'", "'{'", "'}'", "'`'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "INT_LITERAL", "FLOAT_LITERAL", "BOOL_LITERAL", "STRING_LITERAL", 
			"NULL_LITERAL", "TEMPLATE", "LBRACK", "RBRACK", "LPAREN", "RPAREN", "PLUS", 
			"MINUS", "TIMES", "DIV", "MODULO", "DOT", "GT", "GE", "LT", "LE", "EQ", 
			"NE", "NOT", "AND", "OR", "DollarString", "OpenCurlyBracket", "CloseCurlyBracket", 
			"REVERSE_QUOTE_SYMB", "WS", "VARNAME"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JianMuElLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JianMuEl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2!\u00ec\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\6\2M\n\2\r\2\16\2N\3\3\6\3R\n\3\r\3"+
		"\16\3S\3\3\3\3\7\3X\n\3\f\3\16\3[\13\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\5\4f\n\4\3\5\3\5\3\5\7\5k\n\5\f\5\16\5n\13\5\3\5\3\5\3\6\3\6\3\6"+
		"\3\6\3\6\3\7\3\7\3\7\7\7z\n\7\f\7\16\7}\13\7\3\7\3\7\3\b\3\b\3\t\3\t\3"+
		"\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3"+
		"\21\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3"+
		"\27\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\34\3"+
		"\34\3\35\3\35\3\36\3\36\3\37\6\37\u00b6\n\37\r\37\16\37\u00b7\3\37\3\37"+
		"\3 \3 \3 \3 \7 \u00c0\n \f \16 \u00c3\13 \3 \3 \3!\3!\3!\3!\5!\u00cb\n"+
		"!\3!\5!\u00ce\n!\3!\3!\3!\6!\u00d3\n!\r!\16!\u00d4\3!\3!\3!\3!\3!\5!\u00dc"+
		"\n!\3\"\3\"\3#\3#\3$\3$\3$\5$\u00e5\n$\3%\3%\3%\3%\5%\u00eb\n%\2\2&\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37"+
		"\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37="+
		" ?!A\2C\2E\2G\2I\2\3\2\16\6\2\f\f\17\17$$^^\4\2\17\17^^\5\2\13\f\17\17"+
		"\"\"\n\2$$))^^ddhhppttvv\3\2\62\65\3\2\629\5\2\62;CHch\3\2\62;\6\2&&C"+
		"\\aac|\4\2\2\u0081\ud802\udc01\3\2\ud802\udc01\3\2\udc02\ue001\2\u00f9"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3"+
		"\2\2\2\2=\3\2\2\2\2?\3\2\2\2\3L\3\2\2\2\5Q\3\2\2\2\7e\3\2\2\2\tg\3\2\2"+
		"\2\13q\3\2\2\2\rv\3\2\2\2\17\u0080\3\2\2\2\21\u0082\3\2\2\2\23\u0084\3"+
		"\2\2\2\25\u0086\3\2\2\2\27\u0088\3\2\2\2\31\u008a\3\2\2\2\33\u008c\3\2"+
		"\2\2\35\u008e\3\2\2\2\37\u0090\3\2\2\2!\u0092\3\2\2\2#\u0094\3\2\2\2%"+
		"\u0096\3\2\2\2\'\u0099\3\2\2\2)\u009b\3\2\2\2+\u009e\3\2\2\2-\u00a1\3"+
		"\2\2\2/\u00a4\3\2\2\2\61\u00a6\3\2\2\2\63\u00a9\3\2\2\2\65\u00ac\3\2\2"+
		"\2\67\u00ae\3\2\2\29\u00b0\3\2\2\2;\u00b2\3\2\2\2=\u00b5\3\2\2\2?\u00bb"+
		"\3\2\2\2A\u00db\3\2\2\2C\u00dd\3\2\2\2E\u00df\3\2\2\2G\u00e4\3\2\2\2I"+
		"\u00ea\3\2\2\2KM\5E#\2LK\3\2\2\2MN\3\2\2\2NL\3\2\2\2NO\3\2\2\2O\4\3\2"+
		"\2\2PR\5E#\2QP\3\2\2\2RS\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TU\3\2\2\2UY\7\60"+
		"\2\2VX\5E#\2WV\3\2\2\2X[\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z\6\3\2\2\2[Y\3\2"+
		"\2\2\\]\7v\2\2]^\7t\2\2^_\7w\2\2_f\7g\2\2`a\7h\2\2ab\7c\2\2bc\7n\2\2c"+
		"d\7u\2\2df\7g\2\2e\\\3\2\2\2e`\3\2\2\2f\b\3\2\2\2gl\7$\2\2hk\n\2\2\2i"+
		"k\5A!\2jh\3\2\2\2ji\3\2\2\2kn\3\2\2\2lj\3\2\2\2lm\3\2\2\2mo\3\2\2\2nl"+
		"\3\2\2\2op\7$\2\2p\n\3\2\2\2qr\7p\2\2rs\7w\2\2st\7n\2\2tu\7n\2\2u\f\3"+
		"\2\2\2v{\7b\2\2wz\n\3\2\2xz\5A!\2yw\3\2\2\2yx\3\2\2\2z}\3\2\2\2{y\3\2"+
		"\2\2{|\3\2\2\2|~\3\2\2\2}{\3\2\2\2~\177\7b\2\2\177\16\3\2\2\2\u0080\u0081"+
		"\7]\2\2\u0081\20\3\2\2\2\u0082\u0083\7_\2\2\u0083\22\3\2\2\2\u0084\u0085"+
		"\7*\2\2\u0085\24\3\2\2\2\u0086\u0087\7+\2\2\u0087\26\3\2\2\2\u0088\u0089"+
		"\7-\2\2\u0089\30\3\2\2\2\u008a\u008b\7/\2\2\u008b\32\3\2\2\2\u008c\u008d"+
		"\7,\2\2\u008d\34\3\2\2\2\u008e\u008f\7\61\2\2\u008f\36\3\2\2\2\u0090\u0091"+
		"\7\'\2\2\u0091 \3\2\2\2\u0092\u0093\7\60\2\2\u0093\"\3\2\2\2\u0094\u0095"+
		"\7@\2\2\u0095$\3\2\2\2\u0096\u0097\7@\2\2\u0097\u0098\7?\2\2\u0098&\3"+
		"\2\2\2\u0099\u009a\7>\2\2\u009a(\3\2\2\2\u009b\u009c\7>\2\2\u009c\u009d"+
		"\7?\2\2\u009d*\3\2\2\2\u009e\u009f\7?\2\2\u009f\u00a0\7?\2\2\u00a0,\3"+
		"\2\2\2\u00a1\u00a2\7#\2\2\u00a2\u00a3\7?\2\2\u00a3.\3\2\2\2\u00a4\u00a5"+
		"\7#\2\2\u00a5\60\3\2\2\2\u00a6\u00a7\7(\2\2\u00a7\u00a8\7(\2\2\u00a8\62"+
		"\3\2\2\2\u00a9\u00aa\7~\2\2\u00aa\u00ab\7~\2\2\u00ab\64\3\2\2\2\u00ac"+
		"\u00ad\7&\2\2\u00ad\66\3\2\2\2\u00ae\u00af\7}\2\2\u00af8\3\2\2\2\u00b0"+
		"\u00b1\7\177\2\2\u00b1:\3\2\2\2\u00b2\u00b3\7b\2\2\u00b3<\3\2\2\2\u00b4"+
		"\u00b6\t\4\2\2\u00b5\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b5\3\2"+
		"\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00ba\b\37\2\2\u00ba"+
		">\3\2\2\2\u00bb\u00bc\5\65\33\2\u00bc\u00bd\5\67\34\2\u00bd\u00c1\5I%"+
		"\2\u00be\u00c0\5G$\2\u00bf\u00be\3\2\2\2\u00c0\u00c3\3\2\2\2\u00c1\u00bf"+
		"\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4"+
		"\u00c5\59\35\2\u00c5@\3\2\2\2\u00c6\u00c7\7^\2\2\u00c7\u00dc\t\5\2\2\u00c8"+
		"\u00cd\7^\2\2\u00c9\u00cb\t\6\2\2\u00ca\u00c9\3\2\2\2\u00ca\u00cb\3\2"+
		"\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ce\t\7\2\2\u00cd\u00ca\3\2\2\2\u00cd"+
		"\u00ce\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00dc\t\7\2\2\u00d0\u00d2\7^"+
		"\2\2\u00d1\u00d3\7w\2\2\u00d2\u00d1\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4"+
		"\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\5C"+
		"\"\2\u00d7\u00d8\5C\"\2\u00d8\u00d9\5C\"\2\u00d9\u00da\5C\"\2\u00da\u00dc"+
		"\3\2\2\2\u00db\u00c6\3\2\2\2\u00db\u00c8\3\2\2\2\u00db\u00d0\3\2\2\2\u00dc"+
		"B\3\2\2\2\u00dd\u00de\t\b\2\2\u00deD\3\2\2\2\u00df\u00e0\t\t\2\2\u00e0"+
		"F\3\2\2\2\u00e1\u00e5\5I%\2\u00e2\u00e5\5!\21\2\u00e3\u00e5\t\t\2\2\u00e4"+
		"\u00e1\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e3\3\2\2\2\u00e5H\3\2\2\2"+
		"\u00e6\u00eb\t\n\2\2\u00e7\u00eb\n\13\2\2\u00e8\u00e9\t\f\2\2\u00e9\u00eb"+
		"\t\r\2\2\u00ea\u00e6\3\2\2\2\u00ea\u00e7\3\2\2\2\u00ea\u00e8\3\2\2\2\u00eb"+
		"J\3\2\2\2\23\2NSYejly{\u00b7\u00c1\u00ca\u00cd\u00d4\u00db\u00e4\u00ea"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}