import java.io.*;
import java.util.*;

public class App {
    static class TrieNode {
        Map<Character, TrieNode> children;
        TrieNode failureLink;
        List<Integer> patternIndices;
        boolean isEndOfPattern;

        TrieNode() {
            children = new HashMap<>();
            patternIndices = new ArrayList<>();
            isEndOfPattern = false;
            failureLink = null;
        }
    }

    static class AhoCorasick {
        private TrieNode root;
        private String[] patterns;

        AhoCorasick(String[] patterns) {
            this.root = new TrieNode();
            this.patterns = patterns;
            buildTrie(patterns);
            buildFailureLinks();
        }

        private void buildTrie(String[] patterns) {
            for (int i = 0; i < patterns.length; i++) {
                TrieNode current = root;
                String pattern = patterns[i];
                
                for (char c : pattern.toCharArray()) {
                    current.children.putIfAbsent(c, new TrieNode());
                    current = current.children.get(c);
                }
                
                current.isEndOfPattern = true;
                current.patternIndices.add(i);
            }
        }

        private void buildFailureLinks() {
            Queue<TrieNode> queue = new LinkedList<>();
            root.failureLink = root;
            
            // Set failure links for depth 1 nodes to root
            for (Map.Entry<Character, TrieNode> entry : root.children.entrySet()) {
                TrieNode node = entry.getValue();
                node.failureLink = root;
                queue.add(node);
            }

            // Build failure links for remaining nodes
            while (!queue.isEmpty()) {
                TrieNode current = queue.poll();
                
                for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                    char c = entry.getKey();
                    TrieNode child = entry.getValue();
                    queue.add(child);

                    TrieNode failureNode = current.failureLink;
                    while (failureNode != root && !failureNode.children.containsKey(c)) {
                        failureNode = failureNode.failureLink;
                    }

                    child.failureLink = failureNode.children.containsKey(c) ? 
                        failureNode.children.get(c) : root;
                }
            }
        }

        public Map<Integer, List<Integer>> findPatterns(String text) {
            Map<Integer, List<Integer>> results = new HashMap<>();
            TrieNode current = root;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                
                while (current != root && !current.children.containsKey(c)) {
                    current = current.failureLink;
                }

                if (!current.children.containsKey(c)) {
                    current = root;
                    continue;
                }

                current = current.children.get(c);
                
                if (current.isEndOfPattern) {
                    for (int patternIndex : current.patternIndices) {
                        String pattern = patterns[patternIndex];
                        int startPos = i - pattern.length() + 1;
                        
                        if (startPos >= 0) {
                            // Get the exact text segment
                            String foundPattern = text.substring(startPos, i + 1);
                            
                            // For multi-character patterns
                            if (pattern.length() > 1) {
                                if (foundPattern.equals(pattern)) {
                                    // Check if it's a complete word
                                    boolean isWordStart = startPos == 0 || !Character.isLetter(text.charAt(startPos - 1));
                                    boolean isWordEnd = (i + 1 >= text.length()) || !Character.isLetter(text.charAt(i + 1));
                                    
                                    if (isWordStart && isWordEnd) {
                                        results.computeIfAbsent(patternIndex, k -> new ArrayList<>())
                                               .add(startPos);
                                    }
                                }
                            } 
                            // For single characters
                            else if (foundPattern.equals(pattern)) {
                                results.computeIfAbsent(patternIndex, k -> new ArrayList<>())
                                       .add(startPos);
                            }
                        }
                    }
                }

                // Check failure link for additional matches
                TrieNode failureNode = current.failureLink;
                while (failureNode != root) {
                    if (failureNode.isEndOfPattern) {
                        for (int patternIndex : failureNode.patternIndices) {
                            String pattern = patterns[patternIndex];
                            int startPos = i - pattern.length() + 1;
                            
                            if (startPos >= 0) {
                                String foundPattern = text.substring(startPos, i + 1);
                                if (foundPattern.equals(pattern)) {
                                    boolean isWordStart = startPos == 0 || !Character.isLetter(text.charAt(startPos - 1));
                                    boolean isWordEnd = (i + 1 >= text.length()) || !Character.isLetter(text.charAt(i + 1));
                                    
                                    if (isWordStart && isWordEnd) {
                                        results.computeIfAbsent(patternIndex, k -> new ArrayList<>())
                                               .add(startPos);
                                    }
                                }
                            }
                        }
                    }
                    failureNode = failureNode.failureLink;
                }
            }

            return results;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        // Get the source file name
        System.out.print("Enter the source file for the text: ");
        String fileName = scanner.nextLine();

        // Read the text from file
        String text = readFile(fileName);

        // Get the number of patterns
        System.out.print("Enter the number of patterns: ");
        int z = Integer.parseInt(scanner.nextLine());

        // Read the patterns
        String[] patterns = new String[z];
        for (int i = 0; i < z; i++) {
            System.out.printf("Enter pattern %d: ", i + 1);
            patterns[i] = scanner.nextLine();
        }

        // Create Aho-Corasick automaton and find patterns
        AhoCorasick ac = new AhoCorasick(patterns);
        Map<Integer, List<Integer>> occurrences = ac.findPatterns(text);

        // Display results
        for (int i = 0; i < patterns.length; i++) {
            System.out.printf("Pattern %d occurs at positions: ", i + 1);
            List<Integer> positions = occurrences.getOrDefault(i, new ArrayList<>());
            if (positions.isEmpty()) {
                System.out.println("no occurrences found");
            } else {
                System.out.println(String.join(", ", 
                    positions.stream().map(String::valueOf).toArray(String[]::new)));
            }
        }

        scanner.close();
    }

    private static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
