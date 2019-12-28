import java.util.*;

public class WordPacking {
   
    /**
     * Returns the Ryerson student ID of the author of this class.
     * @return The Ryerson student ID of the author of this class.
     */
    public static String getRyersonID() { return "123456789"; }
    
    /**
     * Returns the name of the author of this class, in format "Lastname, Firstname".
     * @return The name of the author of this class.
     */
    public static String getAuthorName() { return "Kokkarinen, Ilkka"; }
    
    // Decide whether two words cannot be placed into same bin.
    private static boolean inConflict(String w1, String w2) {
        for(int i = 0; i < 5; i++) {
            if(w1.charAt(i) == w2.charAt(i)) { return true; }
        }
        return false;
    }
    
    // Encode index for the proposition "Word w is in bin b, out of k bins."
    private static int enc(int w, int b, int k) {
        return k * w + b + 1;
    }
    
    /**
     * Distributes the given words into bins so that every word is in exactly one bin, and
     * no two words in the same bin have the same character in any position.
     * @param words The words to be distributed into bins.
     * @return A list of list of strings, so that each of these lists contains the words
     * in that bin.
     */
    public static List<List<String>> wordPack(List<String> words) {
        int n = words.size();
        int[][] conflicts = new int[26][5];
        // Count which letters are used in how many words and positions.
        for(String word: words) {
            for(int j = 0; j < 5; j++) {
                conflicts[word.charAt(j) - 'a'][j]++;
            }
        }
        // Find the most discriminating letter and position. 
        int max = -1, let = 0, col = 0;
        for(int i = 0; i < 26; i++) {
            for(int j = 0; j < 5; j++) {
                if(conflicts[i][j] > max) {
                    max = conflicts[i][j]; let = (char)('a' + i); col = j; 
                }
            }
        }
        
        // For each word, create the list of later words that it is in conflict with.
        int totalConflictCount = 0;
        List<List<Integer>> conflict = new ArrayList<>();
        for(int i = 0; i < words.size(); i++) {
            List<Integer> cc = new ArrayList<>();
            String word = words.get(i);
            for(int j = i + 1; j < words.size(); j++) {
                if(inConflict(word, words.get(j))) {
                    cc.add(j); totalConflictCount++;
                }
            }
            conflict.add(cc);
        }
        
        int kmin = max, kmax = n;
        List<List<String>> result = null;
        while(kmin <= kmax) {
            // Number of bins to attempt this round.
            int k = (kmin + kmax) / 2;
            
            // Create the clauses for placing the given n words into k separate bins.
            int clauseCount = max + n + k * totalConflictCount;
            int[][] clauses = new int[clauseCount][];
            int loc = 0; int[] curr;
            
            // Words in the longest conflict are all placed in particular bins.
            // This does not eliminate the solution, if one exists.
            for(int i = 0; i < words.size(); i++) {
                if(words.get(i).charAt(col) == let) {
                    curr = new int[1]; curr[0] = enc(i, loc, k);
                    clauses[loc++] = curr;
                }
            }
            assert loc == max;
            
            // Every word must be in some bin.
            for(int i = 0; i < n; i++) {
                curr = new int[k];
                for(int b = 0; b < k; b++) { curr[b] = enc(i, b, k); }
                clauses[loc++] = curr;
            }
            
            // No two words in conflict with each other may be in the same bin.
            for(int i = 0; i < n; i++) {
                for(int j: conflict.get(i)) {
                    for(int b = 0; b < k; b++) {
                        curr = new int[2]; curr[0] = -enc(i, b, k); curr[1] = -enc(j, b, k);
                        clauses[loc++] = curr;
                    }
                }
            }
            
            // All that should produce the correct number of clauses.
            assert loc == clauseCount : "" + loc + " " + clauseCount;
            
            // Then, just have the SAT Solver compute the solution for us.
            boolean[] solution = SATSolver.solveDPLL(n * k, clauses);
            
            // If a solution was found, convert it into the requested list of bins.
            if(solution != null) {                
                result = new ArrayList<>();
                for(int b = 0; b < k; b++) {
                    result.add(new ArrayList<String>());                    
                }
                boolean[] taken = new boolean[n];
                for(int v = 0; v <= n * k; v++) {
                    if(solution[v]) {
                        int i = (v - 1) / k;
                        if(taken[i]) { continue; }
                        taken[i] = true;
                        int b = (v - 1) % k;
                        assert enc(i, b, k) == v;
                        result.get(b).add(words.get(i));
                    }
                }
                kmax = k - 1;
            }
            else { kmin = k + 1; }
        }
        return result;
    }
}
