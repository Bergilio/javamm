// Generated from /home/hugora/Documentos/Estudo/Comp/comp2026-1d/src/main/antlr/pt/up/fe/comp2026/Javamm.g4 by ANTLR 4.13.1

    package pt.up.fe.comp2026;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class JavammParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, PACKAGE=19, IMPORT=20, CLASS=21, EXTENDS=22, PUBLIC=23, STATIC=24, 
		RETURN=25, INT=26, BOOLEAN=27, VOID=28, IF=29, ELSE=30, WHILE=31, LENGTH=32, 
		NEW=33, THIS=34, INTEGER=35, BOOL=36, ID=37, WS=38, SINGLE_COMMENT=39, 
		BLOCK_COMMENT=40;
	public static final int
		RULE_program = 0, RULE_packageDecl = 1, RULE_importDecl = 2, RULE_classDecl = 3, 
		RULE_varDecl = 4, RULE_param = 5, RULE_type = 6, RULE_methodDecl = 7, 
		RULE_stmt = 8, RULE_expr = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "packageDecl", "importDecl", "classDecl", "varDecl", "param", 
			"type", "methodDecl", "stmt", "expr"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "';'", "'{'", "'}'", "'['", "']'", "'('", "','", "')'", 
			"'='", "'!'", "'*'", "'/'", "'+'", "'-'", "'<'", "'&&'", "'||'", "'package'", 
			"'import'", "'class'", "'extends'", "'public'", "'static'", "'return'", 
			"'int'", "'boolean'", "'void'", "'if'", "'else'", "'while'", "'length'", 
			"'new'", "'this'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "PACKAGE", "IMPORT", "CLASS", 
			"EXTENDS", "PUBLIC", "STATIC", "RETURN", "INT", "BOOLEAN", "VOID", "IF", 
			"ELSE", "WHILE", "LENGTH", "NEW", "THIS", "INTEGER", "BOOL", "ID", "WS", 
			"SINGLE_COMMENT", "BLOCK_COMMENT"
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

	@Override
	public String getGrammarFileName() { return "Javamm.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JavammParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public ClassDeclContext classNode;
		public PackageDeclContext packageDecl() {
			return getRuleContext(PackageDeclContext.class,0);
		}
		public TerminalNode EOF() { return getToken(JavammParser.EOF, 0); }
		public ClassDeclContext classDecl() {
			return getRuleContext(ClassDeclContext.class,0);
		}
		public List<ImportDeclContext> importDecl() {
			return getRuleContexts(ImportDeclContext.class);
		}
		public ImportDeclContext importDecl(int i) {
			return getRuleContext(ImportDeclContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
			packageDecl();
			setState(24);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(21);
				importDecl();
				}
				}
				setState(26);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(27);
			((ProgramContext)_localctx).classNode = classDecl();
			setState(28);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PackageDeclContext extends ParserRuleContext {
		public Token ID;
		public List<Token> path = new ArrayList<Token>();
		public TerminalNode PACKAGE() { return getToken(JavammParser.PACKAGE, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public PackageDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDecl; }
	}

	public final PackageDeclContext packageDecl() throws RecognitionException {
		PackageDeclContext _localctx = new PackageDeclContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_packageDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(PACKAGE);
			setState(31);
			((PackageDeclContext)_localctx).ID = match(ID);
			((PackageDeclContext)_localctx).path.add(((PackageDeclContext)_localctx).ID);
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(32);
				match(T__0);
				setState(33);
				((PackageDeclContext)_localctx).ID = match(ID);
				((PackageDeclContext)_localctx).path.add(((PackageDeclContext)_localctx).ID);
				}
				}
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(39);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclContext extends ParserRuleContext {
		public Token ID;
		public List<Token> path = new ArrayList<Token>();
		public TerminalNode IMPORT() { return getToken(JavammParser.IMPORT, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public ImportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDecl; }
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			match(IMPORT);
			setState(42);
			((ImportDeclContext)_localctx).ID = match(ID);
			((ImportDeclContext)_localctx).path.add(((ImportDeclContext)_localctx).ID);
			setState(45); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(43);
				match(T__0);
				setState(44);
				((ImportDeclContext)_localctx).ID = match(ID);
				((ImportDeclContext)_localctx).path.add(((ImportDeclContext)_localctx).ID);
				}
				}
				setState(47); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__0 );
			setState(49);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassDeclContext extends ParserRuleContext {
		public Token name;
		public Token extension;
		public TerminalNode CLASS() { return getToken(JavammParser.CLASS, 0); }
		public List<TerminalNode> ID() { return getTokens(JavammParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(JavammParser.ID, i);
		}
		public TerminalNode EXTENDS() { return getToken(JavammParser.EXTENDS, 0); }
		public List<VarDeclContext> varDecl() {
			return getRuleContexts(VarDeclContext.class);
		}
		public VarDeclContext varDecl(int i) {
			return getRuleContext(VarDeclContext.class,i);
		}
		public List<MethodDeclContext> methodDecl() {
			return getRuleContexts(MethodDeclContext.class);
		}
		public MethodDeclContext methodDecl(int i) {
			return getRuleContext(MethodDeclContext.class,i);
		}
		public ClassDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDecl; }
	}

	public final ClassDeclContext classDecl() throws RecognitionException {
		ClassDeclContext _localctx = new ClassDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_classDecl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(CLASS);
			setState(52);
			((ClassDeclContext)_localctx).name = match(ID);
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(53);
				match(EXTENDS);
				setState(54);
				((ClassDeclContext)_localctx).extension = match(ID);
				}
			}

			setState(57);
			match(T__2);
			setState(61);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(58);
					varDecl();
					}
					} 
				}
				setState(63);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 137933881344L) != 0)) {
				{
				{
				setState(64);
				methodDecl();
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(70);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VarDeclContext extends ParserRuleContext {
		public TypeContext typeNode;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			((VarDeclContext)_localctx).typeNode = type();
			setState(73);
			((VarDeclContext)_localctx).name = match(ID);
			setState(74);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public TypeContext typeNode;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param; }
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			((ParamContext)_localctx).typeNode = type();
			setState(77);
			((ParamContext)_localctx).name = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public Token name;
		public TerminalNode INT() { return getToken(JavammParser.INT, 0); }
		public TerminalNode BOOLEAN() { return getToken(JavammParser.BOOLEAN, 0); }
		public TerminalNode VOID() { return getToken(JavammParser.VOID, 0); }
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			((TypeContext)_localctx).name = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 137908715520L) != 0)) ) {
				((TypeContext)_localctx).name = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(82);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(80);
				match(T__4);
				setState(81);
				match(T__5);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodDeclContext extends ParserRuleContext {
		public boolean isStatic = false;
		public Token visibility;
		public TypeContext returnType;
		public Token name;
		public ParamContext param;
		public List<ParamContext> params = new ArrayList<ParamContext>();
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public TerminalNode STATIC() { return getToken(JavammParser.STATIC, 0); }
		public List<VarDeclContext> varDecl() {
			return getRuleContexts(VarDeclContext.class);
		}
		public VarDeclContext varDecl(int i) {
			return getRuleContext(VarDeclContext.class,i);
		}
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public TerminalNode PUBLIC() { return getToken(JavammParser.PUBLIC, 0); }
		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}
		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}
		public MethodDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDecl; }
	}

	public final MethodDeclContext methodDecl() throws RecognitionException {
		MethodDeclContext _localctx = new MethodDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_methodDecl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUBLIC) {
				{
				setState(84);
				((MethodDeclContext)_localctx).visibility = match(PUBLIC);
				}
			}

			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STATIC) {
				{
				setState(87);
				match(STATIC);
				((MethodDeclContext)_localctx).isStatic = true;
				}
			}

			setState(91);
			((MethodDeclContext)_localctx).returnType = type();
			setState(92);
			((MethodDeclContext)_localctx).name = match(ID);
			setState(93);
			match(T__6);
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 137908715520L) != 0)) {
				{
				setState(94);
				((MethodDeclContext)_localctx).param = param();
				((MethodDeclContext)_localctx).params.add(((MethodDeclContext)_localctx).param);
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__7) {
					{
					{
					setState(95);
					match(T__7);
					setState(96);
					((MethodDeclContext)_localctx).param = param();
					((MethodDeclContext)_localctx).params.add(((MethodDeclContext)_localctx).param);
					}
					}
					setState(101);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(104);
			match(T__8);
			setState(105);
			match(T__2);
			setState(109);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(106);
					varDecl();
					}
					} 
				}
				setState(111);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 269005883528L) != 0)) {
				{
				{
				setState(112);
				stmt();
				}
				}
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(118);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StmtContext extends ParserRuleContext {
		public StmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stmt; }
	 
		public StmtContext() { }
		public void copyFrom(StmtContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IfStmtContext extends StmtContext {
		public TerminalNode IF() { return getToken(JavammParser.IF, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(JavammParser.ELSE, 0); }
		public IfStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprStmtContext extends StmtContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WhileStmtContext extends StmtContext {
		public TerminalNode WHILE() { return getToken(JavammParser.WHILE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public StmtContext stmt() {
			return getRuleContext(StmtContext.class,0);
		}
		public WhileStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignStmtContext extends StmtContext {
		public Token var;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public AssignStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignArrayElementStmtContext extends StmtContext {
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public AssignArrayElementStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends StmtContext {
		public List<StmtContext> stmt() {
			return getRuleContexts(StmtContext.class);
		}
		public StmtContext stmt(int i) {
			return getRuleContext(StmtContext.class,i);
		}
		public BlockContext(StmtContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ReturnStmtContext extends StmtContext {
		public TerminalNode RETURN() { return getToken(JavammParser.RETURN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ReturnStmtContext(StmtContext ctx) { copyFrom(ctx); }
	}

	public final StmtContext stmt() throws RecognitionException {
		StmtContext _localctx = new StmtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_stmt);
		int _la;
		try {
			setState(163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				_localctx = new AssignStmtContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(120);
				((AssignStmtContext)_localctx).var = match(ID);
				setState(121);
				match(T__9);
				setState(122);
				expr(0);
				setState(123);
				match(T__1);
				}
				break;
			case 2:
				_localctx = new ReturnStmtContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				match(RETURN);
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 266287974528L) != 0)) {
					{
					setState(126);
					expr(0);
					}
				}

				setState(129);
				match(T__1);
				}
				break;
			case 3:
				_localctx = new BlockContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(130);
				match(T__2);
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 269005883528L) != 0)) {
					{
					{
					setState(131);
					stmt();
					}
					}
					setState(136);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(137);
				match(T__3);
				}
				break;
			case 4:
				_localctx = new IfStmtContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(138);
				match(IF);
				setState(139);
				match(T__6);
				setState(140);
				expr(0);
				setState(141);
				match(T__8);
				setState(142);
				stmt();
				setState(143);
				match(ELSE);
				setState(144);
				stmt();
				}
				break;
			case 5:
				_localctx = new WhileStmtContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(146);
				match(WHILE);
				setState(147);
				match(T__6);
				setState(148);
				expr(0);
				setState(149);
				match(T__8);
				setState(150);
				stmt();
				}
				break;
			case 6:
				_localctx = new ExprStmtContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(152);
				expr(0);
				setState(153);
				match(T__1);
				}
				break;
			case 7:
				_localctx = new AssignArrayElementStmtContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(155);
				match(ID);
				setState(156);
				match(T__4);
				setState(157);
				expr(0);
				setState(158);
				match(T__5);
				setState(159);
				match(T__9);
				setState(160);
				expr(0);
				setState(161);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LengthOperatorContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode LENGTH() { return getToken(JavammParser.LENGTH, 0); }
		public LengthOperatorContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenthesisContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParenthesisContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrayAcessContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArrayAcessContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanLiteralContext extends ExprContext {
		public Token value;
		public TerminalNode BOOL() { return getToken(JavammParser.BOOL, 0); }
		public BooleanLiteralContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VarRefExprContext extends ExprContext {
		public Token name;
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public VarRefExprContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public BinaryExprContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CurrentObjectContext extends ExprContext {
		public TerminalNode THIS() { return getToken(JavammParser.THIS, 0); }
		public CurrentObjectContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ExprContext {
		public Token func;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public FunctionCallContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ClassCreationContext extends ExprContext {
		public Token class_;
		public TerminalNode NEW() { return getToken(JavammParser.NEW, 0); }
		public TerminalNode ID() { return getToken(JavammParser.ID, 0); }
		public ClassCreationContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public UnaryExprContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrayAllocContext extends ExprContext {
		public TerminalNode NEW() { return getToken(JavammParser.NEW, 0); }
		public TerminalNode INT() { return getToken(JavammParser.INT, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ArrayAllocContext(ExprContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralContext extends ExprContext {
		public Token value;
		public TerminalNode INTEGER() { return getToken(JavammParser.INTEGER, 0); }
		public IntegerLiteralContext(ExprContext ctx) { copyFrom(ctx); }
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 18;
		enterRecursionRule(_localctx, 18, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				_localctx = new ParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(166);
				match(T__6);
				setState(167);
				expr(0);
				setState(168);
				match(T__8);
				}
				break;
			case 2:
				{
				_localctx = new ArrayAllocContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(170);
				match(NEW);
				setState(171);
				match(INT);
				setState(172);
				match(T__4);
				setState(173);
				expr(0);
				setState(174);
				match(T__5);
				}
				break;
			case 3:
				{
				_localctx = new ClassCreationContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(176);
				match(NEW);
				setState(177);
				((ClassCreationContext)_localctx).class_ = match(ID);
				setState(178);
				match(T__6);
				setState(179);
				match(T__8);
				}
				break;
			case 4:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(180);
				((UnaryExprContext)_localctx).op = match(T__10);
				setState(181);
				expr(10);
				}
				break;
			case 5:
				{
				_localctx = new IntegerLiteralContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(182);
				((IntegerLiteralContext)_localctx).value = match(INTEGER);
				}
				break;
			case 6:
				{
				_localctx = new BooleanLiteralContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(183);
				((BooleanLiteralContext)_localctx).value = match(BOOL);
				}
				break;
			case 7:
				{
				_localctx = new VarRefExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(184);
				((VarRefExprContext)_localctx).name = match(ID);
				}
				break;
			case 8:
				{
				_localctx = new CurrentObjectContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(185);
				match(THIS);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(228);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(226);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(188);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(189);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__11 || _la==T__12) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(190);
						expr(10);
						}
						break;
					case 2:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(191);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(192);
						((BinaryExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__13 || _la==T__14) ) {
							((BinaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(193);
						expr(9);
						}
						break;
					case 3:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(194);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(195);
						((BinaryExprContext)_localctx).op = match(T__15);
						setState(196);
						expr(8);
						}
						break;
					case 4:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(197);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(198);
						((BinaryExprContext)_localctx).op = match(T__16);
						setState(199);
						expr(7);
						}
						break;
					case 5:
						{
						_localctx = new BinaryExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(200);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(201);
						((BinaryExprContext)_localctx).op = match(T__17);
						setState(202);
						expr(6);
						}
						break;
					case 6:
						{
						_localctx = new ArrayAcessContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(203);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(204);
						match(T__4);
						setState(205);
						expr(0);
						setState(206);
						match(T__5);
						}
						break;
					case 7:
						{
						_localctx = new LengthOperatorContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(208);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(209);
						match(T__0);
						setState(210);
						match(LENGTH);
						}
						break;
					case 8:
						{
						_localctx = new FunctionCallContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(211);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(212);
						match(T__0);
						setState(213);
						((FunctionCallContext)_localctx).func = match(ID);
						setState(214);
						match(T__6);
						setState(223);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 266287974528L) != 0)) {
							{
							setState(215);
							expr(0);
							setState(220);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__7) {
								{
								{
								setState(216);
								match(T__7);
								setState(217);
								expr(0);
								}
								}
								setState(222);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(225);
						match(T__8);
						}
						break;
					}
					} 
				}
				setState(230);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 9:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 9);
		case 1:
			return precpred(_ctx, 8);
		case 2:
			return precpred(_ctx, 7);
		case 3:
			return precpred(_ctx, 6);
		case 4:
			return precpred(_ctx, 5);
		case 5:
			return precpred(_ctx, 15);
		case 6:
			return precpred(_ctx, 14);
		case 7:
			return precpred(_ctx, 13);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001(\u00e8\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0005\u0000\u0017\b"+
		"\u0000\n\u0000\f\u0000\u001a\t\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001#\b\u0001"+
		"\n\u0001\f\u0001&\t\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0004\u0002.\b\u0002\u000b\u0002\f\u0002/\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u00038\b\u0003\u0001\u0003\u0001\u0003\u0005\u0003<\b\u0003\n\u0003\f"+
		"\u0003?\t\u0003\u0001\u0003\u0005\u0003B\b\u0003\n\u0003\f\u0003E\t\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006S\b\u0006\u0001\u0007\u0003\u0007V\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007Z\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007b\b\u0007\n\u0007\f\u0007e\t"+
		"\u0007\u0003\u0007g\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005"+
		"\u0007l\b\u0007\n\u0007\f\u0007o\t\u0007\u0001\u0007\u0005\u0007r\b\u0007"+
		"\n\u0007\f\u0007u\t\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u0080\b\b\u0001\b\u0001\b\u0001"+
		"\b\u0005\b\u0085\b\b\n\b\f\b\u0088\t\b\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u00a4\b\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u00bb\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u00db\b\t\n\t\f\t\u00de"+
		"\t\t\u0003\t\u00e0\b\t\u0001\t\u0005\t\u00e3\b\t\n\t\f\t\u00e6\t\t\u0001"+
		"\t\u0000\u0001\u0012\n\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0000\u0003\u0002\u0000\u001a\u001c%%\u0001\u0000\f\r\u0001\u0000\u000e"+
		"\u000f\u0103\u0000\u0014\u0001\u0000\u0000\u0000\u0002\u001e\u0001\u0000"+
		"\u0000\u0000\u0004)\u0001\u0000\u0000\u0000\u00063\u0001\u0000\u0000\u0000"+
		"\bH\u0001\u0000\u0000\u0000\nL\u0001\u0000\u0000\u0000\fO\u0001\u0000"+
		"\u0000\u0000\u000eU\u0001\u0000\u0000\u0000\u0010\u00a3\u0001\u0000\u0000"+
		"\u0000\u0012\u00ba\u0001\u0000\u0000\u0000\u0014\u0018\u0003\u0002\u0001"+
		"\u0000\u0015\u0017\u0003\u0004\u0002\u0000\u0016\u0015\u0001\u0000\u0000"+
		"\u0000\u0017\u001a\u0001\u0000\u0000\u0000\u0018\u0016\u0001\u0000\u0000"+
		"\u0000\u0018\u0019\u0001\u0000\u0000\u0000\u0019\u001b\u0001\u0000\u0000"+
		"\u0000\u001a\u0018\u0001\u0000\u0000\u0000\u001b\u001c\u0003\u0006\u0003"+
		"\u0000\u001c\u001d\u0005\u0000\u0000\u0001\u001d\u0001\u0001\u0000\u0000"+
		"\u0000\u001e\u001f\u0005\u0013\u0000\u0000\u001f$\u0005%\u0000\u0000 "+
		"!\u0005\u0001\u0000\u0000!#\u0005%\u0000\u0000\" \u0001\u0000\u0000\u0000"+
		"#&\u0001\u0000\u0000\u0000$\"\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000"+
		"\u0000%\'\u0001\u0000\u0000\u0000&$\u0001\u0000\u0000\u0000\'(\u0005\u0002"+
		"\u0000\u0000(\u0003\u0001\u0000\u0000\u0000)*\u0005\u0014\u0000\u0000"+
		"*-\u0005%\u0000\u0000+,\u0005\u0001\u0000\u0000,.\u0005%\u0000\u0000-"+
		"+\u0001\u0000\u0000\u0000./\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000"+
		"\u0000/0\u0001\u0000\u0000\u000001\u0001\u0000\u0000\u000012\u0005\u0002"+
		"\u0000\u00002\u0005\u0001\u0000\u0000\u000034\u0005\u0015\u0000\u0000"+
		"47\u0005%\u0000\u000056\u0005\u0016\u0000\u000068\u0005%\u0000\u00007"+
		"5\u0001\u0000\u0000\u000078\u0001\u0000\u0000\u000089\u0001\u0000\u0000"+
		"\u00009=\u0005\u0003\u0000\u0000:<\u0003\b\u0004\u0000;:\u0001\u0000\u0000"+
		"\u0000<?\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000"+
		"\u0000\u0000>C\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000@B\u0003"+
		"\u000e\u0007\u0000A@\u0001\u0000\u0000\u0000BE\u0001\u0000\u0000\u0000"+
		"CA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DF\u0001\u0000\u0000"+
		"\u0000EC\u0001\u0000\u0000\u0000FG\u0005\u0004\u0000\u0000G\u0007\u0001"+
		"\u0000\u0000\u0000HI\u0003\f\u0006\u0000IJ\u0005%\u0000\u0000JK\u0005"+
		"\u0002\u0000\u0000K\t\u0001\u0000\u0000\u0000LM\u0003\f\u0006\u0000MN"+
		"\u0005%\u0000\u0000N\u000b\u0001\u0000\u0000\u0000OR\u0007\u0000\u0000"+
		"\u0000PQ\u0005\u0005\u0000\u0000QS\u0005\u0006\u0000\u0000RP\u0001\u0000"+
		"\u0000\u0000RS\u0001\u0000\u0000\u0000S\r\u0001\u0000\u0000\u0000TV\u0005"+
		"\u0017\u0000\u0000UT\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000"+
		"VY\u0001\u0000\u0000\u0000WX\u0005\u0018\u0000\u0000XZ\u0006\u0007\uffff"+
		"\uffff\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000Z[\u0001"+
		"\u0000\u0000\u0000[\\\u0003\f\u0006\u0000\\]\u0005%\u0000\u0000]f\u0005"+
		"\u0007\u0000\u0000^c\u0003\n\u0005\u0000_`\u0005\b\u0000\u0000`b\u0003"+
		"\n\u0005\u0000a_\u0001\u0000\u0000\u0000be\u0001\u0000\u0000\u0000ca\u0001"+
		"\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000dg\u0001\u0000\u0000\u0000"+
		"ec\u0001\u0000\u0000\u0000f^\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000"+
		"\u0000gh\u0001\u0000\u0000\u0000hi\u0005\t\u0000\u0000im\u0005\u0003\u0000"+
		"\u0000jl\u0003\b\u0004\u0000kj\u0001\u0000\u0000\u0000lo\u0001\u0000\u0000"+
		"\u0000mk\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000ns\u0001\u0000"+
		"\u0000\u0000om\u0001\u0000\u0000\u0000pr\u0003\u0010\b\u0000qp\u0001\u0000"+
		"\u0000\u0000ru\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000st\u0001"+
		"\u0000\u0000\u0000tv\u0001\u0000\u0000\u0000us\u0001\u0000\u0000\u0000"+
		"vw\u0005\u0004\u0000\u0000w\u000f\u0001\u0000\u0000\u0000xy\u0005%\u0000"+
		"\u0000yz\u0005\n\u0000\u0000z{\u0003\u0012\t\u0000{|\u0005\u0002\u0000"+
		"\u0000|\u00a4\u0001\u0000\u0000\u0000}\u007f\u0005\u0019\u0000\u0000~"+
		"\u0080\u0003\u0012\t\u0000\u007f~\u0001\u0000\u0000\u0000\u007f\u0080"+
		"\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000\u0000\u0081\u00a4"+
		"\u0005\u0002\u0000\u0000\u0082\u0086\u0005\u0003\u0000\u0000\u0083\u0085"+
		"\u0003\u0010\b\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0085\u0088\u0001"+
		"\u0000\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086\u0087\u0001"+
		"\u0000\u0000\u0000\u0087\u0089\u0001\u0000\u0000\u0000\u0088\u0086\u0001"+
		"\u0000\u0000\u0000\u0089\u00a4\u0005\u0004\u0000\u0000\u008a\u008b\u0005"+
		"\u001d\u0000\u0000\u008b\u008c\u0005\u0007\u0000\u0000\u008c\u008d\u0003"+
		"\u0012\t\u0000\u008d\u008e\u0005\t\u0000\u0000\u008e\u008f\u0003\u0010"+
		"\b\u0000\u008f\u0090\u0005\u001e\u0000\u0000\u0090\u0091\u0003\u0010\b"+
		"\u0000\u0091\u00a4\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u001f\u0000"+
		"\u0000\u0093\u0094\u0005\u0007\u0000\u0000\u0094\u0095\u0003\u0012\t\u0000"+
		"\u0095\u0096\u0005\t\u0000\u0000\u0096\u0097\u0003\u0010\b\u0000\u0097"+
		"\u00a4\u0001\u0000\u0000\u0000\u0098\u0099\u0003\u0012\t\u0000\u0099\u009a"+
		"\u0005\u0002\u0000\u0000\u009a\u00a4\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u0005%\u0000\u0000\u009c\u009d\u0005\u0005\u0000\u0000\u009d\u009e\u0003"+
		"\u0012\t\u0000\u009e\u009f\u0005\u0006\u0000\u0000\u009f\u00a0\u0005\n"+
		"\u0000\u0000\u00a0\u00a1\u0003\u0012\t\u0000\u00a1\u00a2\u0005\u0002\u0000"+
		"\u0000\u00a2\u00a4\u0001\u0000\u0000\u0000\u00a3x\u0001\u0000\u0000\u0000"+
		"\u00a3}\u0001\u0000\u0000\u0000\u00a3\u0082\u0001\u0000\u0000\u0000\u00a3"+
		"\u008a\u0001\u0000\u0000\u0000\u00a3\u0092\u0001\u0000\u0000\u0000\u00a3"+
		"\u0098\u0001\u0000\u0000\u0000\u00a3\u009b\u0001\u0000\u0000\u0000\u00a4"+
		"\u0011\u0001\u0000\u0000\u0000\u00a5\u00a6\u0006\t\uffff\uffff\u0000\u00a6"+
		"\u00a7\u0005\u0007\u0000\u0000\u00a7\u00a8\u0003\u0012\t\u0000\u00a8\u00a9"+
		"\u0005\t\u0000\u0000\u00a9\u00bb\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005"+
		"!\u0000\u0000\u00ab\u00ac\u0005\u001a\u0000\u0000\u00ac\u00ad\u0005\u0005"+
		"\u0000\u0000\u00ad\u00ae\u0003\u0012\t\u0000\u00ae\u00af\u0005\u0006\u0000"+
		"\u0000\u00af\u00bb\u0001\u0000\u0000\u0000\u00b0\u00b1\u0005!\u0000\u0000"+
		"\u00b1\u00b2\u0005%\u0000\u0000\u00b2\u00b3\u0005\u0007\u0000\u0000\u00b3"+
		"\u00bb\u0005\t\u0000\u0000\u00b4\u00b5\u0005\u000b\u0000\u0000\u00b5\u00bb"+
		"\u0003\u0012\t\n\u00b6\u00bb\u0005#\u0000\u0000\u00b7\u00bb\u0005$\u0000"+
		"\u0000\u00b8\u00bb\u0005%\u0000\u0000\u00b9\u00bb\u0005\"\u0000\u0000"+
		"\u00ba\u00a5\u0001\u0000\u0000\u0000\u00ba\u00aa\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b0\u0001\u0000\u0000\u0000\u00ba\u00b4\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b6\u0001\u0000\u0000\u0000\u00ba\u00b7\u0001\u0000\u0000\u0000"+
		"\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba\u00b9\u0001\u0000\u0000\u0000"+
		"\u00bb\u00e4\u0001\u0000\u0000\u0000\u00bc\u00bd\n\t\u0000\u0000\u00bd"+
		"\u00be\u0007\u0001\u0000\u0000\u00be\u00e3\u0003\u0012\t\n\u00bf\u00c0"+
		"\n\b\u0000\u0000\u00c0\u00c1\u0007\u0002\u0000\u0000\u00c1\u00e3\u0003"+
		"\u0012\t\t\u00c2\u00c3\n\u0007\u0000\u0000\u00c3\u00c4\u0005\u0010\u0000"+
		"\u0000\u00c4\u00e3\u0003\u0012\t\b\u00c5\u00c6\n\u0006\u0000\u0000\u00c6"+
		"\u00c7\u0005\u0011\u0000\u0000\u00c7\u00e3\u0003\u0012\t\u0007\u00c8\u00c9"+
		"\n\u0005\u0000\u0000\u00c9\u00ca\u0005\u0012\u0000\u0000\u00ca\u00e3\u0003"+
		"\u0012\t\u0006\u00cb\u00cc\n\u000f\u0000\u0000\u00cc\u00cd\u0005\u0005"+
		"\u0000\u0000\u00cd\u00ce\u0003\u0012\t\u0000\u00ce\u00cf\u0005\u0006\u0000"+
		"\u0000\u00cf\u00e3\u0001\u0000\u0000\u0000\u00d0\u00d1\n\u000e\u0000\u0000"+
		"\u00d1\u00d2\u0005\u0001\u0000\u0000\u00d2\u00e3\u0005 \u0000\u0000\u00d3"+
		"\u00d4\n\r\u0000\u0000\u00d4\u00d5\u0005\u0001\u0000\u0000\u00d5\u00d6"+
		"\u0005%\u0000\u0000\u00d6\u00df\u0005\u0007\u0000\u0000\u00d7\u00dc\u0003"+
		"\u0012\t\u0000\u00d8\u00d9\u0005\b\u0000\u0000\u00d9\u00db\u0003\u0012"+
		"\t\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00db\u00de\u0001\u0000\u0000"+
		"\u0000\u00dc\u00da\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000"+
		"\u0000\u00dd\u00e0\u0001\u0000\u0000\u0000\u00de\u00dc\u0001\u0000\u0000"+
		"\u0000\u00df\u00d7\u0001\u0000\u0000\u0000\u00df\u00e0\u0001\u0000\u0000"+
		"\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1\u00e3\u0005\t\u0000\u0000"+
		"\u00e2\u00bc\u0001\u0000\u0000\u0000\u00e2\u00bf\u0001\u0000\u0000\u0000"+
		"\u00e2\u00c2\u0001\u0000\u0000\u0000\u00e2\u00c5\u0001\u0000\u0000\u0000"+
		"\u00e2\u00c8\u0001\u0000\u0000\u0000\u00e2\u00cb\u0001\u0000\u0000\u0000"+
		"\u00e2\u00d0\u0001\u0000\u0000\u0000\u00e2\u00d3\u0001\u0000\u0000\u0000"+
		"\u00e3\u00e6\u0001\u0000\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5\u0013\u0001\u0000\u0000\u0000"+
		"\u00e6\u00e4\u0001\u0000\u0000\u0000\u0015\u0018$/7=CRUYcfms\u007f\u0086"+
		"\u00a3\u00ba\u00dc\u00df\u00e2\u00e4";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}