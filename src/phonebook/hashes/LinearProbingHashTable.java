package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link LinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with <b>Linear Probing</b> as its
 * collision resolution strategy: every key collision is resolved by moving one address over. It is
 * the most famous collision resolution strategy, praised for its simplicity, theoretical properties
 * and cache locality. It <b>does</b>, however, suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author Alessandro Gagarin
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class LinearProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/
	
	private int t_count;
	private boolean soft;
	//private KVPairList record;

	
	
    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */
	
	

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     *
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *             we want soft deletion, {@code false} otherwise.
     */
    public LinearProbingHashTable(boolean soft) {
        this.soft = soft;
        this.t_count = 0;
        this.count = 0; //from open addressing has 
        this.primeGenerator = new PrimeGenerator();
        this.table = new KVPair[primeGenerator.getCurrPrime()];
        // this will keep track of all the KVPairs in the Table
        //this.record = new KVPairList();
    }

    /**
     * Inserts the pair &lt;key, value&gt; into this. The container should <b>not</b> allow for {@code null}
     * keys and values, and we <b>will</b> test if you are throwing a {@link IllegalArgumentException} from your code
     * if this method is given {@code null} arguments! It is important that we establish that no {@code null} entries
     * can exist in our database because the semantics of {@link #get(String)} and {@link #remove(String)} are that they
     * return {@code null} if, and only if, their key parameter is {@code null}. This method is expected to run in <em>amortized
     * constant time</em>.
     * <p>
     * Instances of {@link LinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     *
     * @param key   The record's key.
     * @param value The record's value.
     * @return The {@link phonebook.utils.Probes} with the value added and the number of probes it makes.
     * @throws IllegalArgumentException if either argument is {@code null}.
     */
    @Override
    public Probes put(String key, String value) {
    	//probes has the value as well as the number of probes taken
    	if (value == null) {
    		throw new IllegalArgumentException("val is null");
    	}
    	if (key == null) {
    		throw new IllegalArgumentException("key is null");
    	}
    	
    	int num_probes = 0;
    	double resize = .5; 
    	// we need to check if we need to resize 
    	if (resize < ((double) this.count)/(double)this.table.length) {
    		// KVPairList temp = new KVPairList();
    		int pr = this.primeGenerator.getNextPrime();
    		this.count = 0;
    		this.t_count = 0;
    		KVPairList record = new KVPairList();

    		for (KVPair pair : this.table){
                if(pair != null && pair != TOMBSTONE){
                	record.addBack(pair.getKey(),pair.getValue());
                }
                num_probes ++;
            }
    		
    		this.table = new KVPair[pr];
    		
    		if (record.size() != 0) {
    			for (KVPair p : record) {
    				num_probes += this.put(p.getKey(), p.getValue()).getProbes();
    			}
    		}
    	}
    	
    	// find proper spot to place variable
    	int hashIdx = this.hash(key);
    	
    	while(!(this.table[hashIdx] == null)) {
    		num_probes+=1;
    		if (hashIdx >= this.table.length-1) {
    			hashIdx = 0;
    		}else {
    			hashIdx+= 1;
    		}
    	}
    	num_probes += 1;
    	this.table[hashIdx] = new KVPair(key, value);
    	this.count ++;
    	return new Probes(value,num_probes);
    }

    @Override
    public Probes get(String key) {
    	int hashIdx = this.hash(key);
    	int num_probes = 1; // always guarenteed a probe
    	while(this.table[hashIdx] != null){
    		if (this.table[hashIdx] != this.TOMBSTONE && this.table[hashIdx].getKey().equals(key)){
    			return new Probes(table[hashIdx].getValue(), num_probes);
    		}
    		if (hashIdx >= this.table.length-1) {
    			hashIdx = 0;
    		}else {
    			hashIdx+= 1;
    		}
    		num_probes += 1;
    	}
    	
    	return new Probes(null, num_probes);
    }


    /**
     * <b>Return</b> the value associated with key in the {@link HashTable}, and <b>remove</b> the {@link phonebook.utils.KVPair} from the table.
     * If key does not exist in the database
     * or if key = {@code null}, this method returns {@code null}. This method is expected to run in <em>amortized constant time</em>.
     *
     * @param key The key to search for.
     * @return The {@link phonebook.utils.Probes} with associated value and the number of probe used. If the key is {@code null}, return value {@code null}
     * and 0 as number of probes; if the key doesn't exist in the database, return {@code null} and the number of probes used.
     */
    @Override
    public Probes remove(String key) {
    	String ret = "";
        if (this.soft == false) {
        	// we need to do hard deletion
        	int hashIdx = this.hash(key);
        	int num_probes = 1;
        	
        	// we need to find the correct key
        	while(this.table[hashIdx] != null){
        		String cKey = this.table[hashIdx].getKey();
        		if (cKey.equals(key)){
        			ret = this.table[hashIdx].getValue();
        			this.table[hashIdx] = null;
        			
        			KVPairList record = new KVPairList(); 
        			this.count --;
        			// loop through the rest of the cluster 
        			// find next elem
        			
        			int next = hashIdx+1;
        			next = next% this.table.length; 
        			

        			while (!(this.table[next] == null)){
        				record.addBack(this.table[next].getKey(),this.table[next].getValue());
        				this.count --; 
        				this.table[next] = null;
        				next += 1;
        				if (next >= this.table.length-2) {
        					next = next% this.table.length; 
        				}
        				num_probes += 1;
        					
        					
            			}
        			num_probes += 1;
        			
        			if (record.size() >0) {
        				for (KVPair p: record) {
        					Probes temp = this.put(p.getKey(), p.getValue());
        					num_probes += temp.getProbes();
        				}
        			}
        			return new Probes(ret, num_probes);
        		}else {
        			num_probes += 1;
                    hashIdx = (hashIdx + 1) % this.table.length;
        		}
        	}
        	return new Probes(null, num_probes);
        }else {// assume soft
        	int hashIdx = this.hash(key);
            int num_probes = 1;
        	while (this.table[hashIdx] != null) {
        		if (this.table[hashIdx] == this.TOMBSTONE) {
        			num_probes += 1;
        			hashIdx = (hashIdx + 1)%this.table.length;
        		}else if(this.table[hashIdx].getKey().equals(key)) {
        			ret = this.table[hashIdx].getValue();
        			this.table[hashIdx] = this.TOMBSTONE; // set the target to TOMBSTONE
                    this.t_count += 1;
                    
                    return new Probes(ret, num_probes);
        		}else {
        			hashIdx = (hashIdx +1) % this.table.length;
        			num_probes += 1;
        		}
        		
        	}
        	return new Probes(null, num_probes);
        	
        }
    }

    @Override
    public boolean containsKey(String key) {
    	for (KVPair p : this.table) {
    		if(p != null && p.getKey().equals(key)) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    public boolean containsValue(String value) {
    	for (KVPair p : this.table) {
    		if(p != null && p.getValue().equals(value)) {
    			return true;
    		}
    	}
    	return false;
        
    }

    @Override
    public int size() {
    	return this.count - this.t_count;
    }

    @Override
    public int capacity() {
    	return this.table.length;
    } 
    
    
}


