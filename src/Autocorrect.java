import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Sohum Berry
 */
public class Autocorrect {
    String[] dictionary;
    int threshold;
    HashSet<String> dictHash;
    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param threshold The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int threshold) {
        this.threshold = threshold;
        dictionary = words;
        dictHash = new HashSet<>(List.of(dictionary));
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        // Use hashmap to quickly figure out if the word is already in the dictionary
        if (dictHash.contains(typed)) {
            return new String[0];
        }
        // Marginally narrow down the number of words to search through by restricting the lengths we look at
        ArrayList<String> candidates = new ArrayList<>();
        int stringLen = typed.length();
        for (String s : dictionary) {
            if (s.length() > stringLen-threshold-1 && s.length() < stringLen+threshold+1) {
                candidates.add(s);
            }
        }

        // Adds each of the candidate words into a sorted position based on its edit distance
        // Holds the indices of where each edit distance starts within the ArrayList
        int[] positions = new int[threshold];
        for (int i = 0; i < threshold; i++) {
            positions[i] = 0;
        }
        // The output ArrayList
        ArrayList<String> options = new ArrayList<>();
        // For each candidate, calculate its edit distance
        for (String s : candidates) {
            int editDistance = calcDistance(typed, s);
            // If the edit distance is within the threshold, add it to its edit distance's position in the arrayList
            if (editDistance <= positions.length) {
                options.add(positions[editDistance-1], s);
                // Increment the indices of all larger edit distances to account for the new item that was inserted
                for (int i = editDistance-1; i < positions.length; i++) {
                    positions[i]++;
                }
            }
        }

        // If no words could be found, return an array with a word in it to differentiate from perfectly spelled word
        if (options.isEmpty()) {
            // Intentionally misspell the word so it cannot be mistaken for coming from the dictionary
            return new String[] {"invld"};
        }

        // If there are possible words, convert ArrayList into an Array and return it
        String[] out = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            out[i] = options.get(i);
        }
        return out;
    }

    // Calculates the number of edits that need to be made to a word to match a goal word
    public int calcDistance(String s, String goal) {
        // Create the tabulation board with a buffer
        int[][] board = new int[s.length()+1][goal.length()+1];

        // Fill the buffers with the corresponding lengths if one word's length it 0
        for (int i = 0; i < s.length()+1; i++) {
            board[i][0] = i;
        }
        for (int i = 0; i < goal.length()+1; i++) {
            board[0][i] = i;
        }

        // Iterate through the board
        for (int i = 1; i < s.length()+1; i++) {
            for (int j = 1; j < goal.length()+1; j++) {
                // If the characters are the same, place the edit distance of the tails of each into that square
                if (s.charAt(i-1) == goal.charAt(j-1)) {
                    board[i][j] = board[i-1][j-1];
                }
                else {
                    // Otherwise add one to the minimum of the three possibilities
                    board[i][j] = 1 + Math.min(board[i-1][j],
                                      Math.min(board[i][j-1],
                                                board[i-1][j-1]));
                }
            }
        }
        // Return the last square in the board to get the edit distance
        return board[s.length()][goal.length()];
    }


    /**
     * Loads a dictionary of words from the provided textfiles in the dictionaries directory.
     * @param dictionary The name of the textfile, [dictionary].txt, in the dictionaries directory.
     * @return An array of Strings containing all words in alphabetical order.
     */
    private static String[] loadDictionary(String dictionary)  {
        try {
            String line;
            BufferedReader dictReader = new BufferedReader(new FileReader("dictionaries/" + dictionary + ".txt"));
            line = dictReader.readLine();

            // Update instance variables with test data
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                line = dictReader.readLine();
                words[i] = line;
            }
            return words;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // Load in the dictionary
        String[] dict = loadDictionary("large");

        // Initialize the Scanner and the Autocorrect class
        Scanner s = new Scanner(System.in);
        Autocorrect a = new Autocorrect(dict, 3);

        // Inside a while loop that will keep the program running
        while (true) {
            // Request and read in a word
            System.out.println("Please enter a word:");
            String input = s.nextLine();

            // Break out of the loop to end the program if the user types 'q'
            if (input.equals("q")) {
                System.out.println("Bye!");
                break;
            }

            // Get the possible spellings of the  misspelled word from the Autocorrect class
            String[] results = a.runTest(input);
            // If it's empty, the word is in the dictionary
            if (results.length == 0) {
                System.out.println(input + " is spelled correctly");
            } else if (results[0].equals("invld")) {
                // If the first item in the results is a misspelled word, there were no matched found
                System.out.println("No matches found.");
            } else {
                // Otherwise, provide all the possible words to the given threshold with the proper formatting
                System.out.println("Did you mean:");
                for (int i = 0; i < results.length; i++) {
                    System.out.print(results[i]);

                    if (i < results.length-1) {
                        System.out.print(", ");
                    } else {
                        System.out.println();
                    }
                }
            }
        }
    }
}