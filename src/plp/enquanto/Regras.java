package plp.enquanto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.ParagraphView;

import plp.enquanto.Linguagem.*;
import plp.enquanto.parser.EnquantoBaseListener;
import plp.enquanto.parser.EnquantoParser.*;

import static java.lang.Integer.parseInt;

public class Regras extends EnquantoBaseListener {
	private final Leia leia;
	private final Skip skip;
	private final Propriedades valores;

	private Programa programa;

	public Regras() {
		leia = new Leia();
		skip = new Skip();
		valores = new Propriedades();
	}

	public Programa getPrograma() {
		return programa;
	}

	@Override
	public void exitBool(BoolContext ctx) {
		valores.insira(ctx, new Booleano("verdadeiro".equals(ctx.getText())));
	}

	@Override
	public void exitLeia(LeiaContext ctx) {
		valores.insira(ctx, leia);
	}
	
	@Override
    public void exitSe(SeContext ctx) {
        final Bool condicao = valores.pegue(ctx.booleano(0));

        final Comando entao = valores.pegue(ctx.comando(0));
        final Comando senao = valores.pegue(ctx.comando(ctx.booleano().size()));
        
        final Map<Comando, Bool> seEntaoLista = new HashMap<Comando, Bool>();

        if (ctx.booleano().size() > 1) {
            for (int i = 1; i < ctx.booleano().size(); i++) {
                final Bool _condicao = valores.pegue(ctx.booleano(i));
                final Comando _comando = valores.pegue(ctx.comando(i));

                seEntaoLista.put(_comando, _condicao);             
            }
        }

        valores.insira(ctx, new Se(condicao, entao, senao, seEntaoLista));
    }
	
	@Override
	public void exitSwitch(SwitchContext ctx) {
	
		final Expressao resultado = valores.pegue(ctx.expressao(0));

        final Comando outro = valores.pegue(ctx.comando(ctx.comando().size() - 1));
        
        final Map<Integer, Comando> casoLista = new HashMap<Integer, Comando>();

        if (ctx.comando().size() > 1) {
            for (int i = 0; i < ctx.comando().size() - 1; i++) {
                final Expressao _caso = valores.pegue(ctx.expressao(i + 1));
                final Comando _comando = valores.pegue(ctx.comando(i));

                casoLista.put(_caso.getValor(), _comando);             
            }
        }

        valores.insira(ctx, new Switch(resultado, outro, casoLista));
	}
	
	@Override
	public void exitFuncao(FuncaoContext ctx) {
		final List<Id> parametros = new ArrayList<Id>();
		super.exitFuncao(ctx);
	}

	@Override
	public void exitInteiro(InteiroContext ctx) {
		valores.insira(ctx, new Inteiro(parseInt(ctx.getText())));
	}

	@Override
	public void exitSkip(SkipContext ctx) {
		valores.insira(ctx, skip);
	}

	@Override
	public void exitEscreva(EscrevaContext ctx) {
		final Expressao exp = valores.pegue(ctx.expressao());
		valores.insira(ctx, new Escreva(exp));
	}

	@Override
	public void exitPrograma(ProgramaContext ctx) {
		final List<Comando> cmds = valores.pegue(ctx.seqComando());
		programa = new Programa(cmds);
		valores.insira(ctx, programa);
	}

	@Override
	public void exitId(IdContext ctx) {
		final String id = ctx.ID().getText();
		valores.insira(ctx, new Id(id));
	}

	@Override
	public void exitSeqComando(SeqComandoContext ctx) {
		final List<Comando> comandos = new ArrayList<>();
		for (ComandoContext c : ctx.comando()) {
			comandos.add(valores.pegue(c));
		}
		valores.insira(ctx, comandos);
	}

	@Override
	public void exitAtribuicao(AtribuicaoContext ctx) {
		final String id = ctx.ID().getText();
		final Expressao exp = valores.pegue(ctx.expressao());
		valores.insira(ctx, new Atribuicao(id, exp));
	}
	
	@Override
	public void exitAtribuicaoParalela(AtribuicaoParalelaContext ctx) {
		final Map<String, Expressao> atribuicaoLista = new HashMap<String, Expressao>();
		
		int numeroParametros = ctx.parametros().children.size();

		if (numeroParametros > 1) {
			for (int i = 0; i < numeroParametros; i++) {
				final String _parametro = ctx.parametros().getChild(i).getText();
				final Expressao _valor = valores.pegue(ctx.valores().getChild(i));

				atribuicaoLista.put(_parametro,  _valor);
			}
		}
		valores.insira(ctx, new AtribuicaoParalela(atribuicaoLista));

	}

