package multithread;

public class Graph1{
      public static void main(String args[])throws Exception {
	  Graph ob = new Graph();
	  Vertex a = new Vertex("A");
	  Vertex b = new Vertex("B");
	  Vertex c = new Vertex("C");
	  Vertex d = new Vertex("D");
//	  Edge e = new Edge(a,b);
//	  Edge e1 = new Edge(b,c);
//	  Edge e2 = new Edge(c,d);
//	  Edge e3 = new Edge(d,a);
	  ob.addVertex(a);
	  ob.addVertex(b);
	  ob.addVertex(c);
	  //ob.addVertex(d);
	  ob.addEdge(a, b, 1);
	  ob.addEdge(b ,c, 1);
	  ob.addEdge(c, a, 1);
	  //ob.addEdge(d, c, 1);
	  
	  
	  for(int i = 0;i<ob.findCycles().length;i++){
		  System.out.println(ob.findCycles()[i]);
	  }
	  
  }
}