package multithread;

/**
 *
 * @author Erik
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class Graph<T> {

    /**
     * Color used to mark unvisited nodes
     */
    public static final int VISIT_COLOR_WHITE = 1;

    /**
     * Color used to mark nodes as they are first visited in DFS order
     */
    public static final int VISIT_COLOR_GREY = 2;

    /**
     * Color used to mark nodes after descendants are completely visited
     */
    public static final int VISIT_COLOR_BLACK = 3;


    private List<Vertex<T>> verticies;


    private List<Edge<T>> edges;


    private Vertex<T> rootVertex;

    //Construct a new graph
    public Graph() {
        verticies = new ArrayList<Vertex<T>>();
        edges = new ArrayList<Edge<T>>();
    }

    public static void main(String[] args) {
        Graph g = new Graph();
        Vertex v = new Vertex();
    }

    
    public boolean isEmpty() {
        return verticies.size() == 0;
    }

    
    public boolean addVertex(Vertex<T> v) {
        boolean added = false;
        if (verticies.contains(v) == false) {
            added = verticies.add(v);
        }
        return added;
    }

    
    public int size() {
        return verticies.size();
    }

   
    public Vertex<T> getRootVertex() {
        return rootVertex;
    }

    
    public void setRootVertex(Vertex<T> root) {
        this.rootVertex = root;
        if (verticies.contains(root) == false) {
            this.addVertex(root);
        }
    }

  
    public Vertex<T> getVertex(int n) {
        return verticies.get(n);
    }

  
    public List<Vertex<T>> getVerticies() {
        return this.verticies;
    }

    public boolean addEdge(Vertex<T> from, Vertex<T> to, int cost) throws IllegalArgumentException {
        if (verticies.contains(from) == false) {
            throw new IllegalArgumentException("from is not in graph");
        }
        if (verticies.contains(to) == false) {
            throw new IllegalArgumentException("to is not in graph");
        }

        Edge<T> e = new Edge<T>(from, to, cost);
        if (from.findEdge(to) != null) {
            return false;
        } else {
            from.addEdge(e);
            to.addEdge(e);
            edges.add(e);
            return true;
        }
    }


    public boolean insertBiEdge(Vertex<T> from, Vertex<T> to, int cost)
            throws IllegalArgumentException {
        return addEdge(from, to, cost) && addEdge(to, from, cost);
    }

    
    public List<Edge<T>> getEdges() {
        return this.edges;
    }


    public boolean removeVertex(Vertex<T> v) {
        if (!verticies.contains(v)) {
            return false;
        }

        verticies.remove(v);
        if (v == rootVertex) {
            rootVertex = null;
        }

        // Remove the edges associated with v
        for (int n = 0; n < v.getOutgoingEdgeCount(); n++) {
            Edge<T> e = v.getOutgoingEdge(n);
            v.remove(e);
            Vertex<T> to = e.getTo();
            to.remove(e);
            edges.remove(e);
        }
        for (int n = 0; n < v.getIncomingEdgeCount(); n++) {
            Edge<T> e = v.getIncomingEdge(n);
            v.remove(e);
            Vertex<T> predecessor = e.getFrom();
            predecessor.remove(e);
        }
        return true;
    }


    public boolean removeEdge(Vertex<T> from, Vertex<T> to) {
        Edge<T> e = from.findEdge(to);
        if (e == null) {
            return false;
        } else {
            from.remove(e);
            to.remove(e);
            edges.remove(e);
            return true;
        }
    }


    public void clearMark() {
        for (Vertex<T> w : verticies) {
            w.clearMark();
        }
    }


    public void clearEdges() {
        for (Edge<T> e : edges) {
            e.clearMark();
        }
    }


    public void depthFirstSearch(Vertex<T> v, final Visitor<T> visitor) {
        VisitorEX<T, RuntimeException> wrapper = new VisitorEX<T, RuntimeException>() {
            public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
                if (visitor != null) {
                    visitor.visit(g, v);
                }
            }
        };
        this.depthFirstSearch(v, wrapper);
    }

    
    public <E extends Exception> void depthFirstSearch(Vertex<T> v, VisitorEX<T, E> visitor) throws E {
        if (visitor != null) {
            visitor.visit(this, v);
        }
        v.visit();
        for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
            Edge<T> e = v.getOutgoingEdge(i);
            if (!e.getTo().visited()) {
                depthFirstSearch(e.getTo(), visitor);
            }
        }
    }

    
    public void breadthFirstSearch(Vertex<T> v, final Visitor<T> visitor) {
        VisitorEX<T, RuntimeException> wrapper = new VisitorEX<T, RuntimeException>() {
            public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
                if (visitor != null) {
                    visitor.visit(g, v);
                }
            }
        };
        this.breadthFirstSearch(v, wrapper);
    }

    
    public <E extends Exception> void breadthFirstSearch(Vertex<T> v, VisitorEX<T, E> visitor)
            throws E {
        LinkedList<Vertex<T>> q = new LinkedList<Vertex<T>>();

        q.add(v);
        if (visitor != null) {
            visitor.visit(this, v);
        }
        v.visit();
        while (q.isEmpty() == false) {
            v = q.removeFirst();
            for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
                Edge<T> e = v.getOutgoingEdge(i);
                Vertex<T> to = e.getTo();
                if (!to.visited()) {
                    q.add(to);
                    if (visitor != null) {
                        visitor.visit(this, to);
                    }
                    to.visit();
                }
            }
        }
    }

    
    public void dfsSpanningTree(Vertex<T> v, DFSVisitor<T> visitor) {
        v.visit();
        if (visitor != null) {
            visitor.visit(this, v);
        }

        for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
            Edge<T> e = v.getOutgoingEdge(i);
            if (!e.getTo().visited()) {
                if (visitor != null) {
                    visitor.visit(this, v, e);
                }
                e.mark();
                dfsSpanningTree(e.getTo(), visitor);
            }
        }
    }

    
    public Vertex<T> findVertexByName(String name) {
        Vertex<T> match = null;
        for (Vertex<T> v : verticies) {
            if (name.equals(v.getName())) {
                match = v;
                break;
            }
        }
        return match;
    }

    
    public Vertex<T> findVertexByData(T data, Comparator<T> compare) {
        Vertex<T> match = null;
        for (Vertex<T> v : verticies) {
            if (compare.compare(data, v.getData()) == 0) {
                match = v;
                break;
            }
        }
        return match;
    }

    
    public Edge<T>[] findCycles() {

        ArrayList<Edge<T>> cycleEdges = new ArrayList<Edge<T>>();
        // Mark all verticies as white
        for (int n = 0; n < verticies.size(); n++) {
            Vertex<T> v = getVertex(n);
            v.setMarkState(VISIT_COLOR_WHITE);
        }
        for (int n = 0; n < verticies.size(); n++) {
            Vertex<T> v = getVertex(n);
            visit(v, cycleEdges);
        }

        Edge<T>[] cycles = new Edge[cycleEdges.size()];
        cycleEdges.toArray(cycles);

        return cycles;
    }

    private void visit(Vertex<T> v, ArrayList<Edge<T>> cycleEdges) {
        v.setMarkState(VISIT_COLOR_GREY);
        int count = v.getOutgoingEdgeCount();
        for (int n = 0; n < count; n++) {
            Edge<T> e = v.getOutgoingEdge(n);
            Vertex<T> u = e.getTo();
            if (u.getMarkState() == VISIT_COLOR_GREY) {
                // A cycle Edge<T>
                cycleEdges.add(e);
            } else if (u.getMarkState() == VISIT_COLOR_WHITE) {
                visit(u, cycleEdges);

            }
        }
        //v.setMarkState(VISIT_COLOR_BLACK);
        v.setMarkState(VISIT_COLOR_WHITE);
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer("Graph[");
        for (Vertex<T> v : verticies) {
            tmp.append(v);
        }
        tmp.append(']');
        return tmp.toString();
    }

}


