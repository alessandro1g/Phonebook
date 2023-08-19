package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link QuadraticProbingHashTable} is an Openly Addressed {@link HashTable} which uses <b>Quadratic
 * Probing</b> as its collision resolution strategy. Quadratic Probing differs from <b>Linear</b> Probing
 * in that collisions are resolved by taking &quot; jumps &quot; on the hash table, the length of which
 * determined by an increasing polynomial factor. For example, during a key insertion which generates
 * several collisions, the first collision will be resolved by moving 1^2 + 1 = 2 positions over from
 * the originally hashed address (like Linear Probing), the second one will be resolved by moving
 * 2^2 + 2= 6 positions over from our hashed address, the third one by moving 3^2 + 3 = 12 positions over, etc.
 * </p>
 *
 * <p>By using this collision resolution technique, {@link QuadraticProbingHashTable} aims to get rid of the
 * &quot;key clustering &quot; problem that {@link LinearProbingHashTable} suffers from. Leaving more
 * space in between memory probes allows other keys to be inserted without many collisions. The tradeoff
 * is that, in doing so, {@link QuadraticProbingHashTable} sacrifices <em>cache locality</em>.</p>
 *
 * @author YOUR NAME HERE!
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see LinearProbingHashTable
 * @see CollisionResolver
 */