	@Override
	public void exitBloco(BlocoContext ctx) {
		final List<Comando> cmds = valores.pegue(ctx.seqComando());
		valores.insira(ctx, new Bloco(cmds));
	}

	@Override
	public void exitOpBin(OpBinContext ctx) {
		final Expressao esq = valores.pegue(ctx.expressao(0));
		final Expressao dir = valores.pegue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final Expressao exp;	
		
		if (op.equals("^")) {
			 exp = new ExpExpon(esq, dir);
		} else if (op.equals("*")) {
			 exp = new ExpMult(esq, dir);
		} else if (op.equals("/")) {
			 exp = new ExpDiv(esq, dir);
		} else if (op.equals("-")) {
			 exp = new ExpSub(esq, dir);
		} else {
			 exp =  new ExpSoma(esq, dir);
		}
		valores.insira(ctx, exp);
	}

	@Override
	public void exitEnquanto(EnquantoContext ctx) {
		final Bool condicao = valores.pegue(ctx.booleano());
		final Comando comando = valores.pegue(ctx.comando());
		valores.insira(ctx, new Enquanto(condicao, comando));
	}
	
	@Override
	public void exitPara(ParaContext ctx) {
		final String id = ctx.ID().getText();
		final Expressao inicio = valores.pegue(ctx.expressao(0));
		final Expressao fim = valores.pegue(ctx.expressao(1));
		final Expressao passo = valores.pegue(ctx.expressao(2));
		final Comando comando = valores.pegue(ctx.comando());
		
		valores.insira(ctx, new Para(id,inicio,fim,passo,comando));
		super.exitPara(ctx);
	}

	@Override
	public void exitELogico(ELogicoContext ctx) {
		final Bool esq = valores.pegue(ctx.booleano(0));
		final Bool dir = valores.pegue(ctx.booleano(1));
		valores.insira(ctx, new ELogico(esq, dir));
	}
	
	@Override
	public void exitOuLogico(OuLogicoContext ctx) {
		final Bool esq = valores.pegue(ctx.booleano(0));
		final Bool dir = valores.pegue(ctx.booleano(1));
		valores.insira(ctx, new OuLogico(esq, dir));
	}
	
	@Override
	public void exitXorLogico(XorLogicoContext ctx) {
		final Bool esq = valores.pegue(ctx.booleano(0));
		final Bool dir = valores.pegue(ctx.booleano(1));
		valores.insira(ctx, new XorLogico(esq, dir));
	}

	@Override
	public void exitBoolPar(BoolParContext ctx) {
		final Bool booleano = valores.pegue(ctx.booleano());
		valores.insira(ctx, booleano);
	}

	@Override
	public void exitNaoLogico(NaoLogicoContext ctx) {
		final Bool b = valores.pegue(ctx.booleano());
		valores.insira(ctx, new NaoLogico(b));
	}

	@Override
	public void exitExpPar(ExpParContext ctx) {
		final Expressao exp = valores.pegue(ctx.expressao());
		valores.insira(ctx, exp);
	}

	@Override
	public void exitExiba(ExibaContext ctx) {
		final String t = ctx.TEXTO().getText();
		final String texto = t.substring(1, t.length() - 1);
		valores.insira(ctx, new Exiba(texto));
	}

	@Override
	public void exitOpRel(OpRelContext ctx) {
		final Expressao esq = valores.pegue(ctx.expressao(0));
		final Expressao dir = valores.pegue(ctx.expressao(1));
		final String op = ctx.getChild(1).getText();
		final Bool exp;
		
		if (op.equals("=")) {
			exp = new ExpIgual(esq, dir);
		} else if (op.equals( "<=")) {
			exp = new ExpMenorIgual(esq, dir);
		} else if (op.equals( ">=")) {
			exp = new ExpMaiorIgual(esq, dir);
		} else if (op.equals( "<")) {
			exp = new ExpMenor(esq, dir);
		} else if (op.equals( ">")) {
				exp = new ExpMaior(esq, dir);
		} else {
			exp = new ExpDiferente(esq, esq);
		}
	
		valores.insira(ctx, exp);
	}
}
