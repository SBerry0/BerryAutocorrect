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
        if (dictHash.contains(typed)) {
            return new String[0];
        }
        ArrayList<String> candidates = new ArrayList<>();
        int stringLen = typed.length();
        for (String s : dictionary) {
            if (s.length() > stringLen-threshold-1 && s.length() < stringLen+threshold+1) {
                candidates.add(s);
            }
        }

        int[] positions = new int[threshold];
        for (int i = 0; i < threshold; i++) {
            positions[i] = 0;
        }
        ArrayList<String> options = new ArrayList<>();
        for (String s : candidates) {
            int editDistance = calcDistance(typed, s);
            if (editDistance <= positions.length) {
                options.add(positions[editDistance-1], s);
                for (int i = editDistance-1; i < positions.length; i++) {
                    positions[i]++;
                }
            }
        }

        if (options.isEmpty()) {
            return new String[] {"invld"};
        }

        String[] out = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            out[i] = options.get(i);
        }

        return out;
    }


    public int calcDistance(String s, String goal) {
        int[][] board = new int[s.length()+1][goal.length()+1];

        for (int i = 0; i < s.length()+1; i++) {
            board[i][0] = i;
        }
        for (int i = 0; i < goal.length()+1; i++) {
            board[0][i] = i;
        }

        for (int i = 1; i < s.length()+1; i++) {
            for (int j = 1; j < goal.length()+1; j++) {
                if (s.charAt(i-1) == goal.charAt(j-1)) {
                    board[i][j] = board[i-1][j-1];
                }
                else {
                    board[i][j] = 1 + Math.min(board[i-1][j],
                                      Math.min(board[i][j-1],
                                                board[i-1][j-1]));
                }
            }
        }
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
        String[] dict = loadDictionary("large");

        Scanner s = new Scanner(System.in);
        Autocorrect a = new Autocorrect(dict, 3);

        while (true) {
            System.out.println("Please enter a word:");
            String input = s.nextLine();
            if (input.equals("q")) {
                break;
            }
            String[] results = a.runTest(input);
            if (results.length == 0) {
                System.out.println(input + " is spelled correctly");
            } else if (results[0].equals("invld")) {
                System.out.println("No matches found.");
            } else {
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