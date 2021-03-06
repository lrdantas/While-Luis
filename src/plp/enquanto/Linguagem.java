package plp.enquanto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import plp.enquanto.Linguagem.Bool;
import plp.enquanto.Linguagem.Comando;
import plp.enquanto.Linguagem.Expressao;

interface Linguagem {
	Map<String, Integer> ambiente = new HashMap<>();
	Scanner scanner = new Scanner(System.in);

	interface Bool {
		boolean getValor();
	}

	interface Comando {
		void execute();
	}

	interface Expressao {
		int getValor();
	}
	
	interface Parametro {
		int getValor();
	}

	/*
	  Comandos
	 */
	class Programa {
		private final List<Comando> comandos;
		public Programa(List<Comando> comandos) {
			this.comandos = comandos;
		}
		public void execute() {
			comandos.forEach(Comando::execute);
		}
	}

	class Se implements Comando {
		private final Bool condicao;
		private final Comando entao;
		private final Comando senao;
		private final Map<Comando, Bool> seEntaoLista;

		public Se(Bool condicao, Comando entao, Comando senao, Map<Comando, Bool> seEntaoLista) {
			this.condicao = condicao;
			this.entao = entao;
			this.senao = senao;
			this.seEntaoLista = seEntaoLista;
		}

		@Override
		public void execute() {
			Bool senaoSe = condicao;
			Comando _entao = entao;
			if (condicao.getValor())
				entao.execute();
			else if (!seEntaoLista.isEmpty()) {			
				for (Comando comando : seEntaoLista.keySet()) {
					senaoSe = seEntaoLista.get(comando);
					if (senaoSe.getValor()) {
						_entao = comando;
						break;
					}				
				}
				if (senaoSe.getValor())
					_entao.execute();
			} else {
				senao.execute();	
			}						
		}
	}
	
	class Switch implements Comando {
		private final Expressao resultado;
		private final Comando outro;
		private final Map<Integer, Comando> casoLista;
		
		public Switch(Expressao resultado,Comando outro, Map<Integer, Comando> casoLista) {
			this.outro = outro;
			this.casoLista = casoLista;
			this.resultado = resultado;
		}

		@Override
		public void execute() {
			Comando comando;
			if (casoLista.containsKey(resultado.getValor())) {
				comando = casoLista.get(resultado.getValor());
				comando.execute();
			} else {
				outro.execute();
			}
		}
	}

	Skip skip = new Skip();
	class Skip implements Comando {
		@Override
		public void execute() {}
	}

	class Escreva implements Comando {
		private final Expressao exp;

		public Escreva(Expressao exp) {
			this.exp = exp;
		}

		@Override
		public void execute() {
			System.out.println(exp.getValor());
		}
	}

	class Enquanto implements Comando {
		private final Bool condicao;
		private final Comando comando;

		public Enquanto(Bool condicao, Comando comando) {
			this.condicao = condicao;
			this.comando = comando;
		}

		@Override
		public void execute() {
			while (condicao.getValor()) {
				comando.execute();
			}
		}
	}
	
	class Para implements Comando {
		private final Comando comando;
		private final String id;
		private final Expressao valInicial;
		private final Expressao valFinal;
		private final Expressao valpasso;

		public Para( String id, Expressao valInicial, Expressao valFinal, Expressao valpasso, Comando comando) {
			this.id = id;
			this.valInicial= valInicial;
			this.valFinal = valFinal;
			this.valpasso = valpasso;
			this.comando = comando;
		}

		@Override
		public void execute() {			
			int inicio = valInicial.getValor();
			int fim = valFinal.getValor();
			int passo = valpasso.getValor() != 0 ? valpasso.getValor() : 1;
			
			while (inicio <= fim) {
				ambiente.put(id, Integer.valueOf(inicio));
				inicio = inicio + passo;
				comando.execute();
			}
		}
	}

	class Exiba implements Comando {
		private final String texto;

		public Exiba(String texto) {
			this.texto = texto;
		}

		@Override
		public void execute() {
			System.out.println(texto);
		}
	}

	class Bloco implements Comando {
		private final List<Comando> comandos;

		public Bloco(List<Comando> comandos) {
			this.comandos = comandos;
		}

		@Override
		public void execute() {
			comandos.forEach(Comando::execute);
		}
	}