public class QuadraticProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/
	private boolean soft;
	private int t_count;
	private KVPairList i;

    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *               we want soft deletion, {@code false} otherwise.
     */
    public QuadraticProbingHashTable(boolean soft) {
        this.soft = soft;
        this.t_count = 0;
        this.count = 0;
        this.primeGenerator = new PrimeGenerator();
        this.table = new KVPair[primeGenerator.getCurrPrime()];
        this.i = new KVPairList();
    }

    @Override
    public Probes put(String key, String value) {
    	if (key == null) {
    		throw new IllegalArgumentException("no key");
    	}else if(value == null){
    		throw new IllegalArgumentException("no value");
    	}
    	if (!this.i.containsKVPair(key, value)) {
    		System.out.println("key: " + key + " value: " + value);
    		i.addBack(key, value);
    	}
    	int p_count = 0;
    	double resize = ((double)this.count)/(double)this.table.length;
    	
    	if (0.5 <resize){
    		int pr = this.primeGenerator.getNextPrime();
    		//this.table = new KVPair[pr];
    		this.count = 0;
    		this.t_count = 0;
    		KVPairList record = new KVPairList();
    		for (KVPair p : this.table){
    			if(p != null ){
    				if (p != this.TOMBSTONE) {
    				record.addBack(p.getKey(), p.getValue());
    				}
                }
    			
    			p_count += 1;
    		}
    		
    		this.table = new KVPair[pr];
    		
    		if (record.size() > 0) {
    			for(KVPair p : record){
                    p_count += put(p.getKey(),p.getValue()).getProbes();
                }
    		}
    	}
    	
    	KVPair node = new KVPair(key, value); 
    	int hashIdx = this.hash(key); 
    	
    	int increment = 2;
    	
    	while(this.table[hashIdx] != null) {
    		hashIdx = (this.hash(key) + (increment - 1) + ((increment - 1)*(increment - 1))) % this.table.length;
    		increment += 1;
    		p_count += 1;
    	}
    	this.table[hashIdx] = node;
    	p_count += 1;
    	this.count += 1;
    	return new Probes(value, p_count);
    }


    @Override
    public Probes get(String key) {
    	int p_count = 1;
    	int increment = 2;
        int hashIdx = this.hash(key);
        int startIdx = this.hash(key);
        
        while(this.table[hashIdx]!= null) {
        	if (hashIdx == startIdx && increment != 2) {
        		return new Probes(null, p_count);
        	}else if(this.table[hashIdx] != this.TOMBSTONE && this.table[hashIdx].getKey().equals(key)) {
        		return new Probes(table[hashIdx].getValue(), p_count);
        	}
        	
        	p_count += 1;
        	hashIdx = (this.hash(key) + (increment - 1) + ((increment - 1)*(increment - 1))) % this.table.length;
        	increment += 1;
        }
        return new Probes(null, p_count);
        
    }

    @Override
    public Probes remove(String key) {
    	throw new UnimplementedMethodException();
    	/*
        int p_count = 1;
        int o = 0;
        int hashIdx = this.hash(key);
        int increment = 2;
        int start = hashIdx;
        
        if (this.soft) {
        	while(this.table[hashIdx] != null){
        		if(increment > 2) {
        			if (hashIdx == start) {
        				return new Probes(null, p_count); 
        			}
        		}
        		
        		if(this.table[hashIdx].getKey().equals(key)){
        			if ( this.table[hashIdx] != this.TOMBSTONE) {
        				this.t_count +=1;
        				String ret = this.table[hashIdx].getValue();
        				this.table[hashIdx] = this.TOMBSTONE;
        				return new Probes(ret, p_count);
        				
        			}else {
        				o += 1;
        			}
        			
        		}
        		p_count += 1;
        		hashIdx = (this.hash(key) + (increment - 1) + ((increment - 1)*(increment - 1))) % this.table.length;
        	}
        	return new Probes(null, p_count);
        	
        }
        
        while(this.table[hashIdx] != null) {
        	if (key.equals(this.table[hashIdx].getKey())){
        		String ret = this.table[hashIdx].getValue();
        		this.table[hashIdx] = null; 
        		
        		this.count = 0;
        		KVPairList record = new KVPairList(); 
        		
        		for(KVPair p : this.table) {
        			if(p != null) {
        				String pk = p.getKey();
        				String pV = p.getValue();
        				record.addBack(pk,pV);
        			}
        			p_count +=1;
        		}
        		
        		this.table = new KVPair[this.primeGenerator.getCurrPrime()];
        		if (record.size() >0) {
        			for(KVPair p : record){
        				p_count += (this.put(p.getKey(),p.getValue())).getProbes(); 
        			}
        		}
        		return new Probes(ret, p_count);
        	} 
        	p_count +=1 ;
        	hashIdx = (this.hash(key) + (increment - 1) + ((increment - 1)*(increment - 1))) % this.table.length;
        	increment += 1;
        } 
        return new Probes(null, p_count);
        */
    }


    @Override
    public boolean containsKey(String key) {
    	Probes temp1 = this.get(key);
    	if (temp1 != null) {
    		return true;
    	}else {
    		return false;
    	}
    }

    @Override
    public boolean containsValue(String value) {
    	for (KVPair p : this.table) {
    		if (p.getValue().equals(value)) {
    			return true;
    		}
    	}
    	return false;
    }
    @Override
    public int size(){
    	return this.count - this.t_count;
    }

    @Override
    public int capacity() {
    	return this.table.length;
    }
    
    public static void main(String[] args) {
    	QuadraticProbingHashTable ol = new QuadraticProbingHashTable(true);
    	ol.put("DeAndre", "a");
    	ol.put("Charles", "b");
    	ol.put("Christine", "c");
    	ol.put("Alexander", "d");
    	ol.put("Carl", "e");
    	
    	ol.put("Paulette", "a");
    	ol.put("Aditya", "b");
    	ol.put("Arnold", "c");
    	ol.put("Jacqueline", "d");
    	ol.put("Yi", "e");
    	
    	ol.put("Tiffany", "c");
    	ol.put("Nakeesha", "d");
    	int i = ol.put("Jason", "e").getProbes();
    	System.out.println(i); 
    	
    	ol.get("DeAndre");
    	ol.get("Charles");
    	ol.get("Christine");
    	ol.get("Alexander");
    	ol.get("Carl");
    	
    	ol.get("Paulette");
    	ol.get("Aditya");
    	ol.get("Arnold");
    	ol.get("Jacqueline");
    	ol.get("Yi");
    	
    	ol.remove("DeAndre");
    	ol.remove("Charles");
    	ol.remove("Christine");
    	ol.remove("Alexander");
    	ol.remove("Carl");
    	
    	ol.remove("Paulette");
    	ol.remove("Aditya");
    	ol.remove("Arnold");
    	ol.remove("Jacqueline");
    	ol.remove("Yi");
    	System.out.println("a"); 
    	
    }
    
}



