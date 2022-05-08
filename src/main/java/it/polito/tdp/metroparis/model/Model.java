package it.polito.tdp.metroparis.model;

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
	
	private Graph<Fermata,DefaultEdge> grafo ;
	
	public void creaGrafo() {
		//dobbiamo scegliere una tra le 16 classi che jgrapht offre per poter creare il grafo
		this.grafo = new SimpleDirectedGraph<Fermata,DefaultEdge>(DefaultEdge.class) ;
		/* questo costruttore è preferibile sempre metterlo dentro questo metodo che va a fare le query che costruiscono il grafo stesso
		 * piuttosto che nella dichiarazione del grafo stesso, perché garantisce che se da interfaccia utente vengono cambiati i parametri
		 * il grafo ogni volta verrà ricreato da zero perché questa new di fatto cancella quella vecchia
		 */
		
		MetroDAO dao = new MetroDAO() ;
		
		List<Fermata> fermate = dao.getAllFermate() ;
		
		Map<Integer,Fermata> fermateIdMap = new HashMap<Integer,Fermata>();	//per punto c
		for(Fermata f : fermate)
			fermateIdMap.put(f.getIdFermata(), f) ;
		
		Graphs.addAllVertices(this.grafo, fermate) ;
		
		//3 METODI per creare gli archi
		//Non fare l'errore di tuffarsi subito su quello più complicato. Se non ci sono problemi di dimensione del grafo per esempio, va benissimo utilizzare il metodo 1 (in quel caso non sarebbe lento)
		
		// METODO 1: itero su ogni coppia di vertici
		// Metodo più semplice ma ha il difetto che devo fare un numero di accessi al database pari a #vertici^2 e questo nel caso di tanti vertici e magari anche di query complicata aumenta il tempo di esecuzione
//		for(Fermata partenza : fermate) {
//			for(Fermata arrivo : fermate) {
//				if(dao.isFermateConnesse(partenza, arrivo)) { //if(esiste almeno una connessone tra partenza ed arrivo)
//					this.grafo.addEdge(partenza, arrivo) ;
//				}
//			}
//		}
		
		// METODO 2: dato ciascun vertice, trova i vertici ad esso adiacenti
		// Variante 2a: il DAO restituisce un elenco di ID numerici
		
		// Nota: posso iterare su 'fermate' oppure su 'this.grafo.vertexSet()'
//		for(Fermata partenza : fermate) {
//			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza) ;
//			for(Integer id : idConnesse) {
//		        // Fermata arrivo = (fermata che possiede questo "id") ;
//				//bisogna convertire gli id ricevuti in oggetti Fermata
//				Fermata arrivo = null ;
//				for(Fermata f : fermate) {   //se non avessi avuto la lista avrei potuto utilizzare "this.grafo.vertexSet()". NON utilizzare invece "dao.getAllFermate()" perché ogni volta fa la query ed è troppo dispendioso
//					if(f.getIdFermata()==id) {
//						arrivo = f ;
//						break ;
//					}
//				}
//				this.grafo.addEdge(partenza, arrivo) ;
//			}
//		}
		
		// METODO 2: dato ciascun vertice, trova i vertici ad esso adiacenti
		// Variante 2b: il DAO restituisce un elenco di oggetti Fermata
//		for(Fermata partenza : fermate) {
//			List<Fermata> arrivi = dao.getFermateConnesse(partenza) ;
//			for(Fermata arrivo : arrivi) {
//				this.grafo.addEdge(partenza, arrivo);
//			}
//		}
		
		// METODO 2: dato ciascun vertice, trova i vertici ad esso adiacenti
		// Variante 2c: il DAO restituisce un elenco di ID numerici, che converto in oggetti
		// tramite una Map<Integer,Fermata> - "Identity Map"
		// ovvero costruire una mappa che mi permetta di passare rapidamente da un ID ad un oggetto Fermata
		
//		for(Fermata partenza : fermate) {
//			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza) ;
//			for(int id : idConnesse) {
//				Fermata arrivo = fermateIdMap.get(id) ;
//				this.grafo.addEdge(partenza, arrivo);
//			}
//		}
		
		
		// METODO 3: faccio una sola query che mi restituisca le coppie
		// di fermate da collegare 
		// (variante preferita: 3c: usare Identity Map)
		List<CoppiaId> fermateDaCollegare = dao.getAllFermateConnesse() ;
		for(CoppiaId coppia : fermateDaCollegare) {
			this.grafo.addEdge(
					fermateIdMap.get(coppia.getIdPartenza()),
					fermateIdMap.get(coppia.getIdArrivo())
					);
		}
		
		
		System.out.println(this.grafo) ;
		System.out.println("#Vertici = " + this.grafo.vertexSet().size());
		System.out.println("#Archi   = " + this.grafo.edgeSet().size());
		
		visitaGrafo(fermate.get(0));	//stampa l'elenco dei vertici raggiungibili a partire da fermate.get(0) attraverso l'algoritmo di visita in ampiezza o in profondità

	}
	
	public void visitaGrafo(Fermata partenza) {
		//visita in ampiezza
//		GraphIterator<Fermata, DefaultEdge> visita = new BreadthFirstIterator<>(this.grafo, partenza) ;
		
		//visita in profondità
		GraphIterator<Fermata, DefaultEdge> visita = new DepthFirstIterator<>(this.grafo, partenza) ;
		while(visita.hasNext()) {	//finché hai vertici successivi
			Fermata f = visita.next();
			System.out.println(f);
		}
	}

}