	class Atribuicao implements Comando {
		private final String id;
		private final Expressao exp;

		Atribuicao(String id, Expressao exp) {
			this.id = id;
			this.exp = exp;
		}

		@Override
		public void execute() {
			ambiente.put(id, exp.getValor());
		}
	}
	
	class AtribuicaoParalela implements Comando {
		private final Map<String, Expressao> listaParametros;

		AtribuicaoParalela(Map<String, Expressao> listaParametros) {
			this.listaParametros = listaParametros;
		}

		@Override
		public void execute() {
			for(String id : listaParametros.keySet()) {
				if (!id.equals(","))
					ambiente.put(id, listaParametros.get(id).getValor());
			}
			
		}
	}

	/*
	   Expressoes
	 */

	abstract class OpBin<T>  {
		protected final T esq;
		protected final T dir;

		OpBin(T esq, T dir) {
			this.esq = esq;
			this.dir = dir;
		}
	}

	abstract class OpUnaria<T>  {
		protected final T operando;

		OpUnaria(T operando) {
			this.operando = operando;
		}
	}

	class Inteiro implements Expressao {
		private final int valor;

		Inteiro(int valor) {
			this.valor = valor;
		}

		@Override
		public int getValor() {
			return valor;
		}
	}

	class Id implements Expressao {
		private final String id;

		Id(String id) {
			this.id = id;
		}

		@Override
		public int getValor() {
			return ambiente.getOrDefault(id, 0);
		}
	}

	Leia leia = new Leia();
	class Leia implements Expressao {
		@Override
		public int getValor() {
			return scanner.nextInt();
		}
	}

	class ExpSoma extends OpBin<Expressao> implements Expressao {
		ExpSoma(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public int getValor() {
			return esq.getValor() + dir.getValor();
		}
	}
	
	class ExpExpon extends OpBin<Expressao> implements Expressao {
		ExpExpon(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public int getValor() {
			return (int) Math.pow(esq.getValor(), dir.getValor()) ;
		}
	}


	class ExpSub extends OpBin<Expressao> implements Expressao {
		ExpSub(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public int getValor() {
			return esq.getValor() - dir.getValor();
		}
	}

	class ExpMult extends OpBin<Expressao> implements Expressao{
		ExpMult(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public int getValor() {
			return esq.getValor() * dir.getValor();
		}
	}
	
	class ExpDiv extends OpBin<Expressao> implements Expressao{
		ExpDiv(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public int getValor() {
			return esq.getValor() / dir.getValor();
		}
	}

	class Booleano implements Bool {
		private final boolean valor;

		Booleano(boolean valor) {
			this.valor = valor;
		}

		@Override
		public boolean getValor() {
			return valor;
		}
	}

	class ExpIgual extends OpBin<Expressao> implements Bool {
		ExpIgual(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() == dir.getValor();
		}
	}

	class ExpMenorIgual extends OpBin<Expressao> implements Bool{
		ExpMenorIgual(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() <= dir.getValor();
		}
	}
	
	class ExpMaiorIgual extends OpBin<Expressao> implements Bool{
		ExpMaiorIgual(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() >= dir.getValor();
		}
	}
	
	class ExpMenor extends OpBin<Expressao> implements Bool{
		ExpMenor(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() < dir.getValor();
		}
	}
	
	class ExpMaior extends OpBin<Expressao> implements Bool{
		ExpMaior(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() > dir.getValor();
		}
	}
	
	class ExpDiferente extends OpBin<Expressao> implements Bool{
		ExpDiferente(Expressao esq, Expressao dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() != dir.getValor();
		}
	}

	class NaoLogico extends OpUnaria<Bool> implements Bool{
		NaoLogico(Bool operando) {
			super(operando);
		}

		@Override
		public boolean getValor() {
			return !operando.getValor();
		}
	}

	class ELogico extends OpBin<Bool> implements Bool{
		ELogico(Bool esq, Bool dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() && dir.getValor();
		}
	}
	
	class OuLogico extends OpBin<Bool> implements Bool{
		OuLogico(Bool esq, Bool dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() || dir.getValor();
		}
	}
	
	class XorLogico extends OpBin<Bool> implements Bool{
		XorLogico(Bool esq, Bool dir) {
			super(esq, dir);
		}

		@Override
		public boolean getValor() {
			return esq.getValor() ^ !dir.getValor();
		}
	}
}