class Edge<T> {

    private Vertex<T> from;

    private Vertex<T> to;

    private int cost;

    private boolean mark;

    
    public Edge(Vertex<T> from, Vertex<T> to) {
        this(from, to, 0);
    }

    
    public Edge(Vertex<T> from, Vertex<T> to, int cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
        mark = false;
    }

   
    public Vertex<T> getTo() {
        return to;
    }

    
    public Vertex<T> getFrom() {
        return from;
    }

    
    public int getCost() {
        return cost;
    }

    
    public void mark() {
        mark = true;
    }

    
    public void clearMark() {
        mark = false;
    }

    
    public boolean isMarked() {
        return mark;
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer("Edge[from: ");
        tmp.append(from.getName());
        tmp.append(",to: ");
        tmp.append(to.getName());
        tmp.append(", cost: ");
        tmp.append(cost);
        tmp.append("]");
        return tmp.toString();
    }
}


class Vertex<T> {

    private List<Edge<T>> incomingEdges;

    private List<Edge<T>> outgoingEdges;

    private String name;

    private boolean mark;

    private int markState;

    private T data;

    
    public Vertex() {
        this(null, null);
    }

   
    public Vertex(String n) {
        this(n, null);
    }

    
    public Vertex(String n, T data) {
        incomingEdges = new ArrayList<Edge<T>>();
        outgoingEdges = new ArrayList<Edge<T>>();
        name = n;
        mark = false;
        this.data = data;
    }

    
    public String getName() {
        return name;
    }


