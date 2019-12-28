import java.util.*;

public class SnowmanPlayer {
    // If true, print the number of possible words at each guess.
    private static final boolean VERBOSE = false;
    
    // Data structures maintained by the methods.
    private static List<List<String>> wordLists = new ArrayList<>();
    private static List<String> possibleWords;
    private static String allowed;
    private static String previousPattern = "$$$$";
    private static char previousGuess = '\0';
    
    public static String getAuthor() {
        return "Kokkarinen, Ilkka";
    }
    
    public static void startGame(String[] words, int minLength, int maxLength, String allowedChars) {
        // Create a list of words for each possible length.
        for(int i = 0; i <= maxLength; i++) {
            wordLists.add(new ArrayList<String>());
        }
        // Insert each word to the appropriate list.
        for(String word: words) {
            wordLists.get(word.length()).add(word);
        }
        allowed = allowedChars;
    }
    
    // From the given list of words, produce a list of words that match the given pattern.
    private static List<String> filterPattern(String pattern, List<String> words) {
        List<String> result = new ArrayList<String>();
        for(String word: words) {
            boolean isMatch = true;
            for(int i = 0; i < word.length(); i++) {
                char c1 = pattern.charAt(i);
                char c2 = word.charAt(i);
                if(c1 == SnowmanRunner.BLANK) { 
                    if(pattern.indexOf(c2) == -1) { continue; }
                    isMatch = false; break;
                }
                if(c1 != c2) { isMatch = false; break; }
            }
            if(isMatch) { result.add(word); }
        }
        return result;
    }
    
    // From the given list of words, produce a list of words that do NOT contain the character c.
    private static List<String> filterChar(char c, List<String> words) {
        List<String> result = new ArrayList<String>();
        for(String word: words) {
            if(word.indexOf(c) == -1) { result.add(word); }
        }
        return result;
    }
 
    // Find the character that occurs in largest number of remaining words.
    public static char guessLetter(String pattern, String previousGuesses) {
        // If previous guess was a match, update the list of possible words
        if(!pattern.equals(previousPattern)) {
            possibleWords = filterPattern(pattern, possibleWords);
            previousPattern = pattern;
        }
        // If previous guess was not a match, remove words that contain that character
        else if(previousGuesses.length() > 0) {
            possibleWords = filterChar(previousGuesses.charAt(previousGuesses.length() - 1),
                                        possibleWords);
        }
        if(VERBOSE) { System.out.print("[" + possibleWords.size() + "]"); }

        int bestScore = Integer.MAX_VALUE; 
        char bestChar = '$';
        for(int i = 0; i < allowed.length(); i++) {
            char c = allowed.charAt(i);
            if(previousGuesses.indexOf(c) > -1) { continue; }
            // Count how many remaining words the character c does not occur in.
            int score = 0;
            for(String word: possibleWords) {
                if(word.indexOf(c) == -1) { score++; }
            }
            if(score == 0) { return c; } // This letter occurs in every possible word, so use it.
            if(score < bestScore) { // This letter beats all the ones we have seen so far.
                bestScore = score; bestChar = c;
            }
        }
        return bestChar;
    }

    public static void startNewWord(int length) {
        // Nothing to do here except initialize the list of possible words and previous pattern.
        possibleWords = wordLists.get(length);
        previousPattern = "\0";
    }   
}
