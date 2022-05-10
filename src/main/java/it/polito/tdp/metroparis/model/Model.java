package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private List<Fermata> fermate;
	Map<Integer, Fermata> fermateIdMap ;
	
	private Graph<Fermata, DefaultEdge> grafo;

	public List<Fermata> getFermate() {
		if (this.fermate == null) {
			MetroDAO dao = new MetroDAO();
			this.fermate = dao.getAllFermate();
			
			this.fermateIdMap = new HashMap<Integer, Fermata>();
			for (Fermata f : this.fermate)
				this.fermateIdMap.put(f.getIdFermata(), f);

		}
		return this.fermate;
	}

	public void creaGrafo() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);

/*		Graphs.addAllVertices(this.grafo, this.fermate);   /* la differenza sta soltanto nel fatto che questa 
															* istruzione funziona solo se qualcuno ha chiamato getFermate() 
															* prima di chiamare creaGrafo()
															*/	
															
		Graphs.addAllVertices(this.grafo, getFermate());	//programmazione difensiva
		
		MetroDAO dao = new MetroDAO();

		List<CoppiaId> fermateDaCollegare = dao.getAllFermateConnesse();
		for (CoppiaId coppia : fermateDaCollegare) {
			this.grafo.addEdge(fermateIdMap.get(coppia.getIdPartenza()), fermateIdMap.get(coppia.getIdArrivo()));
		}

//		System.out.println(this.grafo);
//		System.out.println("Vertici = " + this.grafo.vertexSet().size());
//		System.out.println("Archi   = " + this.grafo.edgeSet().size());
	}

	public List<Fermata> calcolaPercorso(Fermata partenza, Fermata arrivo) {
		creaGrafo() ;
		Map<Fermata, Fermata> alberoInverso = visitaGrafo(partenza);
		
		Fermata corrente = arrivo;
		List<Fermata> percorso = new ArrayList<>();	//se avessi problemi di efficienza potrei utilizzare una 'LinkedList' dove l'inserimento in testa e l'inserimento in coda hanno esattamente lo stesso peso
		while(corrente != null) {	//null perché era il predecessore che abbiamo messo dentro la mappa in corrispondenza del nodo di partenza
									//il nodo di partenza ha come valore nella mappa il null (è la condizione per cui mi fa capire di essere arrivato al nodo di partenza)
			percorso.add(0, corrente);	//aggiungo in testa anziché in coda perché in questo modo gli altri faranno spazio. Altrimenti avrei ottenuto il percorso a ritroso
			
			corrente = alberoInverso.get(corrente);		/* Avrei potuto utilizzare il metodo 'getParent(V,v)' -> mi dà l'informazione dell'albero inverso, dato un vertice mi dà quello precedente (null se è il nodo radice)
														 * qui avrei potuto scrivere: 'corrente = getParent(corrente);'
			 											 * 'getSpanningTreeEdge(V,v)' -> da un vertice mi dice qual è l'arco che attraverso lo SpanningTree raggiunge questo arco (se V è la radice restituisce null). 
			 											 * Questi mi permettevano di evitare di implementare il Listener
			 											 * Questi due metodi li ha SOLO l'iteratore di visita in ampiezza (ATTENZIONE!!)
			 											 */
			//Se mi serve il cammino e non voglio perdere tempo ha costruire l'iteratore di visita ricordare che c'è il metodo 
			//'getParent(V,v)' che permette di consultare l'alberoInverso. Non mi dà il percorso, il percorso me lo devo comunque
			//costruire come abbiamo fatto
		}
		return percorso;
	}
	
	public Map<Fermata, Fermata> visitaGrafo(Fermata partenza) {
		//utilizziamo un visita in ampiezza perché è quello che ci garantisce di usare il numero minimo di archi per raggiungere 
		//un qualunque nodo di destinazione a partire dal singolo nodo di partenza che specifichiamo nel costruttore
		GraphIterator<Fermata, DefaultEdge> visita = new BreadthFirstIterator<>(this.grafo, partenza);
		
		Map<Fermata,Fermata> alberoInverso = new HashMap<>() ;
		alberoInverso.put(partenza, null) ;
		
		visita.addTraversalListener(new RegistraAlberoDiVisita(alberoInverso, this.grafo));
		/* 'addTraversalListener()' è una classe in grado di osservare l'iteratore mentre lavora
		 * e salva dell'informazioni per noi importanti durante la visita stessa. Informazioni importanti
		 * che per noi è l'alberoInverso, ossia l'albero di visita in ampiezza, inverso perché abbiamo gli archi
		 * al contrario. Abbiamo creato l'alberoInverso per 2 motivi: dato un vertice il suo predecessore è uno solo
		 * mentre dato un vertice i suoi successori nell'albero di visita possono essere più di uno, quindi rappresentandolo
		 * al contrario ho una rappresentazione univoca (dato un vertice ne trovo uno solo che lo precede), inoltre in questo modo
		 * non abbiamo bisogno di sfruttare un algoritmo ricorsivo (se un nodo ha 2 'figli' non sa quale strada seguire se non calcolando
		 * entrambe le strade). Il percorso è unico per arrivare al nodo di arrivo ma se il nodo ha 2 figli di conseguenza ho 2 alternative 
		 * possibili che mi portano entrambe a cammini minimi, ma a noi interessa il cammino minimo per arrivare al nodo di destinazione
		 * e quindi ad ogni bivio non saprei quale strada prendere. Avendo l'alberoInverso è più facile trovare questa strada
		 */
		
		while (visita.hasNext()) {
			Fermata f = visita.next();
//			System.out.println(f);
		}
		
		return alberoInverso;
		
		// Ricostruiamo il percorso a partire dall'albero inverso (pseudo-code)
//		List<Fermata> percorso = new ArrayList<>() ;
//		fermata = arrivo
//		while(fermata != null)
//			fermata = alberoInverso.get(fermata)
//			percorso.add(fermata)
	}

}