    public T getData() {
        return this.data;
    }

    
    public void setData(T data) {
        this.data = data;
    }

    
    public boolean addEdge(Edge<T> e) {
        if (e.getFrom() == this) {
            outgoingEdges.add(e);
        } else if (e.getTo() == this) {
            incomingEdges.add(e);
        } else {
            return false;
        }
        return true;
    }

    
    public void addOutgoingEdge(Vertex<T> to, int cost) {
        Edge<T> out = new Edge<T>(this, to, cost);
        outgoingEdges.add(out);
    }

    
    public void addIncomingEdge(Vertex<T> from, int cost) {
        Edge<T> out = new Edge<T>(this, from, cost);
        incomingEdges.add(out);
    }

    
    public boolean hasEdge(Edge<T> e) {
        if (e.getFrom() == this) {
            return incomingEdges.contains(e);
        } else if (e.getTo() == this) {
            return outgoingEdges.contains(e);
        } else {
            return false;
        }
    }

    
    public boolean remove(Edge<T> e) {
        if (e.getFrom() == this) {
            incomingEdges.remove(e);
        } else if (e.getTo() == this) {
            outgoingEdges.remove(e);
        } else {
            return false;
        }
        return true;
    }

   
    public int getIncomingEdgeCount() {
        return incomingEdges.size();
    }


    public Edge<T> getIncomingEdge(int i) {
        return incomingEdges.get(i);
    }


    public List getIncomingEdges() {
        return this.incomingEdges;
    }

    public int getOutgoingEdgeCount() {
        return outgoingEdges.size();
    }


    public Edge<T> getOutgoingEdge(int i) {
        return outgoingEdges.get(i);
    }


    public List getOutgoingEdges() {
        return this.outgoingEdges;
    }


    public Edge<T> findEdge(Vertex<T> dest) {
        for (Edge<T> e : outgoingEdges) {
            if (e.getTo() == dest) {
                return e;
            }
        }
        return null;
    }


    public Edge<T> findEdge(Edge<T> e) {
        if (outgoingEdges.contains(e)) {
            return e;
        } else {
            return null;
        }
    }


    public int cost(Vertex<T> dest) {
        if (dest == this) {
            return 0;
        }

        Edge<T> e = findEdge(dest);
        int cost = Integer.MAX_VALUE;
        if (e != null) {
            cost = e.getCost();
        }
        return cost;
    }


    public boolean hasEdge(Vertex<T> dest) {
        return (findEdge(dest) != null);
    }


    public boolean visited() {
        return mark;
    }


    public void mark() {
        mark = true;
    }


    public void setMarkState(int state) {
        markState = state;
    }


    public int getMarkState() {
        return markState;
    }


    public void visit() {
        mark();
    }


    public void clearMark() {
        mark = false;
    }


    public String toString() {
        StringBuffer tmp = new StringBuffer("Vertex(");
        tmp.append(name);
        tmp.append(", data=");
        tmp.append(data);
        tmp.append("), in:[");
        for (int i = 0; i < incomingEdges.size(); i++) {
            Edge<T> e = incomingEdges.get(i);
            if (i > 0) {
                tmp.append(',');
            }
            tmp.append('{');
            tmp.append(e.getFrom().name);
            tmp.append(',');
            tmp.append(e.getCost());
            tmp.append('}');
        }
        tmp.append("], out:[");
        for (int i = 0; i < outgoingEdges.size(); i++) {
            Edge<T> e = outgoingEdges.get(i);
            if (i > 0) {
                tmp.append(',');
            }
            tmp.append('{');
            tmp.append(e.getTo().name);
            tmp.append(',');
            tmp.append(e.getCost());
            tmp.append('}');
        }
        tmp.append(']');
        return tmp.toString();
    }
}



interface Visitor<T> {

    
    public void visit(Graph<T> g, Vertex<T> v);
}



interface VisitorEX<T, E extends Exception> {


    public void visit(Graph<T> g, Vertex<T> v) throws E;
}



interface DFSVisitor<T> {

   
    public void visit(Graph<T> g, Vertex<T> v);

    
    public void visit(Graph<T> g, Vertex<T> v, Edge<T> e);
}
