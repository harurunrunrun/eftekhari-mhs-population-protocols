import java.util.*;

public class junta_election_constant_message_size {
	
	/**
	 * We define consecutive disjoint intervals G_0, G_1, G_2 {green} and R_0, R_1, R_2 {red}) partitioning the natural numbers.
	We call {R_i}'s last element d_i {door}. Each agent keeps a local counter, initially 0, that is incremented on some interactions.
	An agent is {in round i} if its counter is in {G_i union R_i}.
	The goal is to get every agent to count up until the round equal to the maximum level "max" generated by any agent and stop its counter at d_max. 
	An agent with level $l$ in round $i$ is \concept{eager} if $i < l$ and \concept{cautious} otherwise.
	Intuitively, eager agents race through doors until their own level, telling all other agents to keep going, 
	but become cautious at and beyond their own level, 
	advancing past a door into the next round only if another agent tells them to do so (via a message $m=\on$).
	The protocol terminates when all agents reach the last door (d_max).
	 *
	 */

	static int n;
	static int c = 16;
	static int r = 2;
	static Random rnd;
	static int max_range = 4000000; // Maximum value for population size. 
	static int number_of_simulations = 10; // Number of simulations per population size.
	static int max_level;
	static int waiting_at_max_d = 0; // The protocol terminates when this value reaches n (population size). 
	static int[] di;
	static int[] gi;

	public static void main(String[] args) {
		rnd = new Random();
		for (int range = 100; range < max_range; range++) {
			range = range * 2;
			n = rnd.nextInt(range);
			float log_2_n = (float) (Math.log(n)/Math.log(2));
			System.out.printf("----------------START SIMULATION FOR N = %d---------------------- \n", n);
			long total_number_of_interactions = 0;
			for (int sim = 0; sim < number_of_simulations; sim++) {		
				print("Simulation "+ sim);
				List<Agent> agents = new ArrayList<>();
				for (int i = 0; i < n; i++) {
					int g = generate_geometric(rnd);
					agents.add(new Agent(g, compute_interval(g)));
				}
				print_distribution_grv(agents);
				di = new int[max_level + 1];
				gi = new int[max_level + 1];
				precompute_gi_di();
				
//				Uncomment the following code to print G_i, d_i values: 
//				print(" end of G_i intervals: ");
//				for (int x : gi) {
//					print(x + " , ");
//				}
//				
//				print("\n d_i points: ");
//				for (int x : di) {
//					print(x + " , ");
//				}
				while (true) {
					int a = rnd.nextInt(n);
					int b = rnd.nextInt(n);
					while (a == b) {
						b = rnd.nextInt(n);
					}
					Agent rec = agents.get(a);
					Agent sen = agents.get(b);

					total_number_of_interactions++ ;
					interaction(rec, sen);
					interaction(sen, rec);

					if (waiting_at_max_d == n) {
						break;
					}
				}
				waiting_at_max_d = 0;						
			}
			float average_number_of_interactions = total_number_of_interactions/number_of_simulations;
			System.out.printf("\nAverage number of interactions from %d simulations: %.2f \n",number_of_simulations, average_number_of_interactions);
			System.out.printf("\nAverage time (number of interactions / n) from %d simulations: %.2f \n",number_of_simulations, (float)(average_number_of_interactions/n));		
			System.out.printf("\nAverage time from %d simulations: %.2f nlogn \n \n",number_of_simulations, (float)(average_number_of_interactions/(n*log_2_n)));		
			System.out.printf("----------------END SIMULATION FOR N = %d---------------------- \n \n", n);
		}
	}

	// protocol description:
	private static void interaction(Agent rec, Agent sen) {
		// If not waiting at the door --> increment counter
		if (atDoor(rec) && rec.level <= rec.curr_level) {
			// Checking termination.
			if (rec.count == di[max_level] && rec.noob) {
				waiting_at_max_d++;
				rec.noob = false;
			}
			if (sen.message) {
				inc_counter(rec);
			}
		} else {
			inc_counter(rec);
		}

		if (isGreen(rec) && rec.level <= rec.curr_level) {
			rec.message = true;
		}

		if (isRed(rec) && rec.level <= rec.curr_level) {
			rec.message = false;
		}

		if (rec.level > rec.curr_level) {
			rec.message = true;
		}
	}

	private static boolean isGreen(Agent agent) {
		return (agent.count <= gi[agent.curr_level]);
	}

	private static boolean isRed(Agent agent) {
		return (!isGreen(agent) && agent.count < di[agent.curr_level]);
	}

	private static boolean atDoor(Agent agent) {
		return (agent.count == di[agent.curr_level]);
	}

	private static void inc_counter(Agent a) {
		a.count++;
		a.curr_level = compute_curr_level(a);
	}

	private static int compute_curr_level(Agent agent) {
		int i = -1;
		do {
			i++;
		} while (agent.count > di[i]);
		return i;
	}

	private static void precompute_gi_di() {
		int green, red;
		for (int i = 0; i <= max_level; i++) {
			green = (int) (c * Math.pow(r, i));
			//if  r==2; then the constant = 3
			//if  r==4; then the constant = 6
			red = (int) (3 * c * Math.pow(r, i - 1));
			if (i == 0) {
				di[i] = green + red;
				gi[i] = green;
			} else {
				di[i] = di[i - 1] + green + red;
				gi[i] = di[i - 1] + green;
			}
		}
	}

	private static int compute_interval(int g) {
		if (g > 1)
			return (int) (Math.ceil(Math.log(g) / Math.log(2)));
		else
			return 0;
	}

	// Generate a geometric random variable (minimum = 1).
	private static int generate_geometric(Random r) {
		int g = 1;
		while (r.nextBoolean()) {
			g++;
		}
		return g;
	}	

	// Print the distribution of geometric random variables generated by agents.  
	private static void print_distribution_grv(List<Agent> agents) {
		int max = 0;
		for (Agent a : agents) {
			max = Math.max(max, a.grv);
		}
		max_level = (int) Math.ceil(Math.log(max) / Math.log(2));
		ArrayList<Integer> dist = new ArrayList<Integer>();
		print("   		Values that agents generated for geometric random variables:   [");
		for (int i = 0; i < max + 1; i++) {
			dist.add(0);
			print(i +", ");
		}
		print(" ]   ---> ");
		
		for (Agent a : agents) {
			dist.set(a.grv, dist.get(a.grv) + 1);
		}
		println("   number of agents who generated each value:   " + dist);
		println("");
	}

	public static void println(Object obj) {
		System.out.println(obj);
	}

	public static void print(Object obj) {
		System.out.print(obj);
	}
}

class Agent {
	int grv, curr_level, level, count;
	boolean message, noob;

	public Agent(int g, int l) {
		this.grv = g; // geometric random variable
		this.message = false;
		this.curr_level = 0;
		this.count = 0;
		this.level = l;
		this.noob = true;
	}
}
